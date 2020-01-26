package net.pois0nbread.gflkeeper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <pre>
 *     author : Pois0nBread
 *     e-mail : pois0nbreads@gmail.com
 *     time   : 2020/01/26
 *     desc   : MainActivity
 *     version: 2.0
 * </pre>
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SharedPreferences sharedPreferences = null;

    Switch mSwitch1 = null;
    TextView mTextView1 = null;
    TextView mTextView2 = null;
    TextView mTextView3 = null;
    TextView mTextView4 = null;
    TextView mTextView5 = null;
    //
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("settings", Context.MODE_WORLD_WRITEABLE);
        bindView();
        if (!isXposed()) {
            mTextView5.setVisibility(View.VISIBLE);
            new AlertDialog.Builder(this)
                    .setTitle("模块未激活或未安装Xposed框架")
                    .setMessage("请先激活模块或安装Xposed框架后再运行软件")
                    .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPreferences.edit().putBoolean("dialog", false).apply();
                            dialog.dismiss();
                        }
                    }).create().show();
            return;
        }
        if (sharedPreferences.getBoolean("dialog", true)) alertDialog.show();
    }

    private void bindView() {
        mSwitch1 = (Switch) findViewById(R.id.main_switch);
        mSwitch1.setChecked(sharedPreferences.getBoolean("enable", false));
        mTextView1 = (TextView) findViewById(R.id.main_button1);
        mTextView2 = (TextView) findViewById(R.id.main_button2);
        mTextView3 = (TextView) findViewById(R.id.main_button3);
        mTextView4 = (TextView) findViewById(R.id.main_button4);
        mTextView5 = (TextView) findViewById(R.id.main_textinfo);
        //
        mSwitch1.setOnCheckedChangeListener(this);
        mTextView1.setOnClickListener(this);
        mTextView2.setOnClickListener(this);
        mTextView3.setOnClickListener(this);
        mTextView4.setOnClickListener(this);
        //
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("使用说明（Bata）")
                .setMessage("功能启用后游戏分屏不会暂停" +
                        "\n" +
                        "\n注意事项：" +
                        "\n本软件会使锁屏暂停失效！注意暂停！" +
                        "\n（请先让游戏全屏再进入后台，否游戏会后台保持运行）" +
                        "\n" +
                        "\n目前已适配版本：官服, B服" +
                        "\n(本软件不修改游戏数据 但不保证不会被封号)" +
                        "\n" +
                        "\n祝您游戏愉快 _(:з」∠)_")
                .setNegativeButton("我晓得了,别烦我", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sharedPreferences.edit().putBoolean("dialog", false).apply();
                dialog.dismiss();
            }
        }).create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_button1:
                alertDialog.show();
                break;
            case R.id.main_button2:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/u/2108563")));
                break;
            case R.id.main_button3:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Pois0nBreads/GFLKeeper")));
                break;
            case R.id.main_button4:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://pois0nbreads.github.io/Breads/")));
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.main_switch:
                sharedPreferences.edit().putBoolean("enable", isChecked).apply();
                Toast.makeText(this, "重启游戏后生效", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean isXposed(){
        return false;
    }
}
