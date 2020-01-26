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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SharedPreferences sharedPreferences = null;

    Switch mSwitch1 = null;
    TextView mTextView1 = null;
    TextView mTextView2 = null;
    TextView mTextView3 = null;
    //
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("settings", Context.MODE_WORLD_WRITEABLE);
        bindView();
        if (sharedPreferences.getBoolean("dialog", true)) alertDialog.show();
    }

    private void bindView() {
        mSwitch1 = (Switch) findViewById(R.id.main_switch);
        mSwitch1.setChecked(sharedPreferences.getBoolean("enable", false));
        mTextView1 = (TextView) findViewById(R.id.main_button1);
        mTextView2 = (TextView) findViewById(R.id.main_button2);
        mTextView3 = (TextView) findViewById(R.id.main_button3);
        //
        mSwitch1.setOnCheckedChangeListener(this);
        mTextView1.setOnClickListener(this);
        mTextView2.setOnClickListener(this);
        mTextView3.setOnClickListener(this);
        //
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("使用说明")
                .setMessage("本软件会使锁屏暂停失效！注意暂停！\n（请先退出分屏模式再让游戏进入后台，\n否在可能会后台运行）\n\n目前已适配版本：官服, B服")
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
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://pois0nbreads.github.io/camerachanger/404.html")));
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
}
