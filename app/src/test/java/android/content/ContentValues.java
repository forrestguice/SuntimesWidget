package android.content;

import java.util.HashMap;

/**
 * mocks ContentValues for non-instrumented tests
 */
public class ContentValues
{
    protected HashMap<String, String> stringValues = new HashMap<>();
    protected HashMap<String, Boolean> boolValues = new HashMap<>();
    protected HashMap<String, CharSequence> charSequenceValues = new HashMap<>();
    protected HashMap<String, Integer> intValues = new HashMap<>();
    protected HashMap<String, Double> doubleValues = new HashMap<>();
    protected HashMap<String, Long> longValues = new HashMap<>();
    protected HashMap<String, Short> shortValues = new HashMap<>();
    protected HashMap<String, Float> floatValues = new HashMap<>();

    public void put(String key, String value) {
        stringValues.put(key, value);
    }
    public String getAsString(String key) {
        return stringValues.containsKey(key) ? stringValues.get(key) : null;
    }

    public void put(String key, CharSequence value) {
        charSequenceValues.put(key, value);
    }
    public CharSequence getAsCharSequence(String key) {
        return charSequenceValues.containsKey(key) ? charSequenceValues.get(key) : null;
    }

    public void put(String key, Boolean value) {
        boolValues.put(key, value);
    }
    public Boolean getAsBoolean(String key) {
        return boolValues.containsKey(key) ? boolValues.get(key) : null;
    }

    public void put(String key, Integer value) {
        intValues.put(key, value);
    }
    public Integer getAsInteger(String key) {
        return intValues.containsKey(key) ? intValues.get(key) : null;
    }

    public void put(String key, Double value) {
        doubleValues.put(key, value);
    }
    public Double getAsDouble(String key) {
        return doubleValues.containsKey(key) ? doubleValues.get(key) : null;
    }

    public void put(String key, Long value) {
        longValues.put(key, value);
    }
    public Long getAsLong(String key) {
        return longValues.containsKey(key) ? longValues.get(key) : null;
    }

    public void put(String key, Short value) {
        shortValues.put(key, value);
    }
    public Short getAsShort(String key) {
        return shortValues.containsKey(key) ? shortValues.get(key) : null;
    }

    public void put(String key, Float value) { floatValues.put(key, value);
    }
    public Float getAsFloat(String key) {
        return floatValues.containsKey(key) ? floatValues.get(key) : null;
    }
}