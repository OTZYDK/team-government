import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvHandler {

    public static ArrayList<String> LoadFile(String file) {
        File sourceFile = new File(file);
        ArrayList<String> returnData = new ArrayList<>();

        try (Scanner readFile = new Scanner(sourceFile)) {
            while (readFile.hasNextLine()) {
                String data = readFile.nextLine();
                returnData.add(data);
            }

            return returnData;

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

    public static List<CatalogItem> LoadLibrary(String file) {
        List<CatalogItem> library = new ArrayList<>();
        ArrayList<String> lines = LoadFile(file);

        if (lines == null) {
            return library;
        }

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (!line.isEmpty()) {
                library.add(new CatalogItem(line));
            }
        }

        return library;
    }
}