package transaction;

public class Hotels implements ResourceItem{
    protected boolean isdeleted = false;
    private String location; // key, there is only one hotel at a location
    private int price; // every room has the same price
    private int numRooms;
    private int numAvail;

    public Hotels(String location, int price, int numRooms) {
        this.location = location;
        this.price = price;
        this.numRooms = numRooms;
        this.numAvail = numRooms;
    }

    private Hotels(String location, int price, int numRooms, int numAvail) {
        this.location = location;
        this.price = price;
        this.numRooms = numRooms;
        this.numAvail = numAvail;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumRooms() {
        return numRooms;
    }

    public void setNumRooms(int numRooms) {
        this.numRooms = numRooms;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setNumAvail(int numAvail) {
        this.numAvail = numAvail;
    }
    public void addRooms(int num) {
        this.numRooms += num;
        this.numAvail += num;
    }

    public void deleteRooms(int num) {
        this.numRooms -= num;
        this.numAvail -= num;
    }

    public void bookRooms(int num) {
        this.numAvail -= num;
    }
    public void unbookRooms(int num) {
        this.numAvail += num;
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
    public void resverSeats(int num) {
        this.numAvail -= num;
    }

    public void cancelResverSeats(int num) {
        this.numAvail += num;
    }
    @Override
    public Object getKey() {
        return location;
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
        return new Hotels (this.location, this.price, this.numRooms, this.numAvail);
    }
}
