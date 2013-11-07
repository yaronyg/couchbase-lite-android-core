package android.database.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: yarong
 * Date: 11/6/13
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class SQLiteDatabase {
    private int version;

    public int getVersion() {
        return version;
    }

    public void close() {
        //To change body of created methods use File | Settings | File Templates.
    }

    public boolean isOpen() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public void beginTransaction() throws SQLException {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void setTransactionSuccessful() {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void endTransaction() throws SQLException  {
        //To change body of created methods use File | Settings | File Templates.
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) throws SQLException {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) throws SQLException {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public int delete(String table, String whereClause, String[] whereArgs) throws SQLException {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public long insert(String table, String nullColumnHack, ContentValues values) throws SQLException {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm) throws SQLException {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    public class CursorFactory {

    }

    public static final int CREATE_IF_NECESSARY = 0x10000000;

    public static final int CONFLICT_IGNORE = 4;

    public static final int CONFLICT_REPLACE = 5;

    public void execSQL(String query) throws SQLException {
        //To change body of created methods use File | Settings | File Templates.
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {

    }

    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) throws SQLiteException {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
