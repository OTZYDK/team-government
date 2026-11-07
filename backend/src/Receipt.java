import java.util.ArrayList;

/*
RECEPT FORMAT


DESCRIPION
	amount - item - price
		x% off -x.yz$
		dollar off -x.yz$
	amount - item - price
		x% off -x.yz$
		dollar off -x.yz$
	amount - item - price
		x% off -x.yz$
		dollar off -x.yz$

DISCOUNT (if any)
	discount description
		item name - discount effects
		item name - discount effects
		
	discount description
		item name - discount effects
		item name - discount effects
		

TOTAL TAX: 
DISCOUNT SAVINGS: 
TOTAL PRICE:

PROVIDED CASH:
CHANGE: 






first get list of all Sales applied to receipt items
then

*/

public class Receipt {

	//---------PROVIDED FIELDS / REQUIRED ARGUMENTS
	//ReceiptItems - List of CartItems the Receipt is based on
	private ArrayList<CartItem> ReceiptItems = new ArrayList<CartItem>(); 
	
	//Payment - Cash Payment provided by Customer
	private double Payment;
	
	
	//---------GENERATED FIELDS
	//List of All Sales in ReceiptItems (with no duplicates)
	private ArrayList<Sale> ItemSales = new ArrayList<Sale>(); 
	
	//Area Headers
	final public String DescHead = "ITEMS";
	final public String SaveHead = "DISCOUNTS";
	final public String PriceHead = "PRICING";
	
	//Description - Two Dimensional ArrayList holding description text values for all receipt items
	private ArrayList<ArrayList<String>> Description = new ArrayList<ArrayList<String>>();
	
	//Discounts - Two Dimensional ArrayList containing String data for each Discounts values
	private ArrayList<ArrayList<String>> Discounts = new ArrayList<ArrayList<String>>();
	
	//Price Values
	private double Total_Tax;
	private double Total_Savings;
	
	//Total Before Tax and other values
	private double subTotal;
	
	//All Costs Combined
	private double Total;
		
	//Change if Applicable
	private double Change;
	
	
	//---------CONSTRUCTOR
	public Receipt(ArrayList<CartItem> source, double payment) 
	{
		ReceiptItems = source;
		
		buildDescription();
		setItemSales();
		buildDiscounts();
		setPriceValues();
		setTransactionValues(payment);
		
		printReceipt();
	}
	
	
	//---------INTERNAL SETTERS
	
	//--->BUILD DESCRIPTION, build or return valid list of text for description area
	
	//Builds from internal values and directly updates internal values
	private void buildDescription()
	{Description = buildDescription(ReceiptItems);}
	//Builds from argument and returns valid list
	private ArrayList<ArrayList<String>> buildDescription(ArrayList<CartItem> args)
	{
		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
		
		for (CartItem i: args)
		{	
			ArrayList<String> get = i.getTab();
			temp.add(get);
		}
		
		return temp;
	}
	
	//--->SET ITEM SALES, sets or returns valid list of (unique, non-duplicating) sales objects 
	//Builds from internal values and directly updates internal values
	private void setItemSales() 
	{ItemSales = setItemSales(ReceiptItems);}
	//Builds from argument and returns valid list
	private ArrayList<Sale> setItemSales(ArrayList<CartItem> args) 
	{
		//Create temporary list
		ArrayList<Sale> temp = new ArrayList<Sale>(); 
		
		//for every item in our list
		for (CartItem i: args)
		{
			//If there are no sales, ignore it
			if (i.getSales().isEmpty())
			{continue;}
			else
			{
				//check every sale in its list and add it to our own
				for (Sale z: i.getSales())
				{
					//if a sale is valid
					if (z.getValid())
					{
						//check to see if the sale is already in our list
						if (!temp.contains(z)) 
						{
							//if a sale is not already in the list
							temp.add(z);
						}
					}
				}
			}
		}
		
		return temp;
	}

	//--->BUILD DISCOUNTS, sets or returns valid list of text for discount area
	//Defaults to internal variables
	private void buildDiscounts() 
	{Discounts = buildDiscounts(ItemSales);}
	//Returns list from provided arguments
	private ArrayList<ArrayList<String>> buildDiscounts(ArrayList<Sale> args) 
	{	
		//Empty Array list of Strings
		//ArrayList<String> temp = new ArrayList<String>(); 
		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
		
		
		//Add summary line for every sale
		for (Sale i: args)
		{
			ArrayList<String> get = i.getSummaryList();
			temp.add(get);
			
		}
		
		//return list
		return temp;
	}
	
	//--->SET PRICE AREA VALUES, sets internal price values based on internal data or provided list
	//Builds from internal values and directly updates internal values
	private void setPriceValues() 
	{setPriceValues(ReceiptItems);}
	//Builds from provided values and directly updates internal values
	private void setPriceValues(ArrayList<CartItem> args) 
	{
		//Combined Tax amounts and Discount amounts
		double _Total_Tax = 0;
		double _Total_Savings = 0;
		
		//Total Before Tax and other values
		double _subTotal = 0;
		
		//All Costs Combined
		double _Total = 0;
		
		//Combine Values
		for (CartItem i: args)
		{
			//Add Tax multiplied by unit amount
			_Total_Tax += i.getTaxAmount() * i.getUnitAmount();

			//Add Savings multiplied by unit amount
			_Total_Savings += (i.getPercentOff() + i.getDiscountDollar()) * i.getUnitAmount();
			
			_subTotal += i.unit_price * i.getUnitAmount();
			
			_Total += i.getAdjustedPrice() * i.getUnitAmount();
		}
		
		//Set Internal Values
		Total_Tax = util.priceFormat(_Total_Tax);
		Total_Savings = util.priceFormat(_Total_Savings);
		
		subTotal = util.priceFormat(_subTotal);
		Total = util.priceFormat(_Total);
	}
	
	//--->SET (CALCULATE) TRANSACTION VALUES
	private void setTransactionValues(double arg_payment) 
	{
		this.Payment = arg_payment;
		Change = Payment - Total;
	}
	
	//---------GETTERS
	//--->SECTIONS AS FORMATTED STRING LISTS
	//ITEMS DESCRIPTION SECTION
	public ArrayList<String> getItemSection()
	{
		ArrayList<String> build = new ArrayList<String>();
		
		//For Each list of values
		for (ArrayList<String> i: Description)
		{
			//Main Line
			String amnt = i.get(0);
			String desc = i.get(1);
			String price = i.get(2);
			
			build.add(String.format("%s. %s: %s$", amnt, desc, price ));
			
			//Optional Data
			String rate = i.get(3);
			String dol = i.get(4);
			
			//If there is discounts
			if (rate != "") {build.add("-" + rate);}
			if (dol != "") {build.add("-" + dol);}
		}
		
		return build;
	}
	
	//DISCOUNT SECTION
	public ArrayList<String> getDiscountSection()
	{
		ArrayList<String> build = new ArrayList<String>();
		
		//For Each list of values
		for (ArrayList<String> i: Discounts)
		{
			//Main Line
			String id = i.get(0);
			String desc = i.get(1);
			
			build.add(String.format("%s. %s", id, desc));
			
			//Optional Data
			String rate = i.get(2);
			String dol = i.get(3);
			
			//If there is discounts
			if (rate != "") {build.add(rate + " Off");}
			if (dol != "") {build.add(dol + " Off");}
		}
		
		return build;
	}
		
	//PRICING SECTION
	public ArrayList<String> getPricingSection()
	{
		ArrayList<String> build = new ArrayList<String>();
		
		//Add All values with preceeding text
		build.add("Tax: " + Total_Tax + "$");
		build.add("Savings: " + Total_Savings + "$");
		build.add("Subtotal: " + subTotal + "$");
		build.add("Total: " + Total + "$");
		build.add("CASH: " + Payment + "$");
		build.add("CHANGE: " + Change + "$");
		
		return build;
	}

	
	//--->SECTIONS AS DATA FOR DIRECT PULLING
	//ITEM DESCRIPTION MULTI DIMENSIONAL ARRAY LIST
	public ArrayList<ArrayList<String>> getItemData() {return Description;}
	
	//ITEM DESCRIPTION MULTI DIMENSIONAL ARRAY LIST
	public ArrayList<ArrayList<String>> getDiscountData() {return Discounts;}
	
	//PRICING VALUES
	public String getTotalTax() {return Double.toString(util.priceFormat(Total_Tax));}
	public String getTotalSavings() {return Double.toString(util.priceFormat(Total_Savings));}
	public String getSubTotal() {return Double.toString(util.priceFormat(subTotal));}
	public String getTotal() {return Double.toString(util.priceFormat(Total));}
	public String getPayment() {return Double.toString(util.priceFormat(Payment));}
	public String getChange() {return Double.toString(util.priceFormat(Change));}
	
	
	//---------PRINTERS
	public void printReceipt() 
	{
		//Items Description Head
		System.out.println("\n" + DescHead);
		
		for (String i : getItemSection())
		{System.out.println(i);}
		
		//Savings Description Head
		System.out.println("\n" + SaveHead);
		
		for (String i : getDiscountSection())
		{System.out.println(i);}
		
		//Pricing Area Head + Fields
		System.out.println("\n" + PriceHead);

		for (String i : getPricingSection())
		{System.out.println(i);}
		
	}
	
}
