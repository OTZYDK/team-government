
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
            );
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "POST, DELETE, OPTIONS"
            );
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization"
            );

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(method)
                    && "/api/login".equals(path)) {
                handleLogin(exchange);
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)
                    && "/api/logout".equals(path)) {
                ManagerAuth.logout(ManagerAuth.getToken(exchange));
                sendJson(exchange, 200,
                    "{\"message\":\"Logged out.\"}");
                return;
            }

            sendJson(exchange, 405,
                "{\"error\":\"Method not allowed.\"}");

        } finally {
            exchange.close();
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body;

        try (InputStream input = exchange.getRequestBody()) {
            byte[] bytes = input.readNBytes(100_001);

            if (bytes.length > 100_000) {
                sendJson(exchange, 400,
                    "{\"error\":\"Request is too large.\"}");
                return;
            }

            body = new String(bytes, StandardCharsets.UTF_8);
        }

        String username = readString(body, "username");
        String password = readString(body, "password");

        if (username == null || password == null) {
            sendJson(exchange, 400,
                "{\"error\":\"Username and password are required.\"}");
            return;
        }

        String token = ManagerAuth.login(username, password);

        if (token == null) {
            sendJson(exchange, 401,
                "{\"error\":\"Invalid username or password.\"}");
            return;
        }

        String role = ManagerAuth.getRole(token);

        sendJson(exchange, 200,
            "{\"token\":\"" + token + "\","
            + "\"role\":\"" + role + "\","
            + "\"username\":\"" + username + "\"}");
    }

    private static String readString(String json, String field) {
        Pattern pattern = Pattern.compile(
            "\"" + Pattern.quote(field)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

    private static void sendJson(
            HttpExchange exchange, int status, String json)
            throws IOException {

        byte[] response = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, response.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
