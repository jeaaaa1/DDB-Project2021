package transaction;

public class Cars implements ResourceItem{
    private String location;
    private int price; // every car has the same price
    private int numCars;
    private int numAvail;
    protected boolean isdeleted = false;

    public Cars(String location, int price, int numCars) {
        this.location = location;
        this.price = price;
        this.numCars = numCars;
        this.numAvail = numCars;
    }

    private Cars(String location, int price, int numCars, int numAvail) {
        this.location = location;
        this.price = price;
        this.numCars = numCars;
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

    public int getNumCars() {
        return numCars;
    }

    public void setNumCars(int numCars) {
        this.numCars = numCars;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setNumAvail(int numAvail) {
        this.numAvail = numAvail;
    }

    public void bookCars(int num) {
        this.numAvail -= num;
    }

    public void unbookCars(int num) {
        this.numAvail += num;
    }

    public void addCars(int num) {
        this.numCars += num;
        this.numAvail += num;
    }

    public void deleteCars(int num) {
        this.numCars -= num;
        this.numAvail -= num;
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
        return this.location;
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
        return new Cars(location, price, numCars, numAvail);
    }
}
