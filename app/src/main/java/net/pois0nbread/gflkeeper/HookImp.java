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
 *     time   : 2020/01/27
 *     desc   : HookImp
 *     version: 2.1
 * </pre>
 */

public class HookImp implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    //Context
    Activity mActivity = null;
    //游戏包名列表
    String gameList[] = {
            "com.digitalsky.girlsfrontline.cn",
            "com.digitalsky.girlsfrontline.cn.bili"};
    //UnityActivity列表
    String activityList[] = {
            "com.digitalsky.girlsfrontline.cn.UnityPlayerActivity",
            "com.unity3d.player.UnityPlayerActivity"};
    //hook暂停标识符
    boolean wait_hook = false;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        try {
            if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
                XposedHelpers.findAndHookMethod("net.pois0nbread.gflkeeper.MainActivity", lpparam.classLoader, "isXposed", XC_MethodReplacement.returnConstant(true));
            }

            if (!Setting.getEnable()) return;
            final int listPos = isGamePackage(lpparam.packageName);
            if (listPos != -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                XposedBridge.log("GFK : Start Hook Game Package Name is " + lpparam.packageName);

                //Hook activityList[listPos]->onStart()
                XposedHelpers.findAndHookMethod(activityList[listPos], lpparam.classLoader
                        , "onStart", Bundle.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(final MethodHookParam param) {
                                try {

                                    XposedBridge.log("GFK : Hook mUnityPlayerActivity->onStart() ：Succeed\n");
                                } catch (Exception e) {
                                    XposedBridge.log("GFK : Hook mUnityPlayerActivity->onStart() ：Error\nErrorMessage : \n" + e.getMessage() + "\n");
                                }
                            }
                        });
                //Hook activityList[listPos]->onStop()
                XposedHelpers.findAndHookMethod(activityList[listPos], lpparam.classLoader
                        , "onStop", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param) {
                                try {
                                    Class mUnityPlayerActivity = lpparam.classLoader.loadClass(activityList[listPos]);
                                    Field mUnityPlayer = mUnityPlayerActivity.getDeclaredField("mUnityPlayer");
                                    mUnityPlayer.setAccessible(true);
                                    Class cUnityPlayer = lpparam.classLoader.loadClass("com.unity3d.player.UnityPlayer");
                                    Method pause = cUnityPlayer.getDeclaredMethod("pause");
                                    Object tmUnityPlayer = mUnityPlayer.get(param.thisObject);
                                    //
                                    wait_hook = true;
                                    pause.invoke(tmUnityPlayer);
                                    //
                                    XposedBridge.log("GFK : Hook mUnityPlayerActivity->onStop() Succeed\n");
                                } catch (Exception e) {
                                    wait_hook = false;
                                    XposedBridge.log("GFK : Hook mUnityPlayerActivity->onStop() Error\nErrorMessage : \n" + e.getMessage() + "\n");
                                }
                            }
                        });
                //Hook activityList[listPos]->onRestart()
                XposedHelpers.findAndHookMethod(activityList[listPos], lpparam.classLoader
                        , "onRestart", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(final MethodHookParam param) {
                                wait_hook = false;
                                XposedBridge.log("GFK : Hook mUnityPlayerActivity->onRestart() ：Succeed\n");
                            }
                        });
                //Hook com.unity3d.player.UnityPlayer->pause()
                XposedHelpers.findAndHookMethod("com.unity3d.player.UnityPlayer", lpparam.classLoader
                        , "pause", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (mActivity == null || !mActivity.isInMultiWindowMode() || wait_hook) {
                                    XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->pause() : Abort\n");
                                    return;
                                }
                                XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->pause() : Succeed\n");
                                param.setResult("none");
                            }
                        });
                //Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)
                XposedHelpers.findAndHookMethod("com.unity3d.player.UnityPlayer", lpparam.classLoader
                        , "windowFocusChanged", boolean.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                if (mActivity == null || !mActivity.isInMultiWindowMode()) {
                                    XposedBridge.log("GFK ：Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean) : Abort\n");
                                    return;
                                }
                                if (wait_hook) {
                                    XposedBridge.log("GFK ：Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)->args[0] = false ：Succeed\n");
                                    param.args[0] = false;
                                    return;
                                }
                                XposedBridge.log("GFK ：Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)->args[0] = true ：Succeed\n");
                                param.args[0] = true;
                            }
                        });
                XposedBridge.log("GFK : " + lpparam.packageName + "is Hooked\n");
            }
        } catch (Exception e) {
            XposedBridge.log("GFK : " + lpparam.packageName + "Hook Error\nErrorMessage : \n" + e.getMessage() + "\n");
        }
    }

    private int isGamePackage(String packageName) {
        for (int i = 0; i < gameList.length; i++)
            if (packageName.equals(gameList[i])) return i;
        return -1;
    }

    @Override
    public void initZygote(StartupParam startupParam) {
    }
}
