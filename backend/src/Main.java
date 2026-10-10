import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String CSV_FILE = "backend/src/data/items.csv";
    private static final List<CatalogItem> library =
            new ArrayList<>();

    public static void main(String[] args) throws Exception {

        library.addAll(CsvHandler.LoadLibrary(CSV_FILE));

        System.out.println(
                "Loaded " + library.size() + " items from CSV."
        );

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(8080),
                        0
                );

        server.createContext(
                "/api/items",
                new ItemsHandler()
        );

        server.setExecutor(null);
        server.start();

        System.out.println(
                "Server running at http://localhost:8080"
        );

        System.out.println(
                "Endpoint: http://localhost:8080/api/items"
        );
    }

    static class ItemsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange)
                throws IOException {

            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Origin",
                    "*"
            );

            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS"
            );

            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Headers",
                    "Content-Type"
            );

            if ("OPTIONS".equalsIgnoreCase(
                    exchange.getRequestMethod())) {

                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String method =
                    exchange.getRequestMethod();

            String path =
                    exchange.getRequestURI().getPath();

            try {

                // GET /api/items
                if (method.equals("GET")
                        && path.equals("/api/items")) {

                    sendResponse(
                            exchange,
                            200,
                            libraryToJson(library)
                    );

                    return;
                }

                // POST /api/items
                if (method.equals("POST")
                        && path.equals("/api/items")) {

                    String body =
                            readRequestBody(exchange);

                    CatalogItem newItem =
                            createItemFromJson(body);

                    if (newItem == null) {

                        sendResponse(
                                exchange,
                                400,
                                "{\"error\":\"Invalid item data\"}"
                        );

                        return;
                    }

                    for (CatalogItem item : library) {

                        if (item.ID == newItem.ID) {

                            sendResponse(
                                    exchange,
                                    409,
                                    "{\"error\":\"Item ID already exists\"}"
                            );

                            return;
                        }
                    }

                    library.add(newItem);

                    CsvHandler.SaveLibrary(
                            CSV_FILE,
                            library
                    );

                    sendResponse(
                            exchange,
                            201,
                            itemToJson(newItem)
                    );

                    return;
                }

                // PUT /api/items/{id}
                if (method.equals("PUT")
                        && path.startsWith("/api/items/")) {

                    String idText =
                            path.substring(
                                    "/api/items/".length()
                            );

                    int id;

                    try {
                        id = Integer.parseInt(idText);
                    } catch (NumberFormatException e) {

                        sendResponse(
                                exchange,
                                400,
                                "{\"error\":\"Invalid item ID\"}"
                        );

                        return;
                    }

                    String body =
                            readRequestBody(exchange);

                    CatalogItem updatedItem =
                            createItemFromJson(body);

                    if (updatedItem == null) {

                        sendResponse(
                                exchange,
                                400,
                                "{\"error\":\"Invalid item data\"}"
                        );

                        return;
                    }

                    for (int i = 0;
                         i < library.size();
                         i++) {

                        if (library.get(i).ID == id) {

                            library.set(i, updatedItem);

                            CsvHandler.SaveLibrary(
                                    CSV_FILE,
                                    library
                            );

                            sendResponse(
                                    exchange,
                                    200,
                                    itemToJson(updatedItem)
                            );

                            return;
                        }
                    }

                    sendResponse(
                            exchange,
                            404,
                            "{\"error\":\"Item not found\"}"
                    );

                    return;
                }

                // DELETE /api/items/{id}
                if (method.equals("DELETE")
                        && path.startsWith("/api/items/")) {

                    String idText =
                            path.substring(
                                    "/api/items/".length()
                            );

                    int id;

                    try {
                        id = Integer.parseInt(idText);
                    } catch (NumberFormatException e) {

                        sendResponse(
                                exchange,
                                400,
                                "{\"error\":\"Invalid item ID\"}"
                        );

                        return;
                    }

                    for (int i = 0;
                         i < library.size();
                         i++) {

                        if (library.get(i).ID == id) {

                            CatalogItem removed =
                                    library.remove(i);

                            CsvHandler.SaveLibrary(
                                    CSV_FILE,
                                    library
                            );

                            sendResponse(
                                    exchange,
                                    200,
                                    itemToJson(removed)
                            );

                            return;
                        }
                    }

                    sendResponse(
                            exchange,
                            404,
                            "{\"error\":\"Item not found\"}"
                    );

                    return;
                }

                sendResponse(
                        exchange,
                        404,
                        "{\"error\":\"Endpoint not found\"}"
                );

            } catch (Exception e) {

                e.printStackTrace();

                sendResponse(
                        exchange,
                        500,
                        "{\"error\":\"Server error\"}"
                );
            }
        }
    }

    private static String readRequestBody(
            HttpExchange exchange) throws IOException {

        InputStream input =
                exchange.getRequestBody();

        return new String(
                input.readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    private static CatalogItem createItemFromJson(
            String json) {

        try {

            int id =
                    (int) getJsonNumber(json, "id");

            double price =
                    getJsonNumber(json, "price");

            String description =
                    getJsonString(json, "description");

            String category =
                    getJsonString(json, "category");

            String[] tags =
                    getJsonTags(json);

            return new CatalogItem(
                    id,
                    price,
                    description,
                    category,
                    tags
            );

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    private static String getJsonString(
            String json,
            String key) {

        String search =
                "\"" + key + "\":";

        int start =
                json.indexOf(search);

        if (start == -1) {
            throw new IllegalArgumentException();
        }

        start += search.length();

        while (start < json.length()
                && Character.isWhitespace(
                        json.charAt(start))) {

            start++;
        }

        if (json.charAt(start) == '"') {
            start++;
        }

        int end =
                json.indexOf('"', start);

        return json.substring(start, end);
    }

    private static double getJsonNumber(
            String json,
            String key) {

        String search =
                "\"" + key + "\":";

        int start =
                json.indexOf(search);

        if (start == -1) {
            throw new IllegalArgumentException();
        }

        start += search.length();

        while (start < json.length()
                && Character.isWhitespace(
                        json.charAt(start))) {

            start++;
        }

        int end = start;

        while (end < json.length()
                && "0123456789.-"
                .indexOf(json.charAt(end)) >= 0) {

            end++;
        }

        return Double.parseDouble(
                json.substring(start, end)
        );
    }

    private static String[] getJsonTags(
            String json) {

        String search = "\"tags\":[";

        int start =
                json.indexOf(search);

        if (start == -1) {
            return new String[0];
        }

        start += search.length();

        int end =
                json.indexOf("]", start);

        String tagsText =
                json.substring(start, end)
                        .replace("\"", "");

        if (tagsText.trim().isEmpty()) {
            return new String[0];
        }

        String[] tags =
                tagsText.split(",");

        for (int i = 0;
             i < tags.length;
             i++) {

            tags[i] = tags[i].trim();
        }

        return tags;
    }

    private static String itemToJson(
            CatalogItem item) {

        StringBuilder json =
                new StringBuilder();

        json.append("{");
        json.append("\"id\":").append(item.ID).append(",");
        json.append("\"price\":").append(item.Price).append(",");
        json.append("\"description\":\"")
                .append(item.Description)
                .append("\",");
        json.append("\"category\":\"")
                .append(item.Category)
                .append("\",");
        json.append("\"tags\":[");

        for (int i = 0;
             i < item.ItemTags.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append("\"")
                    .append(item.ItemTags.get(i))
                    .append("\"");
        }

        json.append("]}");

        return json.toString();
    }

    private static String libraryToJson(
            List<CatalogItem> items) {

        StringBuilder json =
                new StringBuilder();

        json.append("[");

        for (int i = 0;
             i < items.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append(
                    itemToJson(items.get(i))
            );
        }

        json.append("]");

        return json.toString();
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response)
            throws IOException {

        byte[] bytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(
                statusCode,
                bytes.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(bytes);
        }
    }
}