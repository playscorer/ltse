package ltse.coding_exercise;

import java.io.IOException;

import ltse.coding_exercise.extractor.Parser;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Parser parser = new Parser();
        try {
	    parser.extract("resources/firms.txt", "resources/symbols.txt", "resources/trades.csv");
	    
	    parser.writeAcceptedIds("out/acceptedIds.csv");
	    parser.writeRejectedIds("out/rejectedIds.csv");
	    parser.writeAcceptedOrders("out/acceptedOrders.txt");
	    parser.writeRejectedOrders("out/rejectedOrders.txt");
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
        
    }
}
