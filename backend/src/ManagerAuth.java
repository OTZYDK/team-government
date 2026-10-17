
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManagerAuth {

    private static final Map<String, String> sessions =
        new ConcurrentHashMap<>();

    public static String login(String username, String password) {
        String role = null;

        if ("student".equals(username)
                && "learningRules".equals(password)) {
            role = "student";
        } else if ("manager".equals(username)
                && "storeOverlord".equals(password)) {
            role = "manager";
        }

        if (role == null) {
            return null;
        }

        String token = UUID.randomUUID().toString();
        sessions.put(token, role);
        return token;
    }

    public static String getRole(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        return sessions.get(token);
    }

    public static String getToken(HttpExchange exchange) {
        String authorization = exchange.getRequestHeaders()
            .getFirst("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7).trim();
    }

    public static void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
