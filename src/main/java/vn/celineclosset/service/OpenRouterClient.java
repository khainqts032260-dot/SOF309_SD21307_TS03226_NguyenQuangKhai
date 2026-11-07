package vn.celineclosset.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import vn.celineclosset.util.AppConfig;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client gọi OpenRouter ở phía server. API key không bao giờ được gửi xuống trình duyệt.
 */
public final class OpenRouterClient {
    public record Result(boolean success, String message, String errorCode) {
        public static Result ok(String message) {
            return new Result(true, message, "");
        }

        public static Result fail(String code) {
            return new Result(false, "", code == null ? "UNKNOWN" : code);
        }
    }

    private final HttpClient httpClient;
    private final String endpoint;
    private final String apiKey;
    private final String model;
    private final String systemPrompt;
    private final int requestTimeoutSeconds;
    private final int maxTokens;
    private final int historyLimit;
    private final double temperature;

    public OpenRouterClient() {
        int connectTimeout = Math.max(3, AppConfig.getInt("openrouter.connectTimeoutSeconds", 15));
        this.requestTimeoutSeconds = Math.max(10, AppConfig.getInt("openrouter.requestTimeoutSeconds", 60));
        this.maxTokens = Math.max(200, AppConfig.getInt("openrouter.maxTokens", 900));
        this.historyLimit = Math.max(4, AppConfig.getInt("openrouter.historyLimit", 18));
        this.temperature = parseDouble(AppConfig.get("openrouter.temperature", "0.25"), 0.25d);
        this.endpoint = AppConfig.get("openrouter.api.url", "https://openrouter.ai/api/v1/chat/completions");
        String configuredKey = AppConfig.get("openrouter.apiKey", "");
        String environmentKey = System.getenv("OPENROUTER_API_KEY");
        this.apiKey = !configuredKey.isBlank() ? configuredKey : (environmentKey == null ? "" : environmentKey.trim());
        this.model = AppConfig.get("openrouter.model", "google/gemma-4-26b-a4b-it:free");
        this.systemPrompt = loadPrompt(AppConfig.get(
                "openrouter.promptResource", "chatbot/celine-chatbox-system-prompt.txt"));
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public Result answer(List<Map<String, Object>> messages, String subject, String customerName) {
        if (apiKey.isBlank()) {
            return Result.fail("API_KEY_MISSING");
        }
        if (endpoint.isBlank() || model.isBlank()) {
            return Result.fail("CONFIG_INVALID");
        }

        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            payload.addProperty("temperature", temperature);
            payload.addProperty("max_tokens", maxTokens);

            JsonArray apiMessages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", systemPrompt
                    + "\n\nChủ đề hiện tại: " + safe(subject)
                    + "\nTên khách hàng: " + safe(customerName));
            apiMessages.add(system);

            int from = Math.max(0, messages.size() - historyLimit);
            for (int i = from; i < messages.size(); i++) {
                Map<String, Object> item = messages.get(i);
                String content = safe(String.valueOf(item.getOrDefault("noiDung", "")));
                if (content.isBlank()) continue;
                String senderRole = String.valueOf(item.getOrDefault("vaiTroNguoiGui", "CUSTOMER"));
                JsonObject message = new JsonObject();
                message.addProperty("role", "CUSTOMER".equals(senderRole) ? "user" : "assistant");
                message.addProperty("content", content);
                apiMessages.add(message);
            }
            payload.add("messages", apiMessages);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(requestTimeoutSeconds))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("X-Title", "Celine Closet Customer Support")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8));

            String baseUrl = AppConfig.get("app.baseUrl", "");
            if (!baseUrl.isBlank()) {
                requestBuilder.header("HTTP-Referer", baseUrl);
            }

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Result.fail("HTTP_" + response.statusCode());
            }

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray choices = root.has("choices") && root.get("choices").isJsonArray()
                    ? root.getAsJsonArray("choices") : new JsonArray();
            if (choices.isEmpty()) return Result.fail("EMPTY_CHOICES");

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null || !message.has("content")) return Result.fail("EMPTY_CONTENT");
            String content = readContent(message.get("content")).trim();
            if (content.isBlank()) return Result.fail("EMPTY_CONTENT");
            return Result.ok(content);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return Result.fail("INTERRUPTED");
        } catch (Exception exception) {
            return Result.fail("CONNECTION_ERROR");
        }
    }

    private String readContent(JsonElement element) {
        if (element == null || element.isJsonNull()) return "";
        if (element.isJsonPrimitive()) return element.getAsString();
        if (element.isJsonArray()) {
            StringBuilder text = new StringBuilder();
            for (JsonElement part : element.getAsJsonArray()) {
                if (part.isJsonPrimitive()) {
                    text.append(part.getAsString());
                } else if (part.isJsonObject() && part.getAsJsonObject().has("text")) {
                    text.append(part.getAsJsonObject().get("text").getAsString());
                }
            }
            return text.toString();
        }
        return "";
    }

    private String loadPrompt(String resourceName) {
        try (InputStream input = OpenRouterClient.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) return "Bạn là C&C Assistant, trợ lý chăm sóc khách hàng của Celine Closet.";
            StringBuilder result = new StringBuilder();
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                char[] buffer = new char[4096];
                int read;
                while ((read = reader.read(buffer)) >= 0) result.append(buffer, 0, read);
            }
            return result.toString();
        } catch (Exception ignored) {
            return "Bạn là C&C Assistant, trợ lý chăm sóc khách hàng của Celine Closet.";
        }
    }

    private String safe(String value) {
        if (value == null || "null".equals(value)) return "";
        String trimmed = value.trim();
        return trimmed.length() > 3000 ? trimmed.substring(0, 3000) : trimmed;
    }

    private double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
