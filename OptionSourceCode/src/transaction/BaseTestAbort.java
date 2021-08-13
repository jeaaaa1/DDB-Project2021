package transaction;

import java.rmi.Naming;

public class BaseTestAbort {
    public static void main(String args[]) {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        WorkflowController wc = null;
        try {
            wc = (WorkflowController) Naming.lookup(rmiPort + WorkflowController.RMIName);
            System.out.println("Bound to WC");
        } catch (Exception e) {
            System.err.println("Cannot bind to WC:" + e);
            System.exit(1);
        }

        try {
            int xid = wc.start();

            if (!wc.addFlight(xid, "999", 300, 500)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "SH", 500, 150)) {
                System.err.println("Add room failed");
            }
            if (!wc.addCars(xid, "SH", 200, 100)) {
                System.err.println("Add car failed");
            }
            if (!wc.newCustomer(xid, "LiGan")) {
                System.err.println("Add customer failed");
            }

            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

            xid = wc.start();
            System.out.println("Flight 999 has " +
                    wc.queryFlight(xid, "999") +
                    " seats.");
            System.out.println("Hotel SH has " +
                    wc.queryRooms(xid, "SH") +
                    " rooms");
            System.out.println("CarStation SH has " +
                    wc.queryCars(xid, "SH") +
                    " cars");

            if (!wc.reserveFlight(xid, "LiGan", "999")) {
                System.err.println("Reserve flight failed");
            }
            if (!wc.reserveRoom(xid, "LiGan", "SH")) {
                System.err.println("Reserve room failed");
            }
            if (!wc.reserveCar(xid, "LiGan", "SH")) {
                System.err.println("Reserve car failed");
            }
            System.out.println("Flight 999 has " +
                    wc.queryFlight(xid, "999") +
                    " seats.");
            System.out.println("Hotel SH has " +
                    wc.queryRooms(xid, "SH") +
                    " rooms");
            System.out.println("CarStation SH has " +
                    wc.queryCars(xid, "SH") +
                    " cars");
            wc.abort(xid);

            xid=wc.start();
            System.out.println("Flight 999 has " +
                    wc.queryFlight(xid, "999") +
                    " seats.");
            System.out.println("Hotel SH has " +
                    wc.queryRooms(xid, "SH") +
                    " rooms");
            System.out.println("CarStation SH has " +
                    wc.queryCars(xid, "SH") +
                    " cars");

        } catch (Exception e) {
            System.err.println("Received exception:" + e);
            System.exit(1);
        }

    }
}
