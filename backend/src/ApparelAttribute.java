
public class ApparelAttribute {

    private int apparelID;
    private int itemID;
    private String size;
    private String color;
    private int supply;

    public ApparelAttribute(
            int apparelID,
            int itemID,
            String size,
            String color,
            int supply) {

        this.apparelID = apparelID;
        this.itemID = itemID;
        this.size = size;
        this.color = color;
        this.supply = supply;
    }

    public ApparelAttribute(String csvLine) {

        String[] data = csvLine.split(",", -1);

        this.apparelID = Integer.parseInt(data[0]);
        this.itemID = Integer.parseInt(data[1]);
        this.size = data[2];
        this.color = data[3];
        this.supply = Integer.parseInt(data[4]);
    }

    public int getApparelID() {
        return apparelID;
    }

    public int getItemID() {
        return itemID;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        if (supply < 0) {
            throw new IllegalArgumentException(
                    "Supply cannot be negative."
            );
        }

        this.supply = supply;
    }
    public boolean reduceSupply(int quantity) {
    if (quantity <= 0) {
        return false;
    }

    if (supply < quantity) {
        return false;
    }

    supply -= quantity;
    return true;
    }

    public String DataToString() {

        return apparelID + ","
                + itemID + ","
                + size + ","
                + color + ","
                + supply;
    }
    
}

