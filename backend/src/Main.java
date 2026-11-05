import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    
    private static final String CSV_FILE = "app/items.csv";
    private static final List<CatalogItem> library =
            new ArrayList<>();

    public static void main(String[] args) throws Exception {

        //ItemCatalog.init();

        //library.addAll(ItemCatalog.EnterQuery(Search.getAll()));
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

        // Serve the static frontend (HTML, CSS, JS, images) from app/frontend/.
        // This lets the user open http://127.0.0.1:8080/ and see the storefront
        // instead of raw JSON, and avoids CORS issues because everything is
        // same-origin.
        server.createContext("/", exchange -> {
        String path = exchange.getRequestURI().getPath();

        // Root URL should serve the homepage.
        if (path.equals("/") || path.equals("/index.html")) {
                path = "/index.html";
        }

        // Security: block "../" so a crafted URL can't escape app/frontend/
        // and read arbitrary files (like the app's JAR).
        if (path.contains("..")) {
                exchange.sendResponseHeaders(400, -1);
                return;
        }

        // Only serve file types the frontend needs. This blocks access to
        // .jar, .db, .exe, and anything else not meant for the browser.
        String lower = path.toLowerCase();
        boolean allowed = lower.endsWith(".html") || lower.endsWith(".css")
                        || lower.endsWith(".js")   || lower.endsWith(".png")
                        || lower.endsWith(".jpg")  || lower.endsWith(".jpeg")
                        || lower.endsWith(".gif")  || lower.endsWith(".svg")
                        || lower.endsWith(".ico");
        if (!allowed) {
                exchange.sendResponseHeaders(403, -1);
                return;
        }

        // Resolve the file from the installed app's frontend folder.
        // jpackage copies --input contents into "app/", so frontend/ ends
        // up at C:\Program Files\DallasCollegeBookstore\app\frontend\.
        java.io.File file = new java.io.File("app/frontend" + path);
        if (!file.exists() || !file.isFile()) {
                exchange.sendResponseHeaders(404, -1);
                return;
        }

        // Browsers need the correct Content-Type to render files properly.
        // Without charset=UTF-8, non-ASCII characters can garble.
        String contentType;
        if (lower.endsWith(".html"))      contentType = "text/html; charset=UTF-8";
        else if (lower.endsWith(".css"))  contentType = "text/css; charset=UTF-8";
        else if (lower.endsWith(".js"))   contentType = "application/javascript; charset=UTF-8";
        else if (lower.endsWith(".png"))  contentType = "image/png";
        else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) contentType = "image/jpeg";
        else if (lower.endsWith(".gif"))  contentType = "image/gif";
        else if (lower.endsWith(".svg"))  contentType = "image/svg+xml";
        else                               contentType = "image/x-icon";

        byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
        }
        });


        server.setExecutor(null);
        server.start();
        System.out.println();
        System.out.println("========================================");
        System.out.println("  Bookstore running at http://127.0.0.1:8080/");
        System.out.println("  Do NOT close this window while using the app.");
        System.out.println("========================================");
        System.out.println();

        Thread.sleep(500);

        try {
        java.awt.Desktop.getDesktop().browse(new java.net.URI("http://127.0.0.1:8080/"));
        } catch (Exception e) {
        System.out.println("Open http://127.0.0.1:8080/ in your browser.");
        }

        System.out.println(
                "Server running at http://localhost:8080"
        );

        System.out.println(
                "Endpoint: http://localhost:8080/api/items"
        );
    }


    /**
 * Serves the frontend files (HTML, CSS, JS, images) that are bundled
 * with the installer.
 *
 * Why this exists:
 *   Without this handler, the user would have to manually find and open
 *   frontend/index.html in their browser, which triggers file:// CORS
 *   issues and defeats the purpose of a "double-click to run" installer.
 *
 *   With this handler, everything is served from the same origin
 *   (http://127.0.0.1:8080), so the browser sees the storefront at "/"
 *   and the API at "/api/items" with no cross-origin restrictions.
 *
 * Where the files come from:
 *   jpackage copies everything in --input into the install folder's
 *   "app/" subdirectory. So frontend/index.html on disk becomes
 *   "app/frontend/index.html" relative to the app's working directory.
 */

    static class StaticFileHandler implements HttpHandler {

    // Allowed file extensions (blocks directory traversal and unwanted types)
    private static final String[] ALLOWED_EXTENSIONS = {
        ".html", ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico"
    };

    @Override
public void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();

    // Root URL ("/") should serve the homepage, not 404.
    if (path.equals("/") || path.equals("/index.html")) {
        path = "/index.html";
    }

    // Security: block "../" so a crafted URL can't escape app/frontend/
    // and read arbitrary files from the filesystem.
    if (path.contains("..")) {
        exchange.sendResponseHeaders(400, -1);
        return;
    }

    // Only serve file types the frontend actually needs. Anything else
    // (e.g. .jar, .db, .exe) is blocked so a curious user can't download
    // the app's own binary files by guessing URLs.
    boolean allowed = false;
    for (String ext : ALLOWED_EXTENSIONS) {
        if (path.toLowerCase().endsWith(ext)) {
            allowed = true;
            break;
        }
    }
    if (!allowed) {
        exchange.sendResponseHeaders(403, -1);
        return;
    }

    // Resolve the file relative to the install folder's app/ directory.
    // "user.dir" is where the .exe was launched from, which for a
    // jpackage install is C:\Program Files\DallasCollegeBookstore\.
    File file = new File("app/frontend" + path);
    if (!file.exists() || !file.isFile()) {
        exchange.sendResponseHeaders(404, -1);
        return;
    }

    // Browsers need the correct Content-Type to render HTML/CSS/JS
    // properly. If we send everything as text/plain, the page shows
    // raw source code instead of rendering.
    String contentType;
    String lower = path.toLowerCase();
    if (lower.endsWith(".html"))      contentType = "text/html; charset=UTF-8";
    else if (lower.endsWith(".css"))  contentType = "text/css; charset=UTF-8";
    else if (lower.endsWith(".js"))   contentType = "application/javascript; charset=UTF-8";
    else if (lower.endsWith(".png"))  contentType = "image/png";
    else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) contentType = "image/jpeg";
    else if (lower.endsWith(".gif"))  contentType = "image/gif";
    else if (lower.endsWith(".svg"))  contentType = "image/svg+xml";
    else if (lower.endsWith(".ico"))  contentType = "image/x-icon";
    else                               contentType = "application/octet-stream";

    byte[] content = java.nio.file.Files.readAllBytes(file.toPath());

    exchange.getResponseHeaders().set("Content-Type", contentType);
    exchange.sendResponseHeaders(200, content.length);

    try (OutputStream os = exchange.getResponseBody()) {
        os.write(content);
    }
}
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