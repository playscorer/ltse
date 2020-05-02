package ltse.coding_exercise.extractor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
    private Map<String, List<Order>> rejectedOrders;
    
    private Map<String, Set<String>> tradeIdsByBroker;
    
    public Parser() {
	formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
	brokers = new HashSet<>();
	symbols = new HashSet<>();
	initTimestampMinuteByBroker = new HashMap<>();
	acceptedOrders = new HashMap<>();
	rejectedOrders = new HashMap<>();
	tradeIdsByBroker = new HashMap<>();
    }
    
    public void extract(String brokersFile, String symbolsFile, String tradesFile) {
	try {
	    brokers = readTxtFile(brokersFile);
	    symbols = readTxtFile(symbolsFile);
	    
	    List<Order> rawOrders = readCsvFile(tradesFile);
	    if (rawOrders != null && !rawOrders.isEmpty()) {
		rawOrders.stream().filter(filterOrder).forEach(action);
	    }
	    
	} catch (IOException e) {
	    e.printStackTrace();
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

		LocalDateTime timestamp = null;
		try {
		    timestamp = LocalDateTime.parse(row[0].trim(), formatter);
		} catch (DateTimeParseException e) {
		    System.out.printf("Error parsing timestamp : %s\n", row[0]);
		}

		String broker = row[1].trim();

		String seqId = row[2].trim();

		String type = row[3].trim();

		String symbol = row[4].trim();

		int qty = 0;
		try {
		    qty = Integer.valueOf(row[5]);
		} catch (NumberFormatException e) {
		    System.out.printf("Error parsing quantity : %s\n", row[5]);
		}

		BigDecimal price = BigDecimal.ZERO;
		try {
		    price = new BigDecimal(row[6]);
		} catch (NumberFormatException e) {
		    System.out.printf("Error parsing price : %s\n", row[6]);
		}

		Side side = Side.None;
		try {
		    side = Side.valueOf(row[7]);
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
	if (tradeIdsByBroker.get(order.getBroker()) != null && tradeIdsByBroker.get(order.getBroker()).contains(order.getSequenceId())) {
	    rejectedOrders.computeIfAbsent(order.getBroker(), empty -> new ArrayList<>()).add(order);
	    
	} else {
	    LocalDateTime initTimestamp = initTimestampMinuteByBroker.get(order.getBroker());
	    
	    if (initTimestamp == null) {
		initTimestampMinuteByBroker.put(order.getBroker(), initTimestamp);
		acceptedOrders.put(order.getBroker(), new TreeMap<>()).put(order.getTimestamp(), new ArrayList<>()).add(order);
		tradeIdsByBroker.put(order.getBroker(), new HashSet<>()).add(order.getSequenceId());
		
	    } else {
		if (Duration.between(initTimestamp, order.getTimestamp()).getSeconds() <= 60) {
		    
		    if (acceptedOrders.get(order.getBroker()).get(initTimestamp).size() < 3) {
			acceptedOrders.get(order.getBroker()).get(initTimestamp).add(order);
			tradeIdsByBroker.put(order.getBroker(), new HashSet<>()).add(order.getSequenceId());
			
		    } else {
			rejectedOrders.computeIfAbsent(order.getBroker(), empty -> new ArrayList<>()).add(order);
		    }
		} else {
		    initTimestampMinuteByBroker.put(order.getBroker(), initTimestamp);
		    acceptedOrders.put(order.getBroker(), new TreeMap<>()).put(order.getTimestamp(), new ArrayList<>()).add(order);
		    tradeIdsByBroker.put(order.getBroker(), new HashSet<>()).add(order.getSequenceId());
		}
	    }
	}
    };
    
    public void writeAcceptedIds() {
	Path path = Paths.get("out/acceptedIds.csv");
	
	acceptedOrders.entrySet().forEach(entry -> {
	    entry.getValue().values().stream().flatMap(l -> l.stream())
	    	.forEach(order -> {
	    	    String line = new StringBuilder(entry.getKey()).append(",").append(order.getSequenceId()).toString();
	    	    try {
			Files.write(path, line.getBytes());
		    } catch (Exception e) {
			e.printStackTrace();
		    }
	    	});
	});
    }
    
    public void writeRejectedIds() {
	Path path = Paths.get("out/rejectedIds.csv");
	
	rejectedOrders.entrySet().forEach(entry -> {
	    entry.getValue().stream().forEach(order -> {
		String line = new StringBuilder(entry.getKey()).append(",").append(order.getSequenceId()).toString();
		try {
		    Files.write(path, line.getBytes());
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    });
	});
    }
    
    public void writeAcceptedOrders() throws IOException {
	FileOutputStream fileOutputStream = new FileOutputStream("out/acceptedOrders.txt");
	ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
	
	acceptedOrders.values().stream().flatMap(m -> m.values().stream()).forEach(order -> {
	    try {
		objectOutputStream.writeObject(order);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	});
	
	objectOutputStream.flush();
	objectOutputStream.close();
    }
    
    public void writeRejectedOrders() throws IOException {
	FileOutputStream fileOutputStream = new FileOutputStream("out/rejectedOrders.txt");
	ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
	
	rejectedOrders.values().forEach(order -> {
	    try {
		objectOutputStream.writeObject(order);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	});
	
	objectOutputStream.flush();
	objectOutputStream.close();
    }
    
    
}
