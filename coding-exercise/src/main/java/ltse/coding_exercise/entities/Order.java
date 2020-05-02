package ltse.coding_exercise.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class Order implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private LocalDateTime timestamp;
    private String broker;
    private String sequenceId;
    private String type;
    private String symbol;
    private int quantity;
    private BigDecimal price;
    private Side side;
    
    public Order(LocalDateTime timestamp, String broker, String sequenceId, String type, String symbol, int quantity,
	    BigDecimal price, Side side) {
	this.timestamp = timestamp;
	this.broker = broker;
	this.sequenceId = sequenceId;
	this.type = type;
	this.symbol = symbol;
	this.quantity = quantity;
	this.price = price;
	this.side = side;
    }
    
    /**
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * @return the broker
     */
    public String getBroker() {
        return broker;
    }

    /**
     * @return the sequenceId
     */
    public String getSequenceId() {
        return sequenceId;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return the price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * @return the side
     */
    public Side getSide() {
        return side;
    }

    public boolean isValid() {
	return broker != null && !broker.isEmpty()
		&& symbol != null && !symbol.isEmpty()
		&& type != null && !type.isEmpty()
		&& quantity != 0
		&& sequenceId != null && sequenceId.isEmpty()
		&& side != null && !Side.None.equals(side)
		&& price != null && !BigDecimal.ZERO.equals(price);
    }
    
    public boolean isSymbolAccepted(Set<String> symbols) {
	return symbols.contains(symbol);
    }

    public boolean isBrokerAccepted(Set<String> brokers) {
	return brokers.contains(broker);
    }
    
}
