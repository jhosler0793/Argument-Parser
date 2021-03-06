import edu.jsu.mcis.*;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class ShippingCalc{		
	public static void main(String[] args){
		
		ArgumentParser p = new ArgumentParser();
		
		p.setProgramDescription("Shipping Order");
		
		p.addPositionalArgument("Item 1", "The item you want to ship", ArgumentParser.Types.STRING);
		p.addPositionalArgument("Price 1", "The price of the first item", ArgumentParser.Types.FLOAT);
		p.addPositionalArgument("Shipping Cost", "The cost to ship this item", ArgumentParser.Types.FLOAT);
		p.addNamedArgument("quantity", "q", "Allows you to order multiple of the same item", ArgumentParser.Types.INTEGER, 1);
		p.addNamedArgument("weight", "w", "The weight of the item", ArgumentParser.Types.STRING, "n/a");
        p.addNamedArgument("local", "l", "This item will be shipped locally", ArgumentParser.Types.BOOLEAN, true);
		
		p.parse(args);
        
        boolean local = p.getValueOf("local");
        
		System.out.println("\n\nYou have requested to ship "  + p.getValueOf("quantity")
                            + " " + p.getValueOf("Item 1") + "(s)\nEach " + p.getValueOf("Item 1") + " costs $"
                            + p.getValueOf("Price 1") + "\nShipping cost is $" + p.getValueOf("Shipping Cost") 
							+ "\nThe weight of this item is: " + p.getValueOf("weight") + "lbs");
		
		float shipping = p.getValueOf("Shipping Cost");
		float price = p.getValueOf("Price 1");
        int quantity = p.getValueOf("quantity");
		float total = (shipping + price) * quantity;
        
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.UP);
        
		System.out.println("\n\nTotal cost: $" + df.format(total));
		
        if(local){
            System.out.println("This will be shipped locally");
        }else{
            System.out.println("This will be shipped internationally");
        }
	}
}