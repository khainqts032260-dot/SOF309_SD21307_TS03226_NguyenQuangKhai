<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đăng nhập | Celine Closet" scope="request" />
<%@ include file="common/header.jsp" %>
<main class="auth-page refined-auth-page centered-auth-page">
    <section class="auth-shell">
        <nav class="auth-tabs" aria-label="Đăng nhập hoặc đăng ký"><a class="active" href="${ctx}/login">Đăng nhập</a><a href="${ctx}/register">Đăng ký</a></nav>
        <form class="auth-card refined-auth-card compact-auth-card" action="${ctx}/login" method="post"><input type="hidden" name="next" value="${param.next}">
            <p class="eyebrow">Tài khoản C&amp;C</p><h1>Chào mừng trở lại</h1>
            <p class="auth-intro">Đăng nhập để theo dõi đơn hàng, tích điểm và sử dụng voucher thành viên.</p>
            <c:if test="${param.session eq 'expired'}"><div class="alert error">Phiên đăng nhập cũ không còn hợp lệ. Vui lòng đăng nhập lại.</div></c:if>
            <c:if test="${not empty error}"><div class="alert error">${error}</div></c:if>
            <c:if test="${not empty success}"><div class="alert success">${success}</div></c:if>
            <label>Email</label><input type="email" name="email" value="${param.email}" required pattern="[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}" placeholder="customer@demo.vn">
            <label>Mật khẩu</label><div class="password-field"><input id="loginPassword" type="password" name="password" required placeholder="Nhập mật khẩu"><button type="button" class="password-eye" data-password-toggle="loginPassword" aria-label="Hiện mật khẩu"><i class="fa-regular fa-eye"></i></button></div>
            <div class="auth-helper-row"><label class="remember-label"><input type="checkbox"> Ghi nhớ</label><a href="${ctx}/forgot-password" title="Khôi phục mật khẩu tài khoản khách hàng">Quên mật khẩu khách hàng?</a></div>
            <button class="btn btn-dark auth-main-button" type="submit">Đăng nhập</button>
            <div class="auth-divider"><span>hoặc</span></div>
            <a class="google-login-button" href="${ctx}/auth/google"><i class="fa-brands fa-google"></i> Đăng nhập bằng Google</a>
            <p class="auth-switch">Chưa có tài khoản? <a class="auth-switch-link" href="${ctx}/register">Đăng ký ngay</a></p>
            <c:if test="${googleConfigMissing}"><small class="config-note">Điền Google OAuth trong <b>src/main/resources/app.properties</b> để bật đăng nhập Google.</small></c:if>
        </form>
    </section>
</main>
<%@ include file="common/footer.jsp" %>
