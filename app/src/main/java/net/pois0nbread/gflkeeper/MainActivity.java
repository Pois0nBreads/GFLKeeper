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
 *     time   : 2020/05/21
 *     desc   : MainActivity.java
 *     version: 4.0
 * </pre>
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SharedPreferences sharedPreferences = null;

    private Switch mEnableSwitch = null;
    //
    private AlertDialog mUse_Info_AlertDialog;
    private AlertDialog mPy_Pay_alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(Constall.Settings, Context.MODE_PRIVATE);
        bindView();
        if (!isXposed()) {
            new AlertDialog.Builder(this)
                    .setTitle("模块未激活或未安装Xposed框架")
                    .setMessage("请先激活模块或安装Xposed框架后再运行软件")
                    .setNegativeButton("确认", (dialog, which) -> {
                        sharedPreferences.edit().putBoolean("dialog", false).apply();
                        dialog.dismiss();
                    }).create().show();
            return;
        }
        if (sharedPreferences.getBoolean(Constall.Dialog, true)) mUse_Info_AlertDialog.show();
        if (sharedPreferences.getBoolean(Constall.FirstOpen, true)) {
            new AlertDialog.Builder(this)
                    .setTitle("来自作者的留言 ⁄(⁄ ⁄•⁄ω⁄•⁄ ⁄)⁄")
                    .setMessage("制作这个软件花费我不少时间，如果你喜欢这个项目，可以赞助我买瓶可乐\n⊙▽⊙")
                    .setNegativeButton("给作者打钱", (dialog, which) -> mPy_Pay_alertDialog.show())
                    .setPositiveButton("下次再说", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
            sharedPreferences.edit().putBoolean(Constall.FirstOpen, false).apply();
        }
    }

    private void bindView() {
        //
        mEnableSwitch = findViewById(R.id.main_enable_switch);
        mEnableSwitch.setChecked(sharedPreferences.getBoolean(Constall.Enable,false));
        //
        mEnableSwitch.setOnCheckedChangeListener(this);
        //
        findViewById(R.id.main_github_button).setOnClickListener(this);
        findViewById(R.id.main_use_info_button).setOnClickListener(this);
        findViewById(R.id.main_py_pay_button).setOnClickListener(this);
        findViewById(R.id.main_developer_info_button).setOnClickListener(this);
        findViewById(R.id.main_qq_group_button).setOnClickListener(this);
        //
        mUse_Info_AlertDialog = new AlertDialog.Builder(this)
                .setTitle("使用说明")
                .setMessage("功能启用后游戏分屏不会暂停" +
                        "\n" +
                        "\n注意事项：" +
                        "\n目前已适配版本：全部版本已适配" +
                        "\n(本软件不修改游戏数据 但不保证不会被封号)" +
                        "\n" +
                        "\n祝您游戏愉快 _(:з」∠)_")
                .setNegativeButton("我晓得了,别烦我", (dialog, which) -> {
                    sharedPreferences.edit().putBoolean(Constall.Dialog, false).apply();
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
            case R.id.main_qq_group_button:
                joinQQGroup("9UitjO-Id4O5Sj-wfbR3icMb76XcAQ57");
                return;
        }
        switch (v.getId()) {
            case R.id.dialog_button1:
                String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                        "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2Ffkx00694rmzfhta8chwcc3c%3F_s" +
                        "%3Dweb-other&_t=1472443966571#Intent;" +
                        "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
                try {
                    getPackageManager().getApplicationInfo("com.eg.android.AlipayGphone", 0);
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
        sharedPreferences.edit().putBoolean(Constall.Enable, isChecked).apply();
        Toast.makeText(this, "重启游戏后生效", Toast.LENGTH_SHORT).show();
    }

    public void joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "未安装手Q或安装的版本不支持", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isXposed(){
        return false;
    }
}
