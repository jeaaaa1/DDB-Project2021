/*
 * Created on 2005-5-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package transaction;

import java.io.Serializable;

/**
 * @author RAdmin
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Reservation implements ResourceItem, Serializable {
	public static final String INDEX_CUSTNAME = "custName";

	public static final int RESERVATION_TYPE_FLIGHT = 1;

	public static final int RESERVATION_TYPE_HOTEL = 2;

	public static final int RESERVATION_TYPE_CAR = 3;

	protected String custName;

	protected int resvType;

	protected String resvKey;

	protected boolean isdeleted = false;

	private int price;

	public Reservation(String custName, int resvType, String resvKey,int price) {
		this.custName = custName;
		this.resvType = resvType;
		this.resvKey = resvKey;
		this.price=price;
	}

	public String[] getColumnNames() {
		return new String[] { "custName", "resvType", "resvKey" };
	}

	public String[] getColumnValues() {
		return new String[] { custName, "" + resvType, "" + resvKey };
	}

	public Object getIndex(String indexName) throws InvalidIndexException {
		if (indexName.equals(INDEX_CUSTNAME))
			return custName;
		else
			return resvKey;
	}

	public Object getKey() {
		return new ReservationKey(custName, resvType, resvKey);
	}

	/**
	 * @return Returns the custName.
	 */
	public String getCustName() {
		return custName;
	}

	public int getPrice() {
		return price;
	}

	/**
	 * @return Returns the resvKey.
	 */
	public String getResvKey() {
		return resvKey;
	}

	/**
	 * @return Returns the resvType.
	 */
	public int getResvType() {
		return resvType;
	}

	public boolean isDeleted() {
		return isdeleted;
	}

	public void delete() {
		isdeleted = true;
	}

	public Object clone() {
		Reservation o = new Reservation(getCustName(), getResvType(),
				getResvKey(),getPrice());
		o.isdeleted = isdeleted;
		return o;
	}
}