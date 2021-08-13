package transaction;

import java.rmi.*;

/** 
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface TransactionManager extends Remote {

    public static final String INITED = "inited";
    public static final String PREPARING = "preparing";
    public static final String COMMITTED = "committed";
    public static final String ABORTED = "aborted";

    public boolean dieNow()
	throws RemoteException;

    public void ping() throws RemoteException;
    
	public String enlist(int xid, ResourceManager rm) throws RemoteException;

    public int start()
            throws RemoteException;

	public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException;


    public boolean setDieTime(String time);
	
    /** The RMI name a TransactionManager binds to. */
    public static final String RMIName = "TM";
}
