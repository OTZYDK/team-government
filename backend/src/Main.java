import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;       // for writing raw bytes to use the HTTP server
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList; // to use the HTTP server: represents an IP address + port number pair — a specific listening point on the machine
import java.util.HashMap;
import java.util.List;   // The Http server
import java.util.Map;
import java.util.Scanner;  // the interface
/*
ITEM CLASS REQUIREMENTS:
ID -> Int
Price -> Double
Description -> String
Category(Traits) -> Separate Class Object

DATABASE REQUIREMENTS:
Save as csv file
holds all fields and items
is hardcoded / prewritten
*/

//COMMIT TEST
//9:47 AM


public class Main {
	
	//LOAD FILE - RETURNS AN ARRAY-LIST WITH ALL DATA
	public static ArrayList<String> LoadFile(String file)
	{
		//Create File Object from source file
	    File sourceFile = new File(file);
	    
	    //Create Empty Array list to hold data
	    ArrayList<String> returnData = new ArrayList<String>(); 
	    
	    //Try-Catch for Scanner
	    //If Scanner is successful
	    try (Scanner readFile = new Scanner(sourceFile)) 
	    {
	      while (readFile.hasNextLine())
	      {
	    	  String data = readFile.nextLine();
	    	  //System.out.println(data);
	    	  returnData.add(data);
	      }
	      readFile.close();
          System.out.println(file + "AHHHHHHH");
	      return returnData;
	      
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	      return null;
	    }
	}


	//BUILD A List<CatalogItem> from CSV File
    //Accepts String (File Location) returns List<CatalogItem>
    public static List<CatalogItem> LoadLibrary(String file) {
        List<CatalogItem> library = new ArrayList<>();
        ArrayList<String> lines = LoadFile(file);
        if (lines == null) return library;

        for (int i = 1; i < lines.size(); i++) {   // skip header row
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                library.add(new CatalogItem(line));
            }
        }
        return library;
    }

    //FRONTEND FUNCTIONS-METHODS
    // -------- NEW: convert one CatalogItem to a JSON object string --------
    private static String itemToJson(CatalogItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(item.ID).append(",");
        sb.append("\"price\":").append(item.Price).append(",");
        sb.append("\"description\":\"").append(escapeJson(item.Description)).append("\",");
        sb.append("\"category\":\"").append(item.getCategory()).append("\",");

        // tags array
        sb.append("\"tags\":[");
        for (int i = 0; i < item.ItemTags.size(); i++) {
            sb.append("\"").append(escapeJson(item.ItemTags.get(i))).append("\"");
            if (i < item.ItemTags.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    // -------- NEW: escape quotes/backslashes so JSON stays valid --------
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // -------- NEW: build a JSON array string from the whole library --------
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

    // -------- MAIN: start server --------
    public static void main(String[] args) throws IOException {
        // 1. Load the CSV into memory
        List<CatalogItem> itemLibrary = LoadLibrary("backend\\src\\data\\items.csv");
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
        // ---- Common headers ----
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        // ---- Handle preflight ----
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();

        if ("POST".equalsIgnoreCase(method)) {
            handlePost(exchange);
        } else {
            handleGet(exchange);
        }
    }

    // ---- GET: return the whole library as JSON ----
    private void handleGet(HttpExchange exchange) throws IOException {
        String json = libraryToJson(library);
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // ---- POST: parse form body, add item, save to CSV ----
private void handlePost(HttpExchange exchange) throws IOException {
    // 1. Read the raw body
    InputStream input = exchange.getRequestBody();
    String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
    System.out.println("POST body: " + body);

    // 2. Parse into key-value pairs
    Map<String, String> fields = parseFormBody(body);

    // 3. Extract expected fields
    String idStr       = fields.getOrDefault("id", "").trim();
    String description = fields.getOrDefault("description", "").trim();
    String category    = fields.getOrDefault("category", "ERROR").trim();
    String tagsRaw     = fields.getOrDefault("tags", "").trim();
    String priceStr    = fields.getOrDefault("price", "0").trim();


    // 4. Validate required fields
    if (idStr.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
        sendJson(exchange, 400, "{\"error\":\"Missing required fields\"}");
        return;
    }

    // 5. Parse numeric fields
    int id;
    double price;
    try {
        id = Integer.parseInt(idStr);
        price = Double.parseDouble(priceStr);
    } catch (NumberFormatException e) {
        sendJson(exchange, 400, "{\"error\":\"Invalid id or price\"}");
        return;
    }

    // About to check duplicates for id

    // 6. Check for duplicate ID
    for (CatalogItem item : library) {
        if (item.ID == id) {
            sendJson(exchange, 409, "{\"error\":\"An item with that ID already exists\"}");
            return;
        }
    }

    // About to write to CSV

    // 7. Build the new CatalogItem
    String[] tags = tagsRaw.isEmpty() ? new String[0] : tagsRaw.split("-");
    CatalogItem newItem = new CatalogItem(id, price, description, category, tags);

    // 8. Add to in-memory library
    library.add(newItem);

    // 9. Append to CSV file
    try (FileWriter fw = new FileWriter("backend\\src\\data\\items.csv", true);
         BufferedWriter bw = new BufferedWriter(fw);
         PrintWriter out = new PrintWriter(bw)) {
        out.println(newItem.DataToString());
        System.out.println("WROTE: " + newItem.DataToString());  //test
    } catch (IOException e) {
        System.out.println("WRITE FAILED: " + e.getMessage()); //test
        e.printStackTrace();
        sendJson(exchange, 500, "{\"error\":\"Could not write to CSV\"}");
        return;
    }

    

    // 10. Success response
    sendJson(exchange, 201, "{\"status\":\"created\",\"id\":" + id + "}");
}

    // ---- Parse "key1=value1&key2=value2" into a map ----
    private Map<String, String> parseFormBody(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;

        for (String pair : body.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) continue;
            String key = pair.substring(0, eq);
            String value = pair.substring(eq + 1);
            try {
                // URL-decode handles "+" → space and "%20" → space
                key = URLDecoder.decode(key, "UTF-8");
                value = URLDecoder.decode(value, "UTF-8");
            } catch (Exception ignored) {}
            map.put(key, value);
        }
        return map;
    }

    // ---- Helper: send a JSON response with a status code ----
    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}



}
