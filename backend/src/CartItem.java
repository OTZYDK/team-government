/*THE CART ITEM CLASS

The CartItem class has three constructors, one that takes only an item argument

One Makes a plain cart item with no discounts applied
the other two accept a Sale object or list of sales as an argument
Sales objects hold discounts and their conditions, and are read in from a csv file/sql database

This allows each cart item to keep track of the sales it has applied to it
and calculate a final discount from that



*/
import java.util.ArrayList;

public class CartItem{

	
	//Default Attributes
	private int unit_amount = 1;
	
	//Cart Item Discount info
	private Discount ItemDiscount = new Discount();
	
	//Constructed Attributes
	final CatalogItem item;
	final double unit_price;
	
	//List of All Sales applied
	private ArrayList<Sale> ItemSales = new ArrayList<Sale>(); 
	
	//Calculated Attributes
	private double adjusted_price;
	
	//------------CONSTRUCTORS
	//PRIMARY CONSTRUCTOR
	public CartItem(CatalogItem item) {
		this.item = item;
		unit_price = item.Price;
		
		//uses default sales data for price
		ItemDiscount = new Discount();
		updateDiscountData();
	}
	
	//Passing A single sale object
	public CartItem(CatalogItem item, Sale sale) {
		this.item = item;
		unit_price = item.Price;
		
		//Calculate Price(s)
		addSale(sale);
		updateDiscountData();
	}
		
	//passing many sales objects
	//CONSTRUCTOR WITH OPTIONAL PARAMTERS: discount_rate, tax_exempt, unit_amount, USE "" WHEN NO ARG
    public CartItem(CatalogItem item, ArrayList<Sale> args) {
    	//SET CATALOG ITEM
		this.item = item;
		unit_price = item.Price;
		
		//add all sales provided, checking them with addSale
		for (Sale i: args) {addSale(i);}
    	
    	//add to list of discounts
		updateDiscountData();
    }

	
	//------------INTERNAL SETTERS
	private void updateDiscountData() 
	{
		Boolean tax = false;
		Double rate = 0.0;
		Double dollar = 0.0;
		
		if (ItemSales.isEmpty())
		{
			setAdjustedPrice();
		}
		else
		{
			//For each sale applied to this item
			for (Sale v : ItemSales)
			{
				//pull discount from sale
				Discount i = v.getDiscount();
				
				//If a discount applies tax exemption, keep it
				if (i.TaxExempt) {tax = true;}
				
				//Combine Discounts
				//If the discount is less then 100%
				if (rate + i.DiscountRate < 100) {rate += i.DiscountRate;}
				
				//If the dollar discount is less than the price with the discount applied
				if (dollar + i.DiscountDollar < (unit_price - ((unit_price / 100) * rate))) {dollar += i.DiscountDollar;}
				
			}
			
			ItemDiscount = new Discount(tax, rate, dollar);
			
			//Update Price based on discount data
			setAdjustedPrice();
		}
	}
	
	private void setAdjustedPrice() 
	{
		adjusted_price = ItemDiscount.CalcPrice(unit_price);
	}
	
	private void addSale(Sale arg) 
	{
		if (arg.getValid()) {ItemSales.add(arg);}
		
		updateDiscountData();
		
	}
	
	//------------SETTERS
	//SET AMOUNT
	public void setAmount(int unit_amount) {
		//If new amount is above 0, else do not update
		if (unit_amount >= 1) {this.unit_amount = unit_amount;}
		setAdjustedPrice();
	}
	
	//INCREMENT AMOUNT BY ARGUMENT
	public void incrementAmount (int increment) {
		if ((unit_amount += increment) >= 1) {unit_amount += increment;};
		setAdjustedPrice();
	}
	
	
	//------------GETTERS
	public double getDiscountRate() {return ItemDiscount.DiscountRate;}
	public Discount getDiscount() {return ItemDiscount;}
	public double getDiscountDollar() {return ItemDiscount.DiscountDollar;}
	public double getTaxRate() {return ItemDiscount.taxRate;}
	public int getUnitAmount() {return unit_amount;}
	public boolean getTaxExempt() {return ItemDiscount.TaxExempt;}
	public double getAdjustedPrice() {return adjusted_price;}
	public ArrayList<Sale> getSales() {return ItemSales;}
	public ArrayList<String> getTab() 
	{
		
		//returns list of strings to print in reciet
		
		ArrayList<String> build = new ArrayList<String>(); 
		
		//-----FIELDS
		//Amount
		build.add(Integer.toString(unit_amount));
		
		//Description
		build.add(item.Description);
		
		//Price
		build.add(Double.toString(util.priceFormat(unit_price * unit_amount)));
		
		
		if (ItemDiscount.isValid())
		{
			if (ItemDiscount.DiscountRate > 0)
			{build.add(Double.toString(ItemDiscount.DiscountRate) + "%");}
			else {build.add("");}
			
			if (ItemDiscount.DiscountDollar > 0)
			{build.add(Double.toString(util.priceFormat(ItemDiscount.DiscountDollar))+ "$");}
			else {build.add("");}
		}
		
		//optional -
		//% off
		//dollar off
		
		//build = unit_amount + " " + item.Description + ": " + unit_price + "$";
		
		
		return build;
	}
	
	
	public double CalcPrice() 
	{return ItemDiscount.CalcPrice(unit_price);}
	
	public double getTaxAmount() 
	{
		return ItemDiscount.calcTaxAmount(unit_price);
	}
	
	public double getPercentOff() 
	{
		return ItemDiscount.calcPercentOff(unit_price);
	}
	
	public double getDollarOff() 
	{
		return ItemDiscount.getDollarOff(unit_price);
	}
	
	
	//------------PRINTERS
	public void printFields() {
		System.out.println("\nALL ITEM DATA:");
		System.out.println("ID: " + item.ID);
		System.out.println("DESCRIPTION: " + item.Description);
		System.out.println("CATEGORY: " + item.Category);
		System.out.println("TAGS: ");
		item.printTags();	
		
		System.out.println("\nPRICE INFORMATION:");
		System.out.println("UNIT PRICE: " + unit_price);
		System.out.println("UNIT AMOUNT: " + unit_amount);
		System.out.println("DISCOUNT RATE: " + ItemDiscount.DiscountRate);
		System.out.println("DISCOUNT DOLLAR OFF: " + ItemDiscount.DiscountDollar);
		System.out.println("TAX EXEMPT: " + ItemDiscount.TaxExempt);
		
		
		System.out.println("ADJUSTED PRICE: " + adjusted_price);
	}
	
	public void printSales()
	{
		
		if (ItemSales.isEmpty())
		{System.out.println("\nNO SALE DATA FOR ITEM: " + item.ID);}
		else
		{
			System.out.println("\nALL SALE DATA FOR ITEM: " + item.ID);
			for (Sale i: ItemSales)
			{
				if (i.getValid()) {i.printSaleData();}
				
			}
			
			System.out.println("\nPRICE INFORMATION:");
			System.out.println("UNIT PRICE: " + unit_price);
			System.out.println("DISCOUNT RATE: " + ItemDiscount.DiscountRate);
			System.out.println("DISCOUNT DOLLAR OFF: " + ItemDiscount.DiscountDollar);
			System.out.println("TAX EXEMPT: " + ItemDiscount.TaxExempt);
			System.out.println("ADJUSTED PRICE: " + adjusted_price);
		}
		

	}
	
}
