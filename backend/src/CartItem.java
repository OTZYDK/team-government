/*THE CART ITEM CLASS

The CartItem class has two constructors, one that takes only an item argument
and one that takes up to 3 optional arguments, these arguments being:
discount_rate, tax_exempt, and unit_amount. Here are examples of the constructor being used ->

	CartItem example = new CartItem(Item_Example, "20", "true", "6");
	Discount Rate -> 20%, Tax Exempt -> True, Unit Amount -> 6

If you want to skip an argument, use "" so that constructors knows, 
any value not provided will use its default value ->

	CartItem example = new CartItem(Item_Example, "", "", "");
	Discount Rate -> 0%, Tax Exempt -> false, Unit Amount -> 1
	
Finally, if you want to easily convert arguments to string, simply add ""->
	CartItem example = new CartItem(Item_Example, example_double + "", example_bool + "", example_int + "");
	Discount Rate -> example_double, Tax Exempt -> example_bool, Unit Amount -> example_int

I designed it this way so that we can could have a more flexible constructor, 
that allows you to skip certain arguments if necessary. 
*/


@FunctionalInterface
interface CalculatePrice
{
    double calculate(double unitPrice, double discountRate, double taxRate);
    //Interface-Lambda to calculate price
}

public class CartItem{
	//Define Lambda to calculate the price
	CalculatePrice updatePrice = (unitPrice, discountRate, taxRate) -> unitPrice + ((unitPrice/100) * taxRate) - ((unitPrice/100) * discountRate);
	
	//Default Attributes
	private double discount_rate = 0;
	private double tax_rate = 2.9;
	private int unit_amount = 1;
	private boolean tax_exempt = false;
	
	//Constructed Attributes
	final CatalogItem item;
	final double unit_price;
	
	//Calculated Attributes
	private double adjusted_price;
	private double total_price;
	
	//------------CONSTRUCTORS
	//PRIMARY CONSTRUCTOR
	public CartItem(CatalogItem item) {
		this.item = item;
		unit_price = item.Price;
		
		//Calculate Price(s)
		setAdjustedPrice();
	}
		
	//CONSTRUCTOR WITH OPTIONAL PARAMTERS: discount_rate, tax_exempt, unit_amount, USE "" WHEN NO ARG
    public CartItem(CatalogItem item, String... args) {
    	//SET CATALOG ITEM
		this.item = item;
		unit_price = item.Price;
		
		//SET OPTIONAL ARGUMENTS
        //ARGUMENT ORDER: -> double discount_rate, boolean tax_exempt, int unit_amount
        //Iterate through provided arguments, 
    	//checking to makes sure the conversions do not fail
		
    	//Hold all potential arguments
    	double discount_rate;
    	boolean tax_exempt;
    	int unit_amount;
    	
    	//ITERATE THROUGH ARGUMENTS TO CHECK VALUES
		for (int i = 0; i < args.length; i++)
		{
        	//ARG 0 - Discount Rate
        	if (i == 0 && args[i] != "")
        	{
                try {discount_rate = Double.parseDouble(args[i]); setDiscount(discount_rate);}
                catch (Exception e) {System.out.println("Discount Rate not provided");}
        	}
        	
        	//ARG 1 - Tax Exempt
        	if (i == 1 && args[i] != "")
        	{
                try {tax_exempt = Boolean.parseBoolean(args[i]); setTaxExempt(tax_exempt);}
                catch (Exception e) {System.out.println("Discount Rate not provided");}
        	}
        	
        	//ARG 2 - Unit Amount
        	if (i == 2 && args[i] != "")
        	{
                try {unit_amount= Integer.parseInt(args[i]); setAmount(unit_amount);}
                catch (Exception e) {System.out.println("Amount was not provided");}
        	}
		}
		
        //System.out.println(); 
    }

	
	//------------SETTERS
    //Update Adjusted Price - Call this any time the amount, discount, or tax parameters are updated
	private void setAdjustedPrice() {
		//Correct Unit Amount
		if (unit_amount < 1) {unit_amount = 1;}
		
		//Apply Tax Rate if we are not tax exempt
		if (tax_exempt) 
		{
			adjusted_price = updatePrice.calculate(unit_price, discount_rate, 0);
			total_price = updatePrice.calculate(unit_price*unit_amount, discount_rate, 0);		
		}
		else 
		{
			adjusted_price = updatePrice.calculate(unit_price, discount_rate, tax_rate);
			total_price = updatePrice.calculate(unit_price*unit_amount, discount_rate, tax_rate);
		}
	}
	
	
	//SET TAX EXEMPT - PROVIDE VALUE OR CALL METHOD TO FLIP IT
	public void setTaxExempt(boolean tax_exempt) {this.tax_exempt = tax_exempt; setAdjustedPrice();}
	public void setExemptTrue() {this.tax_exempt = true; setAdjustedPrice();}
	public void setExemptFalse() {this.tax_exempt = false; setAdjustedPrice();}
	
	//SET DISCOUNT
	public void setDiscount(double discount_rate) {
		if (discount_rate > 0) {this.discount_rate = discount_rate;}
		setAdjustedPrice();
	}
	
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
	//Coming Soon!
	
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
		System.out.println("DISCOUNT RATE: " + discount_rate);
		System.out.println("TAX EXEMPT: " + tax_exempt);
		
		
		System.out.println("ADJUSTED PRICE: " + adjusted_price);
		System.out.println("TOTAL PRICE: " + total_price);
	}
	
}
