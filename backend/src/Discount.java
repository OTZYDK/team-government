import java.util.ArrayList;

/*

DISCOUNT OBJECT

the discount object will hold the transformations applied to an items price
it will have a series of optional fields which will be filled


features:

TaxExempt
DiscountRate
DiscountDollar


*/

public class Discount {
	
	final ArrayList<String> Effects = new ArrayList<String>();

	Boolean TaxExempt = false;
	Double DiscountRate;
	Double DiscountDollar;
	
	Double taxRate = 2.9;
	
	
	public Discount () 
	{
		TaxExempt = false;
		DiscountRate = 0.0;
		DiscountDollar = 0.0;
		
		Effects.add(TaxExempt.toString()); Effects.add(DiscountRate.toString()); Effects.add(DiscountDollar.toString()); 
	}
	
	public Discount (Boolean tx, Double dscntrte, Double dscntdlr) 
	{
		TaxExempt = tx;
		if (dscntrte > 0) {DiscountRate = dscntrte;} else {DiscountRate = 0.0;}
		if (dscntdlr > 0) {DiscountDollar = dscntdlr;} else {DiscountDollar = 0.0;}
		
		Effects.add(TaxExempt.toString()); Effects.add(DiscountRate.toString()); Effects.add(DiscountDollar.toString()); 
	}
	
	//Accepts 3 strings as arguments
	public Discount(String tx, String dscntrte, String dscntdlr) 
	{
		//SET TAX EXEMPT - FIELD 0
		try {TaxExempt = Boolean.parseBoolean(tx.toLowerCase().trim());} catch (NumberFormatException e) {TaxExempt = false;}
		
		//SET DISCOUNT RATE - FIELD 1
		try {DiscountRate = Double.parseDouble(dscntrte.trim());} catch (NumberFormatException e) {DiscountRate = 0.0;}
		
		//SET DISCOUNT AMOUNT - FIELD 2
		try {DiscountDollar = Double.parseDouble(dscntdlr.trim());} catch (NumberFormatException e) {DiscountDollar = 0.0;}
		
	}
	
	//Accepts string as argument
	public Discount(String args) 
	{
		
		
		String[] inputArray = args.split(","); 
		
		//SET TAX EXEMPT - FIELD 0
		try {TaxExempt = Boolean.parseBoolean(inputArray[0].toLowerCase().trim());} catch (NumberFormatException e) {TaxExempt = false;}
		
		//SET DISCOUNT RATE - FIELD 1
		try {DiscountRate = Double.parseDouble(inputArray[1].trim());} catch (NumberFormatException e) {DiscountRate = 0.0;}
		
		//SET DISCOUNT AMOUNT - FIELD 2
		try {DiscountDollar = Double.parseDouble(inputArray[2].trim());} catch (NumberFormatException e) {DiscountDollar = 0.0;}
		
	}
	
	public Discount(String...args) 
	{
		
		

		
		//SET TAX EXEMPT - FIELD 0
		try {TaxExempt = Boolean.parseBoolean(args[0].toLowerCase().trim());} catch (NumberFormatException e) {TaxExempt = false;}
		
		//SET DISCOUNT RATE - FIELD 1
		try {DiscountRate = Double.parseDouble(args[1].trim());} catch (NumberFormatException e) {DiscountRate = 0.0;}
		
		//SET DISCOUNT AMOUNT - FIELD 2
		try {DiscountDollar = Double.parseDouble(args[2].trim());} catch (NumberFormatException e) {DiscountDollar = 0.0;}
		
	}
	
	public Discount (ArrayList<String> args) 
	{

		//Validate Effects
		for (int i = 0; i < args.size() && i < 3; i++) 
		{
			switch (i) 
			{
				case 0:
				{
					try {TaxExempt = Boolean.parseBoolean(args.get(0).toLowerCase());} catch (NumberFormatException e) {TaxExempt = false;}
				}
				case 1:
				{
					try {DiscountRate = Double.parseDouble(args.get(1));} catch (NumberFormatException e) {DiscountRate = 0.0;}
				}
				case 2:
				{
					try {DiscountDollar = Double.parseDouble(args.get(2));} catch (NumberFormatException e) {DiscountDollar = 0.0;}
				}
				default: {break;}
			}
			
		}
		
		//adjust arguments to restrict to 2 decimal places
		int proc1 = (int) (DiscountRate * 100);
		DiscountRate = (proc1 / 100.0);
		
		int proc2 = (int) (DiscountDollar * 100);
		DiscountDollar = (proc2 / 100.0);
		
		if (TaxExempt) {taxRate = 0.0;}
		
		
		//Add all to effects list
		Effects.add(TaxExempt.toString()); Effects.add(DiscountRate.toString()); Effects.add(DiscountDollar.toString()); 
		
		
	}
	
	//GETTERS
	public double CalcPrice(double unitPrice) 
	{
		
		//double calcPrice = unitPrice + ((unitPrice/100) * taxRate) - ((unitPrice/100) * DiscountRate) - (DiscountDollar);
		double calcPrice = unitPrice + calcTaxAmount(unitPrice) - calcPercentOff(unitPrice) - (DiscountDollar);
		return calcPrice;
	}
	
	public double calcTaxAmount(double unitPrice) 
	{
		
		double calcTax = (unitPrice/100) * taxRate;
		return calcTax;
	}
	
	public double calcPercentOff(double unitPrice) 
	{
		
		double calcPerc = (unitPrice/100) * DiscountRate;
		return calcPerc;
	}
	
	public double getDollarOff(double unitPrice) 
	{
		return DiscountDollar;
	}
	
	//If the discount has any unique properties, it is valid
	public Boolean isValid() 
	{
		if (TaxExempt != false || DiscountRate > 0.0 || DiscountDollar > 0.0)
		{return true;} else {return false;}
	}
	
}
