package transaction;

public class Flights implements ResourceItem {

    private String flightNum;
    private int price; // every seat has the same price
    private int numSeats;
    private int numAvail;
    protected boolean isdeleted = false;

    public Flights(String flightNum, int price, int numSeats) {
        this.flightNum = flightNum;
        this.price = price;
        this.numSeats = numSeats;
        this.numAvail = numSeats;
    }

    private Flights(String flightNum, int price, int numSeats, int numAvail) {
        this.flightNum = flightNum;
        this.price = price;
        this.numSeats = numSeats;
        this.numAvail = numAvail;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public void setFlightNum(String flightNum) {
        this.flightNum = flightNum;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public void setNumSeats(int numSeats) {
        this.numSeats = numSeats;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setNumAvail(int numAvail) {
        this.numAvail = numAvail;
    }

    public void bookSeats(int num) {
        this.numAvail -= num;
    }

    public void unbookSeats(int num) {
        this.numAvail += num;
    }

    public void addSeats(int numSeats) {
        this.numSeats += numSeats;
        this.numAvail += numSeats;
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public String[] getColumnValues() {
        return new String[0];
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        throw new InvalidIndexException(indexName);
    }

    @Override
    public Object getKey() {
        return flightNum;
    }

    @Override
    public boolean isDeleted() {
        return isdeleted;
    }

    @Override
    public void delete() {
        isdeleted = true;
    }

    @Override
    public Object clone() {
        return new Flights(this.flightNum, this.price, this.numSeats, this.numAvail);
    }
}
