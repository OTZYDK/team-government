import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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

        List<CatalogItem> library =
                new ArrayList<>();

        ArrayList<String> lines =
                LoadFile(file);

        if (lines == null) {
            return library;
        }

        for (int i = 1;
             i < lines.size();
             i++) {

            String line =
                    lines.get(i).trim();

            if (!line.isEmpty()) {

                library.add(
                        new CatalogItem(line)
                );
            }
        }

        return library;
    }

    public static void SaveLibrary(
            String file,
            List<CatalogItem> library) {

        try {

            ArrayList<String> existingLines =
                    LoadFile(file);

            String header =
                    "ID,Price,Description,Category,Tags";

            if (existingLines != null
                    && !existingLines.isEmpty()) {

                header =
                        existingLines.get(0);
            }

            try (FileWriter writer =
                         new FileWriter(file)) {

                writer.write(header);
                writer.write(
                        System.lineSeparator()
                );

                for (CatalogItem item : library) {

                    writer.write(
                            item.DataToString()
                    );

                    writer.write(
                            System.lineSeparator()
                    );
                }
            }

            System.out.println(
                    "Catalog saved successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Error saving catalog."
            );

            e.printStackTrace();
        }
    }

    public static List<ApparelAttribute> LoadApparelAttributes(String file) {

        List<ApparelAttribute> attributes = new ArrayList<>();

        ArrayList<String> lines = LoadFile(file);

        if (lines == null) {
            return attributes;
        }

        for (int i = 1; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            if (!line.isEmpty()) {
                attributes.add(new ApparelAttribute(line));
            }
        }

        return attributes;
    }

    public static void SaveApparelAttributes(
            String file,
            List<ApparelAttribute> attributes) {

        try (FileWriter writer = new FileWriter(file)) {

            writer.write("apparelID,ItemID,Size,Color,Supply");
            writer.write(System.lineSeparator());

            for (ApparelAttribute attribute : attributes) {

                writer.write(attribute.DataToString());
                writer.write(System.lineSeparator());
            }

            System.out.println("Apparel attributes saved successfully.");

        } catch (IOException e) {

            System.out.println("Error saving apparel attributes.");
            e.printStackTrace();
        }
    }
}