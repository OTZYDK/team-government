
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        ItemCatalog.init();

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/logout", new LoginHandler());
        server.createContext("/api/items", new ItemsHandler());

        server.setExecutor(null);
        server.start();

        System.out.println(
            "Bookstore server running at http://localhost:8080"
        );
        System.out.println("Login API: http://localhost:8080/api/login");
        System.out.println("Catalog API: http://localhost:8080/api/items");
    }
}
