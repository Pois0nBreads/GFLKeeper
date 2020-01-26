package net.pois0nbread.gflkeeper;

import de.robv.android.xposed.XSharedPreferences;

public class Setting {
    private static XSharedPreferences xSharedPreferences = null;
    public static XSharedPreferences getSharedPreferences() {
        if (xSharedPreferences == null) {
            xSharedPreferences = new XSharedPreferences("net.pois0nbread.gflkeeper", "settings");
            xSharedPreferences.makeWorldReadable();
        } else {
            xSharedPreferences.reload();
        }
        return xSharedPreferences;
    }
    public static boolean getEnable() {return getSharedPreferences().getBoolean("enable", false);}
}
