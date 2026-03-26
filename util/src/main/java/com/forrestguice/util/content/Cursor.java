package com.forrestguice.util.content;

/**
 * mirrors android.database.Cursor
 */
public interface Cursor extends java.io.Closeable
{
    int FIELD_TYPE_BLOB = 4;
    int FIELD_TYPE_FLOAT = 2;
    int FIELD_TYPE_INTEGER = 1;
    int FIELD_TYPE_NULL = 0;
    int FIELD_TYPE_STRING = 3;

    int getCount();
    int getPosition();

    boolean move(int i);
    boolean moveToPosition(int i);
    boolean moveToFirst();
    boolean moveToLast();
    boolean moveToNext();
    boolean moveToPrevious();

    boolean isFirst();
    boolean isLast();
    boolean isBeforeFirst();
    boolean isAfterLast();

    int getColumnIndex(java.lang.String s);
    int getColumnIndexOrThrow(java.lang.String s) throws java.lang.IllegalArgumentException;
    java.lang.String getColumnName(int i);
    java.lang.String[] getColumnNames();
    int getColumnCount();

    byte[] getBlob(int i);
    String getString(int i);
    short getShort(int i);
    int getInt(int i);
    long getLong(int i);
    float getFloat(int i);
    double getDouble(int i);
    int getType(int i);

    boolean isNull(int i);

    void close();
    boolean isClosed();
}
