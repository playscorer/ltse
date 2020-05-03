package ltse.coding_exercise.extractor;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;
import ltse.coding_exercise.entities.Order;
import ltse.coding_exercise.entities.Side;

public class ParserTest {
    
    private Parser parser;
    
    @Before
    public void setup() {
	parser = new Parser();
    }

    @Test
    public void testReadTxtFile() {
	String file = "resources/firms.txt";
	
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
	String file = "resources/trades.csv";
	
	try {
	    List<Order> orders = parser.readCsvFile(file);
	    Assert.assertEquals(554, orders.size());
	    
	    Order order = orders.get(0);
	    Assert.assertEquals("Fidelity", order.getBroker());
	    Assert.assertEquals("1", order.getSequenceId());
	    Assert.assertEquals("2", order.getType());
	    Assert.assertEquals("BARK", order.getSymbol());
	    Assert.assertEquals(100, order.getQuantity());
	    Assert.assertEquals(new BigDecimal("1.195"), order.getPrice());
	    Assert.assertEquals(Side.Buy, order.getSide());
	    
	    order = orders.get(5);
	    Assert.assertEquals("Raymond James Financial", order.getBroker());
	    Assert.assertEquals("1", order.getSequenceId());
	    Assert.assertEquals("K", order.getType());
	    Assert.assertEquals("YLLW", order.getSymbol());
	    Assert.assertEquals(200, order.getQuantity());
	    Assert.assertEquals(new BigDecimal("11"), order.getPrice());
	    Assert.assertEquals(Side.Buy, order.getSide());
	    
	    order = orders.get(6);
	    Assert.assertEquals("Wells Fargo Advisors", order.getBroker());
	    Assert.assertEquals("1", order.getSequenceId());
	    Assert.assertEquals("K", order.getType());
	    Assert.assertEquals("LGHT", order.getSymbol());
	    Assert.assertEquals(100, order.getQuantity());
	    Assert.assertEquals(new BigDecimal("140.1"), order.getPrice());
	    Assert.assertEquals(Side.Sell, order.getSide());
	    
	} catch (IOException e) {
	    e.printStackTrace();
	    fail();
	}
    }
    
    @Test
    public void testFilterOrder() {
	parser.setBrokers(new HashSet<>(Arrays.asList("Wells Fargo Advisors", "Fidelity")));
	parser.setSymbols(new HashSet<>(Arrays.asList("HOOF", "LGHT")));
	
	Order emptyOrder = new Order();
	Order validOrder = new Order(LocalDateTime.now(), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order invalidOrder = new Order(LocalDateTime.now(), "Wells", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order invalidOrder2 = new Order(LocalDateTime.now(), "Fidelity", null, null, "LGHT", 100, new BigDecimal(105.4), Side.Buy);
	Order invalidOrder3 = new Order(LocalDateTime.now(), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.None);
	Order invalidOrder4 = new Order(LocalDateTime.now(), "Wells Fargo Advisors", "1", "K", "HOO", 100, new BigDecimal(105.4), Side.Sell);

	Assert.assertEquals(0, Arrays.asList(emptyOrder).stream().filter(parser.filterOrder).count());
	Assert.assertEquals(1, Arrays.asList(validOrder).stream().filter(parser.filterOrder).count());
	Assert.assertEquals(0, Arrays.asList(invalidOrder).stream().filter(parser.filterOrder).count());
	Assert.assertEquals(0, Arrays.asList(invalidOrder2).stream().filter(parser.filterOrder).count());
	Assert.assertEquals(0, Arrays.asList(invalidOrder3).stream().filter(parser.filterOrder).count());
	Assert.assertEquals(0, Arrays.asList(invalidOrder4).stream().filter(parser.filterOrder).count());
	
    }
    
    @Test
    public void testConsumer1() {
	Order order1 = new Order(LocalDateTime.of(2020,1,1,10,0,0), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order2 = new Order(LocalDateTime.of(2020,1,1,10,0,25), "Wells Fargo Advisors", "2", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order3 = new Order(LocalDateTime.of(2020,1,1,10,0,35), "Wells Fargo Advisors", "3", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order4 = new Order(LocalDateTime.of(2020,1,1,10,0,40), "Wells Fargo Advisors", "4", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	
	Arrays.asList(order1, order2, order3, order4).stream().forEach(parser.action);
	
	Assert.assertEquals(3, parser.getAcceptedOrders().size());
	Assert.assertEquals(1, parser.getRejectedOrders().size());
	
    }
    
    @Test
    public void testConsumer2() {
	Order order1 = new Order(LocalDateTime.of(2020,1,1,10,0,0), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order2 = new Order(LocalDateTime.of(2020,1,1,10,0,25), "Wells Fargo Advisors", "2", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order3 = new Order(LocalDateTime.of(2020,1,1,10,0,35), "Wells Fargo Advisors", "3", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order4 = new Order(LocalDateTime.of(2020,1,1,10,10,40), "Wells Fargo Advisors", "4", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	
	Arrays.asList(order1, order2, order3, order4).stream().forEach(parser.action);
	
	Assert.assertEquals(4, parser.getAcceptedOrders().size());
	Assert.assertEquals(0, parser.getRejectedOrders().size());
	
    }
    
    @Test
    public void testConsumer3() {
	Order order1 = new Order(LocalDateTime.of(2020,1,1,10,0,0), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order2 = new Order(LocalDateTime.of(2020,1,1,10,0,25), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order3 = new Order(LocalDateTime.of(2020,1,1,10,0,35), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order4 = new Order(LocalDateTime.of(2020,1,1,10,10,40), "Wells Fargo Advisors", "2", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	
	Arrays.asList(order1, order2, order3, order4).stream().forEach(parser.action);
	
	Assert.assertEquals(2, parser.getAcceptedOrders().size());
	Assert.assertEquals(2, parser.getRejectedOrders().size());
	
    }
    
    @Test
    public void testConsumer4() {
	Order order1 = new Order(LocalDateTime.of(2020,1,1,10,0,0), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order2 = new Order(LocalDateTime.of(2020,1,1,10,0,25), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order3 = new Order(LocalDateTime.of(2020,1,1,10,0,35), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order4 = new Order(LocalDateTime.of(2020,1,1,10,10,40), "Wells Fargo Advisors", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	
	Arrays.asList(order1, order2, order3, order4).stream().forEach(parser.action);
	
	Assert.assertEquals(2, parser.getAcceptedOrders().size());
	Assert.assertEquals(2, parser.getRejectedOrders().size());
	
    }
    
    @Test
    public void testConsumer5() {
	Order order1 = new Order(LocalDateTime.of(2020,1,1,10,0,0), "Wells Fargo Advisors", "1", "K", "LGHT", 100, new BigDecimal(105.4), Side.Buy);
	Order order2 = new Order(LocalDateTime.of(2020,1,1,10,0,25), "Fidelity", "1", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order3 = new Order(LocalDateTime.of(2020,1,1,10,1,35), "Wells Fargo Advisors", "2", "K", "LGHT", 100, new BigDecimal(105.4), Side.Buy);
	Order order4 = new Order(LocalDateTime.of(2020,1,1,10,2,40), "Fidelity", "2", "K", "LGHT", 100, new BigDecimal(105.4), Side.Buy);
	Order order5 = new Order(LocalDateTime.of(2020,1,1,10,10,0), "Wells Fargo Advisors", "3", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order6 = new Order(LocalDateTime.of(2020,1,1,10,20,0), "Fidelity", "2", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order7 = new Order(LocalDateTime.of(2020,1,1,10,24,0), "Wells Fargo Advisors", "3", "K", "LGHT", 100, new BigDecimal(105.4), Side.Buy);
	Order order8 = new Order(LocalDateTime.of(2020,1,1,10,24,20), "Wells Fargo Advisors", "4", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order9 = new Order(LocalDateTime.of(2020,1,1,10,24,40), "Wells Fargo Advisors", "5", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order10 = new Order(LocalDateTime.of(2020,1,1,10,25,10), "Wells Fargo Advisors", "3", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order11 = new Order(LocalDateTime.of(2020,1,1,10,25,20), "Wells Fargo Advisors", "3", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	Order order12 = new Order(LocalDateTime.of(2020,1,1,10,27,20), "Wells Fargo Advisors", "3", "K", "HOOF", 100, new BigDecimal(105.4), Side.Buy);
	
	Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8, order9, order10, order11, order12).stream().forEach(parser.action);
	
	Assert.assertEquals(11, parser.getAcceptedOrders().size());
	Assert.assertEquals(1, parser.getRejectedOrders().size());
	
    }

}
