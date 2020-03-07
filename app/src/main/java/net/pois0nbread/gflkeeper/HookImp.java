package net.pois0nbread.gflkeeper;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2020/05/21
 *     desc   : HookImp.java
 *     version: 4.0
 * </pre>
 */

public class HookImp implements IXposedHookLoadPackage {

    //Context
    private Activity mActivity = null;
    private Setting mSetting = null;
    //UnityActivity列表
    private String[] mUnityPlayerActivityList = {
            "com.digitalsky.girlsfrontline.cn.UnityPlayerActivity", //---官服(老)
            "com.sunborn.girlsfrontline.UnityPlayerActivity", //--------官服(新)
            "com.unity3d.player.UnityPlayerActivity"}; //-----------------混服
    //hook暂停标识符
    private boolean wait_hook = false;

    //handleLoadPackage
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam mLoadPackageParam) {

        try {
            if (mLoadPackageParam.packageName.equals(BuildConfig.APPLICATION_ID)) {
                XposedHelpers.findAndHookMethod("net.pois0nbread.gflkeeper.MainActivity", mLoadPackageParam.classLoader, "isXposed", XC_MethodReplacement.returnConstant(true));
                return;
            }

            final int listPos = isGamePackage(mLoadPackageParam.packageName);
            if (listPos != -1) {
                //isEnable
                XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        mSetting = new Setting((Context) param.args[0]);
                        if (mSetting.getEnable()) {
                            //Start Package
                            XposedBridge.log("GFK : Start Hook Game Package Name is " + mLoadPackageParam.packageName);
                            initHook(listPos, mLoadPackageParam);
                        } else {
                            XposedBridge.log("GFK : Hook Abort " + mLoadPackageParam.packageName + " is not Enable");
                        }
                    }
                });
            }
        } catch (Exception e) {
            XposedBridge.log("GFK : " + mLoadPackageParam.packageName + "Hook Error\nErrorMessage : \n" + e.getMessage() + "");
        }
    }

    //initHook
    private void initHook(int listPos, XC_LoadPackage.LoadPackageParam mLoadPackageParam) {
        //Start Hook

        //Hook mUnityPlayerActivityList[listPos]->onCreate()
        XposedBridge.log("GFK : Start Hook " + mUnityPlayerActivityList[listPos] + "->onCreate()");
        XposedHelpers.findAndHookMethod(mUnityPlayerActivityList[listPos], mLoadPackageParam.classLoader
                , "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) {
                        try {
                            mActivity = (Activity) param.thisObject;
                            XposedBridge.log("GFK : Hook " + mUnityPlayerActivityList[listPos] + "->onCreate() ：Succeed");
                        } catch (Exception e) {
                            XposedBridge.log("GFK : Hook " + mUnityPlayerActivityList[listPos] + "->onCreate() ：Error\nErrorMessage : \n" + e.getMessage());
                        }
                    }
                });

        //Hook mUnityPlayerActivityList[listPos]->onStop()
        XposedBridge.log("GFK : Start Hook " + mUnityPlayerActivityList[listPos] + "->onStop()");
        XposedHelpers.findAndHookMethod(Activity.class, "onStop", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) {
                if (!param.thisObject.equals(mActivity)) return;
                try {
                    Class mUnityPlayerActivity = mLoadPackageParam.classLoader.loadClass(mUnityPlayerActivityList[listPos]);
                    Field mUnityPlayer = mUnityPlayerActivity.getDeclaredField("mUnityPlayer");
                    mUnityPlayer.setAccessible(true);
                    Class<?> cUnityPlayer = mLoadPackageParam.classLoader.loadClass("com.unity3d.player.UnityPlayer");
                    Method pause = cUnityPlayer.getDeclaredMethod("pause");
                    Object tmUnityPlayer = mUnityPlayer.get(param.thisObject);
                    //
                    wait_hook = true;
                    pause.invoke(tmUnityPlayer);
                } catch (Exception e) {
                    wait_hook = false;
                    XposedBridge.log("GFK : Hook " + mUnityPlayerActivityList[listPos] + "->onStop() Error\nErrorMessage : \n" + e.getMessage());
                }
            }
        });

        //Hook mUnityPlayerActivityList[listPos]->onRestart()
        XposedBridge.log("GFK : Start Hook " + mUnityPlayerActivityList[listPos] + "->onRestart()");
        XposedHelpers.findAndHookMethod(Activity.class, "onRestart", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) {
                if (!param.thisObject.equals(mActivity)) return;
                wait_hook = false;
            }
        });

        //Hook com.unity3d.player.UnityPlayer->pause()
        XposedBridge.log("GFK : Start Hook com.unity3d.player.UnityPlayer->pause()");
        XposedHelpers.findAndHookMethod("com.unity3d.player.UnityPlayer", mLoadPackageParam.classLoader
                , "pause", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (mActivity == null) return;
                        if (!mActivity.isInMultiWindowMode() || wait_hook) return;
                        param.setResult("none");
                    }
                });

        //Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)
        XposedBridge.log("GFK : Start Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)");
        XposedHelpers.findAndHookMethod("com.unity3d.player.UnityPlayer", mLoadPackageParam.classLoader
                , "windowFocusChanged", boolean.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (mActivity == null) return;
                        if (!mActivity.isInMultiWindowMode()) return;
                        if (wait_hook) {
                            param.args[0] = false;
                            return;
                        }
                        param.args[0] = true;
                    }
                });

        XposedBridge.log("GFK : " + mLoadPackageParam.packageName + " is Hooked");
    }

    private int isGamePackage(String packageName) {
        if (packageName.equals("com.digitalsky.girlsfrontline.cn")) return 0;
        if (packageName.equals("com.sunborn.girlsfrontline.cn")) return 1;
        if (packageName.matches(".*com.digitalsky.girlsfrontline.cn.*")) return 2;
        return -1;
    }
}
