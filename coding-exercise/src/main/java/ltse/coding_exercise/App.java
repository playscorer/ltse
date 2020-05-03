package ltse.coding_exercise;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import ltse.coding_exercise.extractor.Parser;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
	if (args.length != 3) {
	    System.err.println("Usage : java ltse.coding_exercise.extractor.App");
	    return;
	}
        Parser parser = new Parser();
        try {
            parser.extract(args[0], args[1], args[2]);
	    //parser.extract("resources/firms.txt", "resources/symbols.txt", "resources/trades.csv");
	    
            if (Files.notExists(Paths.get("out"))) {
        	File outputDirectory = new File("out");
        	outputDirectory.mkdir();
            }
            
	    parser.writeAcceptedIds("out/acceptedIds.csv");
	    parser.writeRejectedIds("out/rejectedIds.csv");
	    parser.writeAcceptedOrders("out/acceptedOrders.txt");
	    parser.writeRejectedOrders("out/rejectedOrders.txt");
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
        
    }
}
