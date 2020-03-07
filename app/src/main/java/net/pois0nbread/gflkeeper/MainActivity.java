package net.pois0nbread.gflkeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2020/03/07
 *     desc   : MainActivity
 *     version: 3.3
 * </pre>
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SharedPreferences sharedPreferences = null;

    Switch mEnableSwitch = null;
    Switch mAllModeSwitch = null;
    Switch mBiliModeSwitch = null;
    //
    AlertDialog mUse_Info_AlertDialog;
    AlertDialog mPy_Pay_alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("settings", Context.MODE_WORLD_READABLE);
        bindView();
        if (!isXposed()) {
            new AlertDialog.Builder(this)
                    .setTitle("模块未激活或未安装Xposed框架")
                    .setMessage("请先激活模块或安装Xposed框架后再运行软件")
                    .setNegativeButton("确认", (dialog, which) -> {
                        sharedPreferences.edit().putBoolean("dialog", false).commit();
                        dialog.dismiss();
                    }).create().show();
            return;
        }
        if (sharedPreferences.getBoolean("dialog", true)) mUse_Info_AlertDialog.show();
        if (sharedPreferences.getBoolean("first_open", true)) {
            new AlertDialog.Builder(this)
                    .setTitle("来自作者的留言 ⁄(⁄ ⁄•⁄ω⁄•⁄ ⁄)⁄")
                    .setMessage("制作这个软件花费我不少时间，如果你喜欢这个项目，可以赞助我买瓶可乐\n⊙▽⊙")
                    .setNegativeButton("给作者打钱", (dialog, which) -> mPy_Pay_alertDialog.show())
                    .setPositiveButton("下次再说", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
            sharedPreferences.edit().putBoolean("first_open", false).apply();
        }
    }

    private void bindView() {
        //
        mEnableSwitch = findViewById(R.id.main_enable_switch);
        mAllModeSwitch = findViewById(R.id.main_all_mode_switch);
        mBiliModeSwitch = findViewById(R.id.main_bili_mode_switch);
        mEnableSwitch.setChecked(sharedPreferences.getBoolean("enable", false));
        mAllModeSwitch.setChecked(sharedPreferences.getBoolean("all_mode", false));
        mBiliModeSwitch.setChecked(sharedPreferences.getBoolean("bili_mode", false));
        //
        mEnableSwitch.setOnCheckedChangeListener(this);
        mAllModeSwitch.setOnCheckedChangeListener(this);
        mBiliModeSwitch.setOnCheckedChangeListener(this);
        //
        findViewById(R.id.main_github_button).setOnClickListener(this);
        findViewById(R.id.main_use_info_button).setOnClickListener(this);
        findViewById(R.id.main_py_pay_button).setOnClickListener(this);
        findViewById(R.id.main_developer_info_button).setOnClickListener(this);
        //
        mUse_Info_AlertDialog = new AlertDialog.Builder(this)
                .setTitle("使用说明")
                .setMessage("功能启用后游戏分屏不会暂停" +
                        "\n" +
                        "\n注意事项：" +
                        "\n目前已适配版本：官服, B服, 华为服, 九游服, 小米服" +
                        "\n(本软件不修改游戏数据 但不保证不会被封号)" +
                        "\n" +
                        "\n3.2新增：B服反防沉迷（不保证不会被封号）" +
                        "\n" +
                        "\n祝您游戏愉快 _(:з」∠)_")
                .setNegativeButton("我晓得了,别烦我", (dialog, which) -> {
                    sharedPreferences.edit().putBoolean("dialog", false).apply();
                    dialog.dismiss();
                }).create();
        //
        View view = View.inflate(this, R.layout.dialog_layout, null);
        view.findViewById(R.id.dialog_button1).setOnClickListener(this);
        view.findViewById(R.id.dialog_button2).setOnClickListener(this);
        view.findViewById(R.id.dialog_button3).setOnClickListener(this);
        mPy_Pay_alertDialog = new AlertDialog.Builder(this).setView(view).setTitle("请选择赞助渠道 ⊙▽⊙").create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_use_info_button:
                mUse_Info_AlertDialog.show();
                return;
            case R.id.main_developer_info_button:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/u/2108563")));
                return;
            case R.id.main_github_button:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Pois0nBreads/GFLKeeper")));
                return;
            case R.id.main_py_pay_button:
                mPy_Pay_alertDialog.show();
                return;
        }
        switch (v.getId()) {
            case R.id.dialog_button1:
                String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                        "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2Ffkx00694rmzfhta8chwcc3c%3F_s" +
                        "%3Dweb-other&_t=1472443966571#Intent;" +
                        "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
                try {
                    getPackageManager().getApplicationInfo("com.eg.android.AlipayGphone", PackageManager.GET_UNINSTALLED_PACKAGES);
                    Intent intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "您似乎没有安装支付宝", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.dialog_button2:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/pois0nbread")));
                break;
            case R.id.dialog_button3:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://pois0nbreads.github.io/Breads/")));
                break;
        }
        mPy_Pay_alertDialog.dismiss();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.main_enable_switch:
                sharedPreferences.edit().putBoolean("enable", isChecked).commit();
                Toast.makeText(this, "重启游戏后生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_all_mode_switch:
                sharedPreferences.edit().putBoolean("all_mode", isChecked).commit();
                Toast.makeText(this, "重启游戏后生效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_bili_mode_switch:
                sharedPreferences.edit().putBoolean("bili_mode", isChecked).commit();
                Toast.makeText(this, "重启游戏后生效", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean isXposed(){
        return false;
    }
}
