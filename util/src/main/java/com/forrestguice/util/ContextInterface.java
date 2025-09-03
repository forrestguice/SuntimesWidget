package com.forrestguice.util;

public interface ContextInterface
{
    Resources getResources();
    String getString(int id);
    String getString(int id, Object... formatArgs);
    SharedPreferences getSharedPreferences(String name, int flags);
}
