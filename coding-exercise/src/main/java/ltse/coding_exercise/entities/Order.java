package ltse.coding_exercise.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class Order {
    
    private LocalDateTime timestamp;
    private String broker;
    private String sequenceId;
    private String type;
    private String symbol;
    private int quantity;
    private BigDecimal price;
    private Side side;
    
    public Order() {
    }

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
		&& sequenceId != null && !sequenceId.isEmpty()
		&& side != null && !Side.None.equals(side)
		&& price != null && !BigDecimal.ZERO.equals(price);
    }
    
    public boolean isSymbolAccepted(Set<String> symbols) {
	return symbols.contains(symbol);
    }

    public boolean isBrokerAccepted(Set<String> brokers) {
	return brokers.contains(broker);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((broker == null) ? 0 : broker.hashCode());
	result = prime * result + ((price == null) ? 0 : price.hashCode());
	result = prime * result + quantity;
	result = prime * result + ((sequenceId == null) ? 0 : sequenceId.hashCode());
	result = prime * result + ((side == null) ? 0 : side.hashCode());
	result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
	result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Order other = (Order) obj;
	if (broker == null) {
	    if (other.broker != null)
		return false;
	} else if (!broker.equals(other.broker))
	    return false;
	if (price == null) {
	    if (other.price != null)
		return false;
	} else if (!price.equals(other.price))
	    return false;
	if (quantity != other.quantity)
	    return false;
	if (sequenceId == null) {
	    if (other.sequenceId != null)
		return false;
	} else if (!sequenceId.equals(other.sequenceId))
	    return false;
	if (side != other.side)
	    return false;
	if (symbol == null) {
	    if (other.symbol != null)
		return false;
	} else if (!symbol.equals(other.symbol))
	    return false;
	if (timestamp == null) {
	    if (other.timestamp != null)
		return false;
	} else if (!timestamp.equals(other.timestamp))
	    return false;
	if (type == null) {
	    if (other.type != null)
		return false;
	} else if (!type.equals(other.type))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "Order [timestamp=" + timestamp + ", broker=" + broker + ", sequenceId=" + sequenceId + ", type=" + type
		+ ", symbol=" + symbol + ", quantity=" + quantity + ", price=" + price + ", side=" + side + "]";
    }
    
}
