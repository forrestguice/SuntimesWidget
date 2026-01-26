package android.os;

import java.util.HashMap;

/**
 * mocks Bundle for non-instrumented tests
 */
@SuppressWarnings("ConstantConditions")
public class Bundle
{
    protected HashMap<String, String> stringValues = new HashMap<>();
    protected HashMap<String, Boolean> boolValues = new HashMap<>();
    protected HashMap<String, CharSequence> charSequenceValues = new HashMap<>();
    protected HashMap<String, Integer> intValues = new HashMap<>();
    protected HashMap<String, Double> doubleValues = new HashMap<>();
    protected HashMap<String, Long> longValues = new HashMap<>();

    public void putString(String key, String value) {
        stringValues.put(key, value);
    }
    public String getString(String key) {
        return getString(key, null);
    }
    public String getString(String key, String defValue) {
        return stringValues.containsKey(key) ? stringValues.get(key) : defValue;
    }

    public void putCharSequence(String key, CharSequence value) {
        charSequenceValues.put(key, value);
    }
    public CharSequence getCharSequence(String key) {
        return getCharSequence(key, null);
    }
    public CharSequence getCharSequence(String key, CharSequence defValue) {
        return charSequenceValues.containsKey(key) ? charSequenceValues.get(key) : defValue;
    }

    public void putBoolean(String key, boolean value) {
        boolValues.put(key, value);
    }
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    public boolean getBoolean(String key, boolean defValue) {
        return boolValues.containsKey(key) ? boolValues.get(key) : defValue;
    }

    public void putInt(String key, int value) {
        intValues.put(key, value);
    }
    public int getInt(String key) {
        return getInt(key, 0);
    }
    public int getInt(String key, int defValue) {
        return intValues.containsKey(key) ? intValues.get(key) : defValue;
    }

    public void putDouble(String key, double value) {
        doubleValues.put(key, value);
    }
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }
    public double getDouble(String key, double defValue) {
        return doubleValues.containsKey(key) ? doubleValues.get(key) : defValue;
    }

    public void putLong(String key, long value) {
        longValues.put(key, value);
    }
    public long getLong(String key) {
        return getLong(key, 0L);
    }
    public long getLong(String key, long defValue) {
        return longValues.containsKey(key) ? longValues.get(key) : defValue;
    }
}