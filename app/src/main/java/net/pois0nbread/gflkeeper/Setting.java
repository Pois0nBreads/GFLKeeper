package net.pois0nbread.gflkeeper;

import de.robv.android.xposed.XSharedPreferences;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2020/02/20
 *     desc   : Setting
 *     version: 3.1
 * </pre>
 */

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
    public static boolean getAllModeEnable() {return getSharedPreferences().getBoolean("all_mode", false);}
}
