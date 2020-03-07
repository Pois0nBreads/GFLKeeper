package net.pois0nbread.gflkeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2020/05/21
 *     desc   : Setting.java
 *     version: 4.0
 * </pre>
 */

public class Setting implements SharedPreferences{

    private Context mContext;

    Setting(Context context) {
        this.mContext = context;
    }

    boolean getEnable() {
        return getBoolean(Constall.Enable, false);
    }

    private Map<String, ?> getMap() {
        Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://net.pois0nbread.gflkeeper.SettingProvider/setting"), null, null, null, null);
        Map<String, Object> map = new HashMap<>();
        if (cursor == null) return map;
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String value = cursor.getString(cursor.getColumnIndex("value"));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            cursor.close();
            switch (type) {
                case "boolean":
                    map.put(name, Boolean.valueOf(value));
                    break;
                case "string":
                    map.put(name, value);
                    break;
                case "int":
                    map.put(name, Integer.parseInt(value));
                    break;
                case "long":
                    map.put(name, Long.parseLong(value));
                    break;
                case "float":
                    map.put(name, Float.parseFloat(value));
                    break;
            }
        }
        return map;
    }

    @Override
    public Map<String, ?> getAll() {
        return new HashMap<String, Object>(getMap());
    }

    @Override
    public String getString(String key, String defValue) {
        String v = (String) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override @SuppressWarnings("unchecked")
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Set<String> v = (Set<String>) getMap().get(key);
        return v != null ? v : defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        Integer v = (Integer) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        Long v = (Long) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Float v = (Float) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Boolean v = (Boolean) getMap().get(key);
        return v != null ? v : defValue;
    }

    @Override
    public boolean contains(String key) {
        return getMap().containsKey(key);
    }

    @Override
    public Editor edit() {
        return null;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }
}
