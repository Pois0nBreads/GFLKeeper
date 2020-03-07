package net.pois0nbread.gflkeeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

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
 *     time   : 2020/03/07
 *     desc   : HookImp
 *     version: 3.3
 * </pre>
 */

public class HookImp implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    //TimeFormater
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    //Context
    private Activity mActivity = null;
    //游戏包名列表
    private String[] mGameList = {
            "com.digitalsky.girlsfrontline.cn", //---------------------------官服
            "com.digitalsky.girlsfrontline.cn.uc", //------------------------九游服
            "com.digitalsky.girlsfrontline.cn.huawei", //------------------华为服
            "com.digitalsky.girlsfrontline.cn.bili",  //----------------------B服
            "com.digitalsky.girlsfrontline.cn.mi"}; //-----------------------小米服
    //UnityActivity列表
    private String[] mUnityPlayerActivityList = {
            "com.digitalsky.girlsfrontline.cn.UnityPlayerActivity", //---官服
            "com.unity3d.player.UnityPlayerActivity", //------------------九游服
            "com.unity3d.player.UnityPlayerActivity", //------------------华为服
            "com.unity3d.player.UnityPlayerActivity", //------------------B服
            "com.unity3d.player.UnityPlayerActivity"}; //-----------------小米服
    //MainActivityl列表
    private String[] mMainActivity = {
            "com.digitalsky.girlsfrontline.cn.UnityPlayerActivity", //---官服
            "com.digital.unity.MainActivity", //----------------------------九游服
            "com.digital.unity.MainActivity", //----------------------------华为服
            "com.digital.unity.MainActivity", //----------------------------B服
            "com.digital.unity.MainActivity"}; //---------------------------小米服
    //hook暂停标识符
    private boolean wait_hook = false;

    //handleLoadPackage
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {

        try {

            if (lpparam.packageName.equals("com.digitalsky.girlsfrontline.cn.bili")) {
                if (Setting.getBiliModeEnable()) intoBili(lpparam);
            }

            if (lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) {
                XposedHelpers.findAndHookMethod("net.pois0nbread.gflkeeper.MainActivity", lpparam.classLoader, "isXposed", XC_MethodReplacement.returnConstant(true));
                return;
            }

            final int listPos = isGamePackage(lpparam.packageName);
            if (listPos != -1) {
                //Start Package
                XposedBridge.log("GFK : Start Hook Game Package Name is " + lpparam.packageName);
                //isEnable
                if (Setting.getEnable()) {
                    initHook(Setting.getAllModeEnable(), listPos, lpparam);
                } else {
                    XposedBridge.log("GFK : Hook Abort " + lpparam.packageName + " is not Enable");
                }
            }
        } catch (Exception e) {
            XposedBridge.log("GFK : " + lpparam.packageName + "Hook Error\nErrorMessage : \n" + e.getMessage() + "");
        }
    }

    //initHook
    private void initHook(boolean AllMode, int listPos, XC_LoadPackage.LoadPackageParam lpparam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Start Hook
            XposedBridge.log("GFK : Start Hook AllMode is " + AllMode);
            if (AllMode) {
                //All Mode
                //Hook com.unity3d.player.UnityPlayer->pause()
                XposedBridge.log("GFK : Start Hook com.unity3d.player.UnityPlayer->pause()");
                XposedHelpers.findAndHookMethod("com.unity3d.player.UnityPlayer", lpparam.classLoader
                        , "pause", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
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
                                XposedBridge.log("GFK : Hook com.unity3d.player.UnityPlayer->windowFocusChanged(boolean)->args[0] = true ：Succeed");
                                param.args[0] = true;
                            }
                        });
                XposedBridge.log("GFK : " + lpparam.packageName + " is Hooked");
            } else {
                //No All Mode
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
        } else {
            //Hook Abort (LowAPI)
            XposedBridge.log("GFK : Hook Abort : Target is Low Api (OS Api < N)");
        }
    }

    private int isGamePackage(String packageName) {
        for (int i = 0; i < mGameList.length; i++)
            if (packageName.equals(mGameList[i])) return i;
        return -1;
    }

    SharedPreferences mPreferences = null;

    //UnYouth model
    private void intoBili(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("GFK : Start Hook Youth Model");

        //Hook com.digital.unity.SplashActivity->onCreate(Bundle)
        XposedBridge.log("GFK : Start Hook com.digital.unity.SplashActivity->onCreate(Bundle)");
        XposedHelpers.findAndHookMethod("com.digital.unity.SplashActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mPreferences = ((Context) param.thisObject).getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
                XposedBridge.log("GFK : Get Context for SharedPreferences Succeed");
            }
        });

        //Hook com.digital.cloud.UserCenter->LoginResponseThirdPart(int, int, String, String, String, String, String, String, String)
        XposedBridge.log("GFK : Start Hook com.digital.cloud.UserCenter->LoginResponseThirdPart(int, int, String, String, String, String, String, String, String)");
        XposedHelpers.findAndHookMethod("com.digital.cloud.UserCenter", lpparam.classLoader, "LoginResponseThirdPart"
                , int.class, int.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if (((String) param.args[8]).equals("hooked")) {
                            param.args[8] = "";
                            XposedBridge.log("GFK : UnYouth model Login Succeed");
                            return;
                        }
                        if ((int) param.args[0] == 0 && (int) param.args[1] == 10047) {
                            XposedBridge.log("GFK : Get LoginResponseThirdPart _ uid = " + param.args[2]);
                            XposedBridge.log("GFK : Get LoginResponseThirdPart _ access_token = " + param.args[4]);
                            XposedBridge.log("GFK : Get LoginResponseThirdPart _ expire_times = " + param.args[5]);
                            XposedBridge.log("GFK : Get LoginResponseThirdPart _ refresh_token = " + param.args[6]);
                            mPreferences.edit().putString("last_login_time", mSimpleDateFormat.format(new Date())).commit();
                            mPreferences.edit().putString("uid", (String) param.args[2]).commit();
                            mPreferences.edit().putString("access_token", (String) param.args[4]).commit();
                            mPreferences.edit().putString("expire_times", (String) param.args[5]).commit();
                            mPreferences.edit().putString("refresh_token", (String) param.args[6]).commit();
                            XposedBridge.log("GFK : Get login information Succeed");
                        }
                    }
                });

        //Hook com.bsgamesdk.android.activity.Login_RegActivity->onCreate(Bundle)
        XposedBridge.log("GFK : Start Hook com.bsgamesdk.android.activity.Login_RegActivity->onCreate(Bundle)");
        XposedHelpers.findAndHookMethod("com.bsgamesdk.android.activity.Login_RegActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Activity activity = (Activity) param.thisObject;
                int id = activity.getResources().getIdentifier("bsgamesdk_title_content", "id", activity.getPackageName());
                String uid = mPreferences.getString("uid", "");
                String access_token = mPreferences.getString("access_token", "");
                String expire_times = mPreferences.getString("expire_times", "");
                String refresh_token = mPreferences.getString("refresh_token", "");
                AlertDialog alertDialog = new AlertDialog.Builder(activity)
                        .setCancelable(false)
                        .setMessage("uid : " + uid + "\n上次正常登录的时间是 : " + mPreferences.getString("last_login_time", "没有正常登录信息") + "\n请尽可能在每天第一次使用正常登录")
                        .setTitle("反防沉迷系统")
                        .setNeutralButton("删除最近登录信息", (dialog, which) -> {
                            mPreferences.edit().putString("last_login_time", "没有正常登录信息").commit();
                            mPreferences.edit().putString("uid", "").commit();
                            mPreferences.edit().putString("access_token", "").commit();
                            mPreferences.edit().putString("expire_times", "").commit();
                            mPreferences.edit().putString("refresh_token", "").commit();
                            dialog.dismiss();
                        })
                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("登录", (dialog, which) -> {
                            try {
                                XposedBridge.log("GFK : UnYouth model Login Start");
                                if (unYouthModelLogin(lpparam, uid, access_token, expire_times, refresh_token)) {
                                    Toast.makeText(activity, "登陆成功", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    activity.finish();
                                } else
                                    Toast.makeText(activity, "登陆失败：本地无最近登录信息\n请通过正常方式登录后再使用此功能", Toast.LENGTH_SHORT).show();
                                XposedBridge.log("GFK : UnYouthModelLogin Abort : Not Found Later Login Information");
                            } catch (Exception e) {
                                Toast.makeText(activity, "登陆失败：出现错误\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                XposedBridge.log("GFK : UnYouthModelLogin Error : " + e.getMessage());
                            }
                        }).create();
                activity.findViewById(id).setOnClickListener(v -> alertDialog.show());
                XposedBridge.log("GFK : Get Show UnYouth Model Login Dialog Succeed");
            }
        });
        XposedBridge.log("GFK : End Hook Youth Model");
    }

    //unYouthModelLogin return boolean
    private boolean unYouthModelLogin(XC_LoadPackage.LoadPackageParam lpparam, String uid, String access_token, String expire_times, String refresh_token) throws Exception {
        if (uid == "" || access_token == "" || expire_times == "" || refresh_token == "") return false;
        Class cUserCenter = XposedHelpers.findClass("com.digital.cloud.UserCenter", lpparam.classLoader);
        Method mLoginResponseThirdPart = cUserCenter.getDeclaredMethod("LoginResponseThirdPart", int.class, int.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class);
        Method mGetInstance = cUserCenter.getDeclaredMethod("getInstance");
        Object oUserCenter = mGetInstance.invoke(null);
        mLoginResponseThirdPart.invoke(oUserCenter, 0, 10047, uid, "", access_token, expire_times, refresh_token, "", "hooked");
        return true;
    }

    @Override
    public void initZygote(StartupParam startupParam) {
    }
}
