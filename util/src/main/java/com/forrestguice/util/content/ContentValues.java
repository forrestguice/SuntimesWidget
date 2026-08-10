package com.forrestguice.util.content;

import com.forrestguice.annotation.Nullable;

import java.util.Set;

/**
 * mirrors android.content.ContentValues
 */
public interface ContentValues
{
    ContentValues newInstance();
    Object getNativeObject();

    boolean containsKey(String key);
    Set<String> keySet();
    int size();

    void clear();
    void remove(String key);
    boolean isEmpty();

    void putNull(java.lang.String key);
    void putAll(ContentValues other);
    void put(String key, @Nullable String value);
    void put(String key, @Nullable Byte value);
    void put(String key, @Nullable Integer value);
    void put(String key, @Nullable Long value);
    void put(String key, @Nullable Float value);
    void put(String key, @Nullable Short value);
    void put(String key, @Nullable Double value);
    void put(String key, @Nullable Boolean value);

    @Nullable
    Object get(String key);

    @Nullable
    String getAsString(String key);

    @Nullable
    Long getAsLong(String key);

    @Nullable
    Boolean getAsBoolean(String key);

    @Nullable
    Integer getAsInteger(String key);

    @Nullable
    Double getAsDouble(String key);

    @Nullable
    Byte getAsByte(String key);

    @Nullable
    Float getAsFloat(String key);

    @Nullable
    Short getAsShort(String key);
}
