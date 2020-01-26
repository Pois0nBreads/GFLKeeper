package net.pois0nbread.gflkeeper;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookImp implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    Activity mActivity;
    String gameList[] = {
            "com.digitalsky.girlsfrontline.cn",
            "com.digitalsky.girlsfrontline.cn.bili"};
    String activityList[] = {
            "com.digitalsky.girlsfrontline.cn.UnityPlayerActivity",
            "com.digital.unity.MainActivity"};
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!Setting.getEnable()) return;
        final int listPos = isGamePackage(lpparam.packageName);
        if (listPos != -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            XposedHelpers.findAndHookMethod(activityList[listPos], lpparam.classLoader
                    , "onCreate", Bundle.class, new XC_MethodHook(){
                        @Override
                        protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                            mActivity = (Activity) param.thisObject;
                        }
                    });

            XposedHelpers.findAndHookMethod("com.unity3d.player.UnityPlayer", lpparam.classLoader
                    , "pause", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (mActivity == null) return;
                            if (!mActivity.isInMultiWindowMode()) return;
                            param.setResult("none");
                        }
                    });
            XposedHelpers.findAndHookMethod("com.unity3d.player.UnityPlayer", lpparam.classLoader
                    , "windowFocusChanged", boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (mActivity == null) return;
                            if (!mActivity.isInMultiWindowMode()) return;
                            param.args[0] = true;
                        }
                    });
        }
    }

    private int isGamePackage(String packageName){
        for (int i = 0;i < gameList.length; i++)
            if (packageName.equals(gameList[i])) return i;
        return -1;
    }

    @Override public void initZygote(StartupParam startupParam) {}
}
