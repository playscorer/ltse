package ltse.coding_exercise.extractor;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import junit.framework.Assert;

public class ParserTest {

    @Test
    public void testReadTxtFile() {
	String file = "resources/firms.txt";
	Parser parser = new Parser();
	
	try {
	    Set<String> lines = parser.readTxtFile(file);
	    Assert.assertEquals(12, lines.size());
	    
	} catch (IOException e) {
	    e.printStackTrace();
	    fail();
	}
    }
    
    @Test
    public void testReadCsvFile() {
	
    }
    
    @Test
    public void testConsumer() {
	
    }

}
