package vn.celineclosset.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.celineclosset.util.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/** Google OAuth 2.0 Authorization Code flow cho ứng dụng web server. */
@WebServlet({"/auth/google", "/auth/google/callback"})
public class GoogleAuthController extends BaseController {
    private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo";
    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("/auth/google/callback".equals(req.getServletPath())) {
                handleCallback(req, resp);
            } else {
                startLogin(req, resp);
            }
        } catch (Exception e) {
            req.setAttribute("error", "Không thể đăng nhập Google: " + e.getMessage());
            view(req, resp, "login.jsp");
        }
    }

    private void startLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String clientId = AppConfig.get("google.clientId");
        String redirectUri = AppConfig.get("google.redirectUri");
        if (clientId.isBlank() || redirectUri.isBlank()) {
            req.setAttribute("googleConfigMissing", true);
            req.setAttribute("error", "Chưa cấu hình GOOGLE_CLIENT_ID và GOOGLE_REDIRECT_URI trong app.properties hoặc biến môi trường.");
            view(req, resp, "login.jsp");
            return;
        }

        byte[] randomBytes = new byte[24];
        new SecureRandom().nextBytes(randomBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        req.getSession().setAttribute("googleOAuthState", state);

        String url = AUTH_ENDPOINT
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(state)
                + "&prompt=select_account";
        resp.sendRedirect(url);
    }

    private void handleCallback(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String expectedState = String.valueOf(req.getSession().getAttribute("googleOAuthState"));
        String state = req.getParameter("state");
        String code = req.getParameter("code");
        req.getSession().removeAttribute("googleOAuthState");

        if (state == null || !state.equals(expectedState) || code == null || code.isBlank()) {
            throw new IllegalArgumentException("Phiên xác thực không hợp lệ hoặc đã hết hạn.");
        }

        String clientId = AppConfig.get("google.clientId");
        String clientSecret = AppConfig.get("google.clientSecret");
        String redirectUri = AppConfig.get("google.redirectUri");
        if (clientId.isBlank() || clientSecret.isBlank() || redirectUri.isBlank()) {
            throw new IllegalStateException("Google OAuth chưa được cấu hình đầy đủ.");
        }
        String body = "code=" + encode(code)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(redirectUri)
                + "&grant_type=authorization_code";

        HttpRequest tokenRequest = HttpRequest.newBuilder(URI.create(TOKEN_ENDPOINT))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
        if (tokenResponse.statusCode() / 100 != 2) {
            throw new IllegalStateException("Google không trả về access token hợp lệ.");
        }
        Map<String, Object> tokenJson = gson.fromJson(tokenResponse.body(), new TypeToken<Map<String, Object>>() {}.getType());
        String accessToken = String.valueOf(tokenJson.get("access_token"));

        HttpRequest userInfoRequest = HttpRequest.newBuilder(URI.create(USERINFO_ENDPOINT))
                .header("Authorization", "Bearer " + accessToken)
                .GET().build();
        HttpResponse<String> userInfoResponse = httpClient.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
        if (userInfoResponse.statusCode() / 100 != 2) {
            throw new IllegalStateException("Không đọc được thông tin tài khoản Google.");
        }
        Map<String, Object> profile = gson.fromJson(userInfoResponse.body(), new TypeToken<Map<String, Object>>() {}.getType());
        String email = String.valueOf(profile.get("email"));
        String subject = String.valueOf(profile.get("sub"));
        String name = profile.get("name") == null ? email : String.valueOf(profile.get("name"));
        String picture = profile.get("picture") == null ? null : String.valueOf(profile.get("picture"));
        boolean emailVerified = Boolean.TRUE.equals(profile.get("email_verified"));
        if (email.isBlank() || subject.isBlank() || !emailVerified) {
            throw new IllegalStateException("Tài khoản Google không cung cấp email đã xác minh hợp lệ.");
        }

        Map<String, Object> user = accountDAO.loginOrCreateGoogle(subject, email, name, picture);
        if (user == null) {
            throw new IllegalStateException("Tài khoản đang bị khóa.");
        }
        req.getSession().setAttribute("auth", user);
        resp.sendRedirect(req.getContextPath() + (isBackOffice(user) ? adminStartPath(user) : "/home"));
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
