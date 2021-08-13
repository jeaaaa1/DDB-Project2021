package transaction;

import lockmgr.DeadlockException;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Collection;

/** 
 * Workflow Controller for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the WC.  In the real
 * implementation, the WC should forward calls to either RM or TM,
 * instead of doing the things itself.
 */

public class WorkflowControllerImpl
    extends java.rmi.server.UnicastRemoteObject
    implements WorkflowController {

    protected int flightcounter, flightprice, carscounter, carsprice, roomscounter, roomsprice; 
    protected int xidCounter;
    
    protected ResourceManager rmFlights = null;
    protected ResourceManager rmRooms = null;
    protected ResourceManager rmCars = null;
    protected ResourceManager rmCustomers = null;
    protected TransactionManager tm = null;
	private boolean flag=false;

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public static void main(String args[]) {
	System.setSecurityManager(new RMISecurityManager());

	String rmiPort = System.getProperty("rmiPort");
	if (rmiPort == null) {
	    rmiPort = "";
	} else if (!rmiPort.equals("")) {
	    rmiPort = "//:" + rmiPort + "/";
	}

	try {
	    WorkflowControllerImpl obj = new WorkflowControllerImpl();
	    Naming.rebind(rmiPort + WorkflowController.RMIName, obj);
	    System.out.println("WC bound");
	}
	catch (Exception e) {
	    System.err.println("WC not bound:" + e);
	    System.exit(1);
	}
    }
    
    
    public WorkflowControllerImpl() throws RemoteException {


	while (!reconnect()) {
	    // would be better to sleep a while
		try {
			if(flag){
				System.exit(1);
			}
			Thread.sleep(1000);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	} 
    }


    // TRANSACTION INTERFACE
    public int start()
	throws RemoteException {
		int xid= tm.start();

		return (xid);
    }

    public boolean commit(int xid)
	throws RemoteException, 
	       TransactionAbortedException, 
	       InvalidTransactionException {
		System.out.println("Committing");
		boolean tmRes=tm.commit(xid);
	return tmRes;
    }

    public void abort(int xid)
	throws RemoteException, 
               InvalidTransactionException {
    	tm.abort(xid);
	}


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item =null;
		try {
			item=rmFlights.query(xid,rmFlights.getID(),flightNum);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item!=null){
			return false;
		}else {
			if(flightNum==null||numSeats<0||price<0){
				return false;
			}else {
				Flights fly=new Flights(flightNum,price,numSeats);
				boolean res = false;
				try {
					res = rmFlights.update(xid,rmFlights.getID(),flightNum,fly);
				} catch (DeadlockException e) {
					e.printStackTrace();
					abort(xid);
					System.out.println("abort " +xid+ " because DeadlockExceotion.");
				}
				return res;
			}
		}

	}

    public boolean deleteFlight(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		Collection<ResourceItem> reser=null;
		try {
			reser=rmCustomers.query(xid,rmCustomers.TableReservation,"resvKey",flightNum);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		} catch (InvalidIndexException e) {
			e.printStackTrace();
		}
		if(reser!=null) {
			return false;
		}else {
			ResourceItem item= null;
			try {
				item = rmFlights.query(xid,rmFlights.getID(),flightNum);
			} catch (DeadlockException e) {
				e.printStackTrace();
				abort(xid);
				System.out.println("abort " +xid+ " because DeadlockExceotion.");
			}
			if(item==null){
				return false;
			}else {
				try {
					rmFlights.delete(xid,rmFlights.getID(),flightNum);
				} catch (DeadlockException e) {
					e.printStackTrace();
					abort(xid);
					System.out.println("abort " +xid+ " because DeadlockExceotion.");
				}
			}
		}
		return true;
	}
		
    public boolean addRooms(int xid, String location, int numRooms, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item =null;
		try {
			item=rmRooms.query(xid,rmRooms.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockException.");
		}
		if(item!=null){
			return false;
		}else {
			if(numRooms<0||price<0){
				return false;
			}else {
				Hotels room=new Hotels(location,price,numRooms);
				boolean res= false;
				try {
					res = rmRooms.update(xid,rmRooms.getID(),location,room);
				} catch (DeadlockException e) {
					e.printStackTrace();
					abort(xid);
					System.out.println("abort " +xid+ " because DeadlockExceotion.");
				}
				return res;
			}
		}
    }

    public boolean deleteRooms(int xid, String location, int numRooms) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		Hotels item= null;
		try {
			item = (Hotels) rmRooms.query(xid,rmRooms.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item==null) {
			return false;
		}else {
			if(numRooms<0||item.getNumAvail()<numRooms){
				return false;
			}else{
				item.deleteRooms(numRooms);
				try {
					rmRooms.update(xid,rmRooms.getID(),location,item);
				} catch (DeadlockException e) {
					e.printStackTrace();
					abort(xid);
					System.out.println("abort " +xid+ " because DeadlockExceotion.");
				}
			}
		}
		return true;
    }

    public boolean addCars(int xid, String location, int numCars, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item =null;
		try {
			item=rmCars.query(xid,rmCars.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockException.");
		}
		if(item!=null){
			return false;
		}else {
			if(numCars<0||price<0){
				return false;
			}else {
				Cars car=new Cars(location,price,numCars);
				boolean res= false;
				try {
					res = rmCars.update(xid,rmCars.getID(),location,car);
				} catch (DeadlockException e) {
					e.printStackTrace();
					abort(xid);
					System.out.println("abort " +xid+ " because DeadlockExceotion.");
				}
				return res;
			}
		}
    }

    public boolean deleteCars(int xid, String location, int numCars) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		Cars item= null;
		try {
			item = (Cars) rmCars.query(xid,rmCars.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item==null) {
			return false;
		}else {
			if(numCars<0||item.getNumAvail()<numCars){
				return false;
			}else{
				item.deleteCars(numCars);
				try {
					rmCars.update(xid,rmCars.getID(),location,item);
				} catch (DeadlockException e) {
					e.printStackTrace();
					abort(xid);
					System.out.println("abort " +xid+ " because DeadlockExceotion.");
				}
			}
		}
		return true;
    }

    public boolean newCustomer(int xid, String custName) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item=null;
		try {
			item=rmCustomers.query(xid,rmCustomers.getID(),custName);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item!=null){
			return false;
		}else {
			Customers cus=new Customers(custName);
			try {
				rmCustomers.insert(xid,rmCustomers.getID(),cus);
			} catch (DeadlockException e) {
				e.printStackTrace();
				abort(xid);
				System.out.println("abort " +xid+ " because DeadlockExceotion.");
			}
		}
		return true;
	}

    public boolean deleteCustomer(int xid, String custName) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item=null;
		try {
			item=rmCustomers.query(xid,rmCustomers.getID(),custName);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item==null){
			return false;
		}else {
			try {
				rmCustomers.delete(xid,rmCustomers.getID(),custName);
			} catch (DeadlockException e) {
				e.printStackTrace();
				abort(xid);
				System.out.println("abort " +xid+ " because DeadlockExceotion.");
			}
		}
		return true;
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
    	ResourceItem item=null;
		try {
			item=rmFlights.query(xid,rmFlights.getID(),flightNum);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item==null){
			return -1;
		}else {
			Flights fly=(Flights) item;
			return fly.getNumAvail();
		}
	}

    public int queryFlightPrice(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item=null;
		try {
			item=rmFlights.query(xid,rmFlights.getID(),flightNum);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item==null){
			return -1;
		}else {
			Flights fly=(Flights) item;
			return fly.getNumAvail();
		}
    }

    public int queryRooms(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item=null;
		try {
			item=rmRooms.query(xid,rmRooms.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item==null){
			return -1;
		}else {
			Hotels room=(Hotels) item;
			return room.getNumAvail();
		}
    }

    public int queryRoomsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item=null;
		try {
			item=rmRooms.query(xid,rmRooms.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item==null){
			return -1;
		}else {
			Hotels room=(Hotels) item;
			return room.getPrice();
		}
    }

    public int queryCars(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item=null;
		try {
			item=rmCars.query(xid,rmCars.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item==null){
			return -1;
		}else {
			Cars car=(Cars) item;
			return car.getNumAvail();
		}
    }

    public int queryCarsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem item=null;
		try {
			item=rmCars.query(xid,rmCars.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		}
		if(item==null){
			return -1;
		}else {
			Cars car=(Cars) item;
			return car.getPrice();
		}
    }

    public int queryCustomerBill(int xid, String custName)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		Collection<ResourceItem> item=null;
		int sum=0;
		try {
			item=rmCustomers.query(xid,rmCustomers.TableReservation,"custName",custName);
		} catch (DeadlockException e) {
			e.printStackTrace();
			abort(xid);
			System.out.println("abort " +xid+ " because DeadlockExceotion.");
		} catch (InvalidIndexException e) {
			e.printStackTrace();
		}
		if(item==null){
			return sum;
		}else {
			for(ResourceItem r:item){
				Reservation rs=(Reservation) r;
				sum+=rs.getPrice();
			}
		}
		return sum;
	}


    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem cus=null;
		ResourceItem fly=null;
		try {
			cus=rmCustomers.query(xid,rmCustomers.getID(),custName);
			fly=rmFlights.query(xid,rmFlights.getID(),flightNum);
		} catch (DeadlockException e) {
			e.printStackTrace();
		}
		if(cus==null||fly==null){
			return false;
		}else {
			Flights f=(Flights)fly;
			Reservation res=new Reservation(custName,Reservation.RESERVATION_TYPE_FLIGHT,flightNum,f.getPrice());
			f.resverSeats(1);
			try {
				rmCustomers.insert(xid,rmCustomers.TableReservation,res);
				rmFlights.update(xid,rmFlights.getID(),flightNum,f);
			} catch (DeadlockException e) {
				e.printStackTrace();
				abort(xid);
				System.out.println("abort " +xid+ " because DeadlockExceotion.");
			}
		}
		return true;
    }
 
    public boolean reserveCar(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem cus=null;
		ResourceItem car =null;
		try {
			cus=rmCustomers.query(xid,rmCustomers.getID(),custName);
			car =rmCars.query(xid,rmCars.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
		}
		if(cus==null|| car ==null){
			return false;
		}else {
			Cars c=(Cars) car;
			Reservation res=new Reservation(custName,Reservation.RESERVATION_TYPE_FLIGHT,location,c.getPrice());
			c.resverSeats(1);
			try {
				rmCustomers.insert(xid,rmCustomers.TableReservation,res);
				rmCars.update(xid,rmCars.getID(),location,c);
			} catch (DeadlockException e) {
				e.printStackTrace();
				abort(xid);
				System.out.println("abort " +xid+ " because DeadlockExceotion.");
			}
		}
		return true;
    }

    public boolean reserveRoom(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
		ResourceItem cus=null;
		ResourceItem room =null;
		try {
			cus=rmCustomers.query(xid,rmCustomers.getID(),custName);
			room =rmRooms.query(xid,rmRooms.getID(),location);
		} catch (DeadlockException e) {
			e.printStackTrace();
		}
		if(cus==null|| room ==null){
			return false;
		}else {
			Hotels r=(Hotels) room;
			Reservation res=new Reservation(custName,Reservation.RESERVATION_TYPE_FLIGHT,location,r.getPrice());
			r.resverSeats(1);
			try {
				rmCustomers.insert(xid,rmCustomers.TableReservation,res);
				rmRooms.update(xid,rmRooms.getID(),location,r);
			} catch (DeadlockException e) {
				e.printStackTrace();
				abort(xid);
				System.out.println("abort " +xid+ " because DeadlockExceotion.");
			}
		}
		return true;
    }

    // TECHNICAL/TESTING INTERFACE
    public boolean reconnect()
	throws RemoteException {
	String rmiPort = System.getProperty("rmiPort");
	if (rmiPort == null) {
	    rmiPort = "";
	} else if (!rmiPort.equals("")) {
	    rmiPort = "//:" + rmiPort + "/";
	}

	try {
	    rmFlights =
		(ResourceManager)Naming.lookup(rmiPort +
					       ResourceManager.RMINameFlights);
	    System.out.println("WC bound to RMFlights");
	    rmRooms =
		(ResourceManager)Naming.lookup(rmiPort +
					       ResourceManager.RMINameRooms);
	    System.out.println("WC bound to RMRooms");
	    rmCars =
		(ResourceManager)Naming.lookup(rmiPort +
					       ResourceManager.RMINameCars);
	    System.out.println("WC bound to RMCars");
	    rmCustomers =
		(ResourceManager)Naming.lookup(rmiPort +
					       ResourceManager.RMINameCustomers);
	    System.out.println("WC bound to RMCustomers");
	    tm =
		(TransactionManager)Naming.lookup(rmiPort +
						  TransactionManager.RMIName);
	    System.out.println("WC bound to TM");
	} 
	catch (Exception e) {
	    System.err.println("WC cannot bind to some component:" + e);
	    return false;
	}

	try {
	    if (rmFlights.reconnect() && rmRooms.reconnect() &&
		rmCars.reconnect() && rmCustomers.reconnect()) {
		return true;
	    }
	} catch (Exception e) {
	    System.err.println("Some RM cannot reconnect:" + e);
	    return false;
	}

	return false;
    }

    public boolean dieNow(String who)
	throws RemoteException {
	if (who.equals(TransactionManager.RMIName) ||
	    who.equals("ALL")) {
	    try {
		tm.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(ResourceManager.RMINameFlights) ||
	    who.equals("ALL")) {
	    try {
	    	rmFlights.setFlag(true);
		rmFlights.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(ResourceManager.RMINameRooms) ||
	    who.equals("ALL")) {
	    try {
			rmRooms.setFlag(true);
		rmRooms.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(ResourceManager.RMINameCars) ||
	    who.equals("ALL")) {
	    try {
			rmCars.setFlag(true);
		rmCars.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(ResourceManager.RMINameCustomers) ||
	    who.equals("ALL")) {
	    try {
			rmCustomers.setFlag(true);
		rmCustomers.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(WorkflowController.RMIName) ||
	    who.equals("ALL")) {
		setFlag(true);
	    System.exit(1);
	}
	return true;
    }
	public boolean dieWho(String who,String time) throws RemoteException {
    	if(who.equals(ResourceManager.RMINameCars)){
    		rmCars.setDieTime(time);
		}else if(who.equals(ResourceManager.RMINameCustomers)){
			rmCustomers.setDieTime(time);
		}else if(who.equals(ResourceManager.RMINameFlights)){
    		rmFlights.setDieTime(time);
		}else if(who.equals(ResourceManager.RMINameRooms)){
    		rmRooms.setDieTime(time);
		}else {

    		return false;
		}
    	return true;
	}

    public boolean dieRMAfterEnlist(String who)
	throws RemoteException {
		boolean res=dieWho(who,DieSituation.AfterEnlist.toString());
	return res;
    }
    public boolean dieRMBeforePrepare(String who)
	throws RemoteException {
		boolean res=dieWho(who,DieSituation.BeforePrepare.toString());
		return res;
    }
    public boolean dieRMAfterPrepare(String who)
	throws RemoteException {
		boolean res=dieWho(who,DieSituation.AfterPrepare.toString());
		return res;
    }
    public boolean dieTMBeforeCommit()
	throws RemoteException {
		boolean res=tm.setDieTime(DieSituation.BeforeCommit.toString());
		return res;
    }
    public boolean dieTMAfterCommit()
	throws RemoteException {
		boolean res=tm.setDieTime(DieSituation.AfterCommit.toString());
		return res;
    }
    public boolean dieRMBeforeCommit(String who)
	throws RemoteException {
		boolean res=dieWho(who,DieSituation.BeforeCommit.toString());
		return res;
    }
    public boolean dieRMBeforeAbort(String who)
	throws RemoteException {
		boolean res=dieWho(who,DieSituation.BeforeAbort.toString());
		return res;
    }
}
