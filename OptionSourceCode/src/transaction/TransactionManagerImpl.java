package transaction;

import java.io.File;
import java.rmi.*;
import java.util.HashMap;
import java.util.HashSet;

/** 
 * Transaction Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl
    extends java.rmi.server.UnicastRemoteObject
    implements TransactionManager {
	private Integer xidCounter; // 准备不同的xid
	private String dieTime; // dieTime flag

	private HashMap<Integer, HashSet<ResourceManager>> RMs = new HashMap<>();
	private HashMap<Integer, String> xids = new HashMap<>();
	private HashMap<Integer, Integer> xidsToBeRecovered = new HashMap<>();

	//log path
	private String xidCounterPath = "xidCounter.log";
	private String xidsStatusPath = "xidsStatus.log";
	private String xidsToBeRecoveredPath = "xidsToBeRecovered.log";
	public TransactionManagerImpl() throws RemoteException {
		xidCounter = 1;
		dieTime = "noDie";
		recover();
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
	    TransactionManagerImpl obj = new TransactionManagerImpl();
	    Naming.rebind(rmiPort + TransactionManager.RMIName, obj);
	    System.out.println("TM bound");
	} 
	catch (Exception e) {
	    System.err.println("TM not bound:" + e);
	    System.exit(1);
	}
    }

	private void recover() {
		File dataDir = new File("data");
		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}

		Object xidCounterTmp = Util.loadObject("data/" + xidCounterPath);
		if (xidCounterTmp != null)
			xidCounter = (Integer) xidCounterTmp;

		Object xidsToDo = Util.loadObject("data/" + xidsToBeRecoveredPath);
		if (xidsToDo != null)
			xidsToBeRecovered = (HashMap<Integer, Integer>) xidsToDo;

		Object xidsTmp = Util.loadObject("data/" + xidsStatusPath);
		if (xidsTmp != null) {
			HashMap<Integer, String> xids_to_be_done = (HashMap<Integer, String>) xidsTmp;
			System.out.println("Redo logs");
			for (Integer xidTmp : xids_to_be_done.keySet()) {
				String[] vals = xids_to_be_done.get(xidTmp).split("_");
				String status = vals[0];
				int rm_num = Integer.parseInt(vals[1]);
				if (status.equals(COMMITTED)) {
					setRecoveryLater(xidTmp, rm_num);
				}
			}
			System.out.println("Finish redo logs.");
		}
	}
	private void setRecoveryLater(int xid, int num) {
		synchronized (xidsToBeRecovered) {
			if (xidsToBeRecovered.containsKey(xid)) {
				xidsToBeRecovered.put(xid, xidsToBeRecovered.get(xid) + num);
			} else {
				xidsToBeRecovered.put(xid, num);
			}
			Util.storeObject(xidsToBeRecovered, xidsToBeRecoveredPath);
		}
	}
    
	public void ping() throws RemoteException {
	}
    
	public String enlist(int xid, ResourceManager rm) throws RemoteException {
		if (xidsToBeRecovered.containsKey(xid)) {
			int num = xidsToBeRecovered.get(xid);
			synchronized (xidsToBeRecovered) {
				if (num > 1)
					xidsToBeRecovered.put(xid, num - 1);
//                else
//                    // do not remove this transaction id if rm dies after receiving the committed message.
//                    xidsToBeRecovered.remove(xid);
				Util.storeObject(xidsToBeRecovered, xidsToBeRecoveredPath);
			}
			return COMMITTED;
		}
		if (!xids.containsKey(xid)) {
			return ABORTED; // the xid has been aborted
		}
		synchronized (RMs) {
			if (!RMs.containsKey(xid))
				RMs.put(xid, new HashSet<>());
			HashSet<ResourceManager> xidRMs = RMs.get(xid);
			xidRMs.add(rm);
			synchronized (xids) {
				xids.put(xid, INITED + "_" + xidRMs.size());
				Util.storeObject(xids, "data/" + xidsStatusPath);
			}
		}
		return INITED;
	}

	@Override
	public int start() throws RemoteException {
		synchronized (xidCounter) {
			Integer newXid = xidCounter++;
			Util.storeObject(xidCounter, "data/" + xidCounterPath);

			synchronized (xids) {
				xids.put(newXid, INITED + "_" + 0);
				Util.storeObject(xids, "data/" + xidsStatusPath);
			}

			synchronized (RMs) {
				RMs.put(newXid, new HashSet<>());
			}

			return newXid;
		}
	}

	@Override
	public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		if (!xids.containsKey(xid))
			throw new TransactionAbortedException(xid, "TM");
		HashSet<ResourceManager> xidRMs = RMs.get(xid);
		// prepare
		synchronized (xids) {
			xids.put(xid, PREPARING + "_" + xidRMs.size());
			Util.storeObject(xids, "data/" + xidsStatusPath);
		}
		for (ResourceManager rm : xidRMs) {
			try {
				System.out.println("call rm prepare: " + xid + ": " + rm.getID());
				if (!rm.prepare(xid)) {//准备失败
					this.abort(xid);
					throw new TransactionAbortedException(xid, "RM aborted");
				}
			} catch (Exception e) {
				System.out.println("rm prepare failed: " + rm);
				e.printStackTrace();
				this.abort(xid);
				throw new TransactionAbortedException(xid, "RM aborted");
			}
		}
		if (dieTime.equals("BeforeCommit"))//test
			dieNow();

		synchronized (xids) {//xid记录一下
			xids.put(xid, COMMITTED + "_" + xidRMs.size());
			Util.storeObject(xids, "data/" + xidsStatusPath);
		}

		// test
		if (dieTime.equals("AfterCommit"))
			dieNow();

		// commit
		for (ResourceManager rm : xidRMs) {
			try {
				System.out.println("call rm commit " + xid + ": " + rm.getID());
				rm.commit(xid); // the function return means done signal.
			} catch (Exception e) {
				// rm dies before or during commit
				System.out.println("rm is down before commit: " + rm);
				// let the rm to be recovered when it is relaunched.
				setRecoveryLater(xid, 1);
			}
		}

		// commit log record + completion log record
		// do nothing. actually do not need completion log here because the failure of the following
		// codes can not be checked in our test condition.233
		synchronized (RMs) {
			// remove committed transactions
			RMs.remove(xid);
		}
		synchronized (xids) {
			xids.remove(xid);
			Util.storeObject(xids, "data/" + xidsStatusPath);
		}

		System.out.println("Commit xid: " + xid);
		// success
		return true;
	}

	@Override
	public void abort(int xid) throws RemoteException, InvalidTransactionException {
		if (!xids.containsKey(xid)) {
			throw new InvalidTransactionException(xid, "abort");
		}
		HashSet<ResourceManager> xidRMs = RMs.get(xid);
		for (ResourceManager rm : xidRMs) {
			try {
				System.out.println("call rm abort " + xid + " : " + rm.getID());
				rm.abort(xid);
				System.out.println("rm abort success: " + rm.getID());
			} catch (Exception e) {
				System.out.println("Some RM is down: " + rm);
			}
		}
		synchronized (RMs) {
			// remove aborted transactions
			RMs.remove(xid);
		}
		synchronized (xids) {
			if (xids.containsKey(xid)) {
				xids.remove(xid);
				Util.storeObject(xids, "data/" + xidsStatusPath);
			}
		}

		System.out.println("Abort xid: " + xid);
	}



    public boolean dieNow() 
	throws RemoteException {
	System.exit(1);
	return true;
    }

}
