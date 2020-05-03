package ltse.coding_exercise.extractor;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ltse.coding_exercise.entities.Order;
import ltse.coding_exercise.entities.Side;

public class Parser {
    
    private DateTimeFormatter formatter;

    private Set<String> brokers;
    private Set<String> symbols;
    
    private Map<String, LocalDateTime> initTimestampMinuteByBroker;
    private Map<String, Map<LocalDateTime, List<Order>>> acceptedOrders;
    private List<Order> rejectedOrders;
    
    private Map<String, Set<String>> tradeIdsByBroker;
    
    public Parser() {
	formatter = DateTimeFormatter.ofPattern("MM/d/yyyy HH:mm:ss");
	brokers = new HashSet<>();
	symbols = new HashSet<>();
	initTimestampMinuteByBroker = new HashMap<>();
	acceptedOrders = new HashMap<>();
	rejectedOrders = new ArrayList<>();
	tradeIdsByBroker = new HashMap<>();
    }
    
    public void extract(String brokersFile, String symbolsFile, String tradesFile) throws IOException {
	brokers = readTxtFile(brokersFile);
	symbols = readTxtFile(symbolsFile);

	List<Order> rawOrders = readCsvFile(tradesFile);
	if (rawOrders != null && !rawOrders.isEmpty()) {
	    rawOrders.stream().filter(filterOrder.negate()).forEach(o -> rejectedOrders.add(o));
	    rawOrders.stream().filter(filterOrder).forEach(action);
	}
    }
    
    public Set<String> readTxtFile(String file) throws IOException {
	Path path = Paths.get(file);
	return new HashSet<>(Files.readAllLines(path));
    }
    
    public List<Order> readCsvFile(String file) throws IOException {
	Path path = Paths.get(file);

	try (Stream<String> stream = Files.lines(path)) {
	    return stream.skip(1).map(line -> {
		String[] row = line.split(",");

		if (row.length == 8) {
		    System.out.printf("%s %s %s %s %s %s %s %s\n", row[0],row[1],row[2],row[3],row[4],row[5],row[6],row[7]);
		} else {
		    System.out.println("Malformed order");
		}

		LocalDateTime timestamp = null;
		try {
		    timestamp = row.length > 0 ? LocalDateTime.parse(row[0].trim(), formatter) : null;
		} catch (DateTimeParseException e) {
		    System.out.printf("Error parsing timestamp : %s\n", row[0]);
		}

		String broker = row.length > 1 ? row[1].trim() : "";

		String seqId = row.length > 2 ? row[2].trim() : "";

		String type = row.length > 3 ? row[3].trim() : "";

		String symbol = row.length > 4 ? row[4].trim() : "";

		int qty = 0;
		try {
		    qty = row.length > 5 ? Integer.valueOf(row[5]) : 0;
		} catch (NumberFormatException e) {
		    System.out.printf("Error parsing quantity : %s\n", row[5]);
		}

		BigDecimal price = BigDecimal.ZERO;
		try {
		    price = row.length > 6 ? new BigDecimal(row[6]) : BigDecimal.ZERO;
		} catch (NumberFormatException e) {
		    System.out.printf("Error parsing price : %s\n", row[6]);
		}

		Side side = Side.None;
		try {
		    side = row.length > 7 ? Side.valueOf(row[7]) : Side.None;
		} catch (Exception e) {
		    System.out.printf("Error parsing side : %s\n", row[7]);
		}

		return new Order(timestamp, broker, seqId, type, symbol, qty, price, side);
	    }).collect(Collectors.toList());
	    
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
    
    public Predicate<Order> filterOrder = order -> order.isValid() && order.isSymbolAccepted(symbols) && order.isBrokerAccepted(brokers);
    
    public Consumer<Order> action = order -> {
	LocalDateTime initTimestamp = initTimestampMinuteByBroker.get(order.getBroker());

	// first insertion for a specific broker
	if (initTimestamp == null) {
	    initTimestampMinuteByBroker.put(order.getBroker(), order.getTimestamp());
	    acceptedOrders.computeIfAbsent(order.getBroker(), m -> new TreeMap<>()).computeIfAbsent(order.getTimestamp(), m -> new ArrayList<>()).add(order);
	    tradeIdsByBroker.computeIfAbsent(order.getBroker(), h -> new HashSet<>()).add(order.getSequenceId());

	} else {
	    // order submitted within a minute
	    if (Duration.between(initTimestamp, order.getTimestamp()).getSeconds() <= 60) {
		// three orders per minute autorized
		if (acceptedOrders.get(order.getBroker()).get(initTimestamp).size() < 3) {
		    // sequenceId must be unique within a single trade
		    if (tradeIdsByBroker.get(order.getBroker()).contains(order.getSequenceId())) {
			rejectedOrders.add(order);
		    } else {
			acceptedOrders.get(order.getBroker()).get(initTimestamp).add(order);
			tradeIdsByBroker.get(order.getBroker()).add(order.getSequenceId());			
		    }

		} else { // trade is full
		    rejectedOrders.add(order);
		    tradeIdsByBroker.get(order.getBroker()).clear();
		}
	    } else { // new trade
		tradeIdsByBroker.get(order.getBroker()).clear();
		initTimestampMinuteByBroker.put(order.getBroker(), order.getTimestamp());
		acceptedOrders.get(order.getBroker()).computeIfAbsent(order.getTimestamp(), empty -> new ArrayList<>()).add(order);
		tradeIdsByBroker.get(order.getBroker()).add(order.getSequenceId());
	    }
	}
    };
    
    public void writeAcceptedIds(String output) {
	Path path = Paths.get(output);
	StringBuilder builder = new StringBuilder();
	
	acceptedOrders.values().stream().flatMap(m -> m.values().stream()).flatMap(l -> l.stream())
		.sorted((o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp())).forEach(order -> {
		    builder.append(order.getBroker()).append(",").append(order.getSequenceId()).append(System.lineSeparator());
		});
	
	try {
	    Files.write(path, builder.toString().getBytes());
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public void writeRejectedIds(String output) {
	Path path = Paths.get(output);
	StringBuilder builder = new StringBuilder();
	
	rejectedOrders.stream().sorted((o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()))
		.forEach(o -> builder.append(o.getBroker()).append(",").append(o.getSequenceId()).append(System.lineSeparator()));
	
	try {
	    Files.write(path, builder.toString().getBytes());
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public void writeAcceptedOrders(String output) {
	Path path = Paths.get(output);
	
	try {
	    Files.write(path,
		    acceptedOrders.values().stream().flatMap(m -> m.values().stream()).flatMap(l -> l.stream())
			    .sorted((o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp())).map(o -> o.toString())
			    .collect(Collectors.joining(System.lineSeparator())).getBytes());
	} catch (IOException e) {
	    e.printStackTrace();
	}	
    }
    
    public void writeRejectedOrders(String output) {
	Path path = Paths.get(output);
	
	try {
	    Files.write(path, rejectedOrders.stream().sorted((o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp())).map(o -> o.toString())
		    .collect(Collectors.joining(System.lineSeparator())).getBytes());
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public void logAcceptedOrders() throws IOException {
	System.out.println("## Accepted orders ##");
	acceptedOrders.values().stream().flatMap(m -> m.values().stream()).forEach(System.out::println);
    }
    
    /**
     * @return the acceptedOrders
     */
    public List<Order> getAcceptedOrders() {
        return acceptedOrders.values().stream().flatMap(m -> m.values().stream()).flatMap(v -> v.stream()).collect(Collectors.toList());
    }

    /**
     * @return the rejectedOrders
     */
    public List<Order> getRejectedOrders() {
        return rejectedOrders;
    }

    /**
     * @param brokers the brokers to set
     */
    public void setBrokers(Set<String> brokers) {
        this.brokers = brokers;
    }

    /**
     * @param symbols the symbols to set
     */
    public void setSymbols(Set<String> symbols) {
        this.symbols = symbols;
    }
    
}
