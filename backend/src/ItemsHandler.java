
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin", "*"
            );
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
            );
            exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type, Authorization"
            );


            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            
        String token = ManagerAuth.getToken(exchange);
        String role = ManagerAuth.getRole(token);

        if (role == null) {
            sendError(exchange, 401,
                "Please log in to access the catalog.");
            return;
        }

        if (("POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method))
                && !"manager".equals(role)) {

                sendError(exchange, 403,
                    "Manager permissions are required for this action.");
                return;
            }

            
            boolean collectionPath =
                path.equals("/api/items")
                || path.equals("/api/items/");

            Integer itemId = null;

            if (!collectionPath) {
                Matcher matcher = Pattern.compile(
                    "^/api/items/([1-9][0-9]*)/?$"
                ).matcher(path);

                if (!matcher.matches()) {
                    sendError(exchange, 404, "Invalid item URL.");
                    return;
                }

                try {
                    itemId = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    sendError(exchange, 400, "Invalid item ID.");
                    return;
                }
            }

            switch (method.toUpperCase(Locale.ROOT)) {
                case "GET":
                    if (!collectionPath) {
                        sendError(
                            exchange, 405,
                            "Use GET /api/items to load the catalog."
                        );
                        return;
                    }

                    List<CatalogItem> items =
                        ItemCatalog.EnterQuery(
                            "SELECT * FROM Items ORDER BY ID"
                        );

                    sendJson(exchange, 200, libraryToJson(items));
                    break;

                case "POST":
                    if (!collectionPath) {
                        sendError(
                            exchange, 400,
                            "Use POST /api/items to add an item."
                        );
                        return;
                    }

                    handleAdd(exchange);
                    break;

                case "PUT":
                    if (itemId == null) {
                        sendError(
                            exchange, 400,
                            "An item ID is required for updates."
                        );
                        return;
                    }

                    handleUpdate(exchange, itemId);
                    break;

                case "DELETE":
                    if (itemId == null) {
                        sendError(
                            exchange, 400,
                            "An item ID is required for deletion."
                        );
                        return;
                    }

                    handleDelete(exchange, itemId);
                    break;

                default:
                    exchange.getResponseHeaders().set(
                        "Allow",
                        "GET, POST, PUT, DELETE, OPTIONS"
                    );
                    sendError(
                        exchange, 405, "Method not allowed."
                    );
            }

        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();

            if (exchange.getResponseCode() == -1) {
                sendError(
                    exchange, 500,
                    "The server could not complete the request."
                );
            }
        } finally {
            exchange.close();
        }
    }

    // Add a new item to SQLite.
    private static void handleAdd(
            HttpExchange exchange) throws Exception {

        String body = readBody(exchange);
        validateBody(body);

        double price = readPrice(body);
        String description = readString(body, "description");
        String category = readCategory(body);
        String tags = readTags(body);

        CatalogItem item = ItemCatalog.addItem(
            price, description, category, tags
        );

        sendJson(exchange, 201, itemToJson(item));
    }

    // Update an existing item in SQLite.
    private static void handleUpdate(
            HttpExchange exchange, int id) throws Exception {

        String body = readBody(exchange);
        validateBody(body);

        double price = readPrice(body);
        String description = readString(body, "description");
        String category = readCategory(body);
        String tags = readTags(body);

        CatalogItem item = ItemCatalog.updateItem(
            id, price, description, category, tags
        );

        if (item == null) {
            sendError(
                exchange, 404,
                "No catalog item exists with ID " + id + "."
            );
            return;
        }

        sendJson(exchange, 200, itemToJson(item));
    }

    // Delete an item from SQLite.
    private static void handleDelete(
            HttpExchange exchange, int id) throws Exception {

        CatalogItem item = ItemCatalog.deleteItem(id);

        if (item == null) {
            sendError(
                exchange, 404,
                "No catalog item exists with ID " + id + "."
            );
            return;
        }

        sendJson(exchange, 200, itemToJson(item));
    }

    // Read the incoming request body.
    private static String readBody(
            HttpExchange exchange) throws IOException {

        try (InputStream input = exchange.getRequestBody()) {
            byte[] bytes = input.readNBytes(1_000_001);

            if (bytes.length > 1_000_000) {
                throw new IllegalArgumentException(
                    "Request body is too large."
                );
            }

            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static void validateBody(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException(
                "A JSON request body is required."
            );
        }

        if (!body.trim().startsWith("{")
                || !body.trim().endsWith("}")) {
            throw new IllegalArgumentException(
                "The request body must be a JSON object."
            );
        }
    }

    private static String readString(
            String json, String field) {

        String value = readJsonString(json, field);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                "The field '" + field + "' is required."
            );
        }

        return value.trim();
    }

    private static double readPrice(String json) {
        Matcher matcher = Pattern.compile(
            "\"price\"\\s*:\\s*(-?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?)"
        ).matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                "A valid price is required."
            );
        }

        try {
            double price = Double.parseDouble(matcher.group(1));

            if (!Double.isFinite(price) || price < 0) {
                throw new IllegalArgumentException(
                    "Price must be a non-negative number."
                );
            }

            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "A valid price is required."
            );
        }
    }

    private static String readCategory(String json) {
        String category = readString(json, "category")
            .toUpperCase(Locale.ROOT);

        switch (category) {
            case "BOOK":
            case "DEVICE":
            case "TOOL":
            case "SNACK":
            case "DRINK":
            case "STATIONERY":
            case "APPAREL":
                return category;

            default:
                throw new IllegalArgumentException(
                    "Unsupported category: " + category
                );
        }
    }

    // Convert the frontend tags array into SQLite's hyphen-separated text.
    private static String readTags(String json) {
        Matcher matcher = Pattern.compile(
            "\"tags\"\\s*:\\s*\\[([^\\]]*)\\]"
        ).matcher(json);

        if (!matcher.find()) {
            return "";
        }

        String contents = matcher.group(1);
        Matcher tagMatcher = Pattern.compile(
            "\"((?:\\\\.|[^\"\\\\])*)\""
        ).matcher(contents);

        List<String> tags = new ArrayList<>();

        while (tagMatcher.find()) {
            String tag = unescapeJson(tagMatcher.group(1)).trim();

            if (!tag.isEmpty()) {
                if (tag.contains("-")) {
                    throw new IllegalArgumentException(
                        "Tags cannot contain hyphens."
                    );
                }

                tags.add(tag);
            }
        }

        return String.join("-", tags);
    }

    // Read a JSON string property.
    private static String readJsonString(
            String json, String field) {

        Pattern pattern = Pattern.compile(
            "\"" + Pattern.quote(field)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return null;
        }

        return unescapeJson(matcher.group(1));
    }

    // Decode common JSON escape sequences.
    private static String unescapeJson(String value) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (c != '\\') {
                result.append(c);
                continue;
            }

            if (++i >= value.length()) {
                throw new IllegalArgumentException(
                    "Invalid JSON escape sequence."
                );
            }

            char escaped = value.charAt(i);

            switch (escaped) {
                case '"': result.append('"'); break;
                case '\\': result.append('\\'); break;
                case '/': result.append('/'); break;
                case 'b': result.append('\b'); break;
                case 'f': result.append('\f'); break;
                case 'n': result.append('\n'); break;
                case 'r': result.append('\r'); break;
                case 't': result.append('\t'); break;

                case 'u':
                    if (i + 4 >= value.length()) {
                        throw new IllegalArgumentException(
                            "Invalid Unicode escape sequence."
                        );
                    }

                    try {
                        int code = Integer.parseInt(
                            value.substring(i + 1, i + 5), 16
                        );
                        result.append((char) code);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                            "Invalid Unicode escape sequence."
                        );
                    }

                    i += 4;
                    break;

                default:
                    throw new IllegalArgumentException(
                        "Invalid JSON escape sequence."
                    );
            }
        }

        return result.toString();
    }

    // Serialize one item for the frontend.
    private static String itemToJson(CatalogItem item) {
        return "{"
            + "\"id\":" + item.ID + ","
            + "\"price\":" + item.Price + ","
            + "\"description\":\""
            + escapeJson(item.Description) + "\","
            + "\"category\":\""
            + escapeJson(item.getCategory()) + "\","
            + "\"tags\":" + tagsToJson(item)
            + "}";
    }

    private static String libraryToJson(List<CatalogItem> items) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append(itemToJson(items.get(i)));
        }

        return json.append("]").toString();
    }

    private static String tagsToJson(CatalogItem item) {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < item.ItemTags.size(); i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append("\"")
                .append(escapeJson(item.ItemTags.get(i)))
                .append("\"");
        }

        return json.append("]").toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();

        for (char c : value.toCharArray()) {
            switch (c) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;

                default:
                    if (c < 0x20) {
                        escaped.append(
                            String.format("\\u%04x", (int) c)
                        );
                    } else {
                        escaped.append(c);
                    }
            }
        }

        return escaped.toString();
    }

    private static void sendError(
            HttpExchange exchange,
            int status,
            String message) throws IOException {

        sendJson(
            exchange,
            status,
            "{\"error\":\"" + escapeJson(message) + "\"}"
        );
    }

    private static void sendJson(
            HttpExchange exchange,
            int status,
            String json) throws IOException {

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
