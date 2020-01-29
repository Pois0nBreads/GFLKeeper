package net.pois0nbread.gflkeeper;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2020/01/29
 *     desc   : HookImp
 *     version: 2.2
 * </pre>
 */

public class HookImp implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    //Context
    private  Activity mActivity = null;
    //游戏包名列表
    private String[] mGameList = {
            "com.digitalsky.girlsfrontline.cn", //---------------------------官服
            "com.digitalsky.girlsfrontline.cn.uc", //------------------------九游服
            "com.digitalsky.girlsfrontline.cn.huawei", //------------------华为服
            "com.digitalsky.girlsfrontline.cn.bili"}; //----------------------B服
    //UnityActivity列表
    private String[] mUnityPlayerActivityList = {
            "com.digitalsky.girlsfrontline.cn.UnityPlayerActivity", //---官服
            "com.unity3d.player.UnityPlayerActivity", //------------------九游服
            "com.unity3d.player.UnityPlayerActivity", //------------------华为服
            "com.unity3d.player.UnityPlayerActivity"}; //-----------------B服
    //MainActivityl列表
    private String[] mMainActivity = {
            "com.digitalsky.girlsfrontline.cn.UnityPlayerActivity", //---官服
            "com.digital.unity.MainActivity", //----------------------------九游服
            "com.digital.unity.MainActivity", //----------------------------华为服
            "com.digital.unity.MainActivity"}; //---------------------------B服
    //hook暂停标识符
    private  boolean wait_hook = false;
    //handleLoadPackage
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        try {
            if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
                XposedHelpers.findAndHookMethod("net.pois0nbread.gflkeeper.MainActivity", lpparam.classLoader, "isXposed", XC_MethodReplacement.returnConstant(true));
                return;
            }

            if (!Setting.getEnable()) return;

            final int listPos = isGamePackage(lpparam.packageName);
            if (listPos != -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //Start Hook
                XposedBridge.log("GFK : Start Hook Game Package Name is " + lpparam.packageName);

                //Hook activityList[listPos]->onStart()
                XposedBridge.log("GFK : Start Hook " + mMainActivity[listPos] + "->onStart()");
                XposedHelpers.findAndHookMethod(mMainActivity[listPos], lpparam.classLoader
                        , "onCreate", Bundle.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param) {
                                try {
                                    mActivity = (Activity) param.thisObject;
                                    XposedBridge.log("GFK : Hook " + mMainActivity[listPos] + "->onCreate() ：Succeed");
                                } catch (Exception e) {
                                    XposedBridge.log("GFK : Hook " + mMainActivity[listPos] + "->onCreate() ：Error\nErrorMessage : \n" + e.getMessage());
                                }
                            }
                        });
                XposedBridge.log("GFK : Start Hook " + mUnityPlayerActivityList[listPos] + "->onStop()");

                //Hook activityList[listPos]->onStop()
                XposedHelpers.findAndHookMethod(mUnityPlayerActivityList[listPos], lpparam.classLoader
                        , "onStop", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param) {
                                try {
                                    Class mUnityPlayerActivity = lpparam.classLoader.loadClass(mUnityPlayerActivityList[listPos]);
                                    Field mUnityPlayer = mUnityPlayerActivity.getDeclaredField("mUnityPlayer");
                                    mUnityPlayer.setAccessible(true);
                                    Class<?> cUnityPlayer = lpparam.classLoader.loadClass("com.unity3d.player.UnityPlayer");
                                    Method pause = cUnityPlayer.getDeclaredMethod("pause");
                                    Object tmUnityPlayer = mUnityPlayer.get(param.thisObject);
                                    //
                                    wait_hook = true;
                                    pause.invoke(tmUnityPlayer);
                                    //
                                    XposedBridge.log("GFK : Hook " + mUnityPlayerActivityList[listPos] + "->onStop() Succeed");
                                } catch (Exception e) {
                                    wait_hook = false;
                                    XposedBridge.log("GFK : Hook " + mUnityPlayerActivityList[listPos] + "->onStop() Error\nErrorMessage : \n" + e.getMessage());
                                }
                            }
                        });

                //Hook activityList[listPos]->onRestart()
                XposedBridge.log("GFK : Start Hook " + mMainActivity[listPos] + "->onRestart()");
                XposedHelpers.findAndHookMethod(mMainActivity[listPos], lpparam.classLoader
                        , "onRestart", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param) {
                                wait_hook = false;
                                XposedBridge.log("GFK : Hook " + mMainActivity[listPos] + "->onRestart() ：Succeed");
                            }
                        });

                //Hook com.unity3d.player.UnityPlayer->pause()
                XposedBridge.log("GFK : Start Hook com.unity3d.player.UnityPlayer->pause()");
                XposedHelpers.findAndHookMethod("com.unity3d.player.UnityPlayer", lpparam.classLoader
                        , "pause", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (mActivity == null) {
                                    XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->pause() : Abort (mActivity is null)");
                                    return;
                                }
                                if (!mActivity.isInMultiWindowMode() || wait_hook) {
                                    XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->pause() : Abort");
                                    return;
                                }
                                XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->pause() : Succeed");
                                param.setResult("none");
                            }
                        });

                //Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)
                XposedBridge.log("GFK : Start Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)");
                XposedHelpers.findAndHookMethod("com.unity3d.player.UnityPlayer", lpparam.classLoader
                        , "windowFocusChanged", boolean.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (mActivity == null) {
                                    XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean) : Abort (mActivity is null)");
                                    return;
                                }
                                if (!mActivity.isInMultiWindowMode()) {
                                    XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean) : Abort");
                                    return;
                                }
                                if (wait_hook) {
                                    XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)->args[0] = false ：Succeed");
                                    param.args[0] = false;
                                    return;
                                }
                                XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)->args[0] = true ：Succeed");
                                param.args[0] = true;
                            }
                        });

                XposedBridge.log("GFK : " + lpparam.packageName + " is Hooked");
            }
        } catch (Exception e) {
            XposedBridge.log("GFK : " + lpparam.packageName + "Hook Error\nErrorMessage : \n" + e.getMessage() + "");
        }
    }

    private int isGamePackage(String packageName) {
        for (int i = 0; i < mGameList.length; i++)
            if (packageName.equals(mGameList[i])) return i;
        return -1;
    }

    @Override
    public void initZygote(StartupParam startupParam) {
    }
}
