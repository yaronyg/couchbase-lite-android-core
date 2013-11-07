package android.database;

/**
 * Created with IntelliJ IDEA.
 * User: yarong
 * Date: 11/6/13
 * Time: 3:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cursor {
    final public boolean moveToFirst() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public String getString(int column) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
    
    public void close() {
        
    }

    public int getInt(int column) {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public long getLong(int column) {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public byte[] getBlob(int column) {
        return new byte[0];  //To change body of created methods use File | Settings | File Templates.
    }

    public final boolean isAfterLast() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public final boolean moveToNext() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public int getCount() {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }
}
