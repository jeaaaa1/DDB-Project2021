package transaction;

import java.io.*;

public class Util {
    public static boolean storeObject(Object o, String path) {
        File xidLog = new File(path);
        ObjectOutputStream objectOut = null;
        try {
            objectOut = new ObjectOutputStream(new FileOutputStream(xidLog));
            objectOut.writeObject(o);
            objectOut.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (objectOut != null)
                    objectOut.close();
            } catch (IOException e1) {
            }
        }
    }

    public static Object loadObject(String path) {
        File xidCounterLog = new File(path);
        ObjectInputStream objectIn = null;
        try {
            objectIn = new ObjectInputStream(new FileInputStream(xidCounterLog));
            return objectIn.readObject();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (objectIn != null)
                    objectIn.close();
            } catch (IOException e1) {
            }
        }
    }
}
