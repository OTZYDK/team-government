import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;       // for writing raw bytes to use the HTTP server
import java.io.IOException; // to use the HTTP server: represents an IP address + port number pair — a specific listening point on the machine
import java.io.OutputStream;   // The Http server
import java.net.InetSocketAddress;  // the interface
import java.util.List; // Deals with the conversation of one browser request (one request + response)


public class Main {

    // Convert one CatalogItem to a JSON object string --------
    private static String itemToJson(CatalogItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(item.ID).append(",");
        sb.append("\"price\":").append(item.Price).append(",");
        sb.append("\"description\":\"").append(escapeJson(item.Description)).append("\",");
        sb.append("\"category\":\"").append(item.getCategory()).append("\",");

        // Add tags array to JSON
        sb.append("\"tags\":[");
        for (int i = 0; i < item.ItemTags.size(); i++) {
            sb.append("\"").append(escapeJson(item.ItemTags.get(i))).append("\"");
            if (i < item.ItemTags.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    // Escape quotes/backslashes so JSON stays valid
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Build a JSON array string from the whole library
    private static String libraryToJson(List<CatalogItem> library) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < library.size(); i++) {
            sb.append(itemToJson(library.get(i)));
            if (i < library.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    //MAIN: start server
    public static void main(String[] args) throws IOException {
        // 1. Load the CSV into memory
        List<CatalogItem> itemLibrary = CsvHandler.LoadLibrary("backend\\src\\data\\items.csv");
        System.out.println("Loaded " + itemLibrary.size() + " items from CSV.");

        // 2. Start the HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("Server running at http://localhost:8080");
        System.out.println("Endpoint: http://localhost:8080/api/items");

        // 3. Route /api/items
        server.createContext("/api/items", new ItemsHandler(itemLibrary));

        // 4. Start
        server.setExecutor(null);
        server.start();
    }

    // -------- HANDLER: serve the JSON --------
    static class ItemsHandler implements HttpHandler {
        private final List<CatalogItem> library;

        public ItemsHandler(List<CatalogItem> library) {
            this.library = library;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS so frontend served from a different port can fetch
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            // Handle preflight (browser sends OPTIONS before cross-origin GET sometimes)
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String json = libraryToJson(library);

            byte[] bytes = json.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
