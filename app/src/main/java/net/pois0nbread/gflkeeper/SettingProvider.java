package net.pois0nbread.gflkeeper;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2030/05/21
 *     desc   : SettingProvider.java
 *     version: 4.0
 * </pre>
 */

public class SettingProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"name", "value", "type"});
        if (getContext() == null) return null;
        Map<String, ?> map = getContext().getSharedPreferences(Constall.Settings, Context.MODE_PRIVATE).getAll();
        Set<String> krySet = map.keySet();
        for (String key : krySet) {
            Object[] rows = new Object[3];
            rows[0] = key;
            rows[1] = map.get(key);
            if (rows[1] instanceof Boolean) {
                rows[2]="boolean";
            }else if (rows[1] instanceof String) {
                rows[2]="string";
            }else if (rows[1] instanceof Integer) {
                rows[2]="int";
            }else if (rows[1] instanceof Long) {
                rows[2]="long";
            }else if (rows[1] instanceof Float) {
                rows[2]="float";
            }
            cursor.addRow(rows);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
