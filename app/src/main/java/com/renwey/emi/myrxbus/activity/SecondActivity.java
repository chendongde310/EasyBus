package com.renwey.emi.myrxbus.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.renwey.emi.myrxbus.BaseActivity;
import com.renwey.emi.myrxbus.R;
import com.renwey.emi.myrxbus.bean.Lover;
import com.renwey.emi.rxbus.RxBus;
import com.renwey.emi.rxbus.annotation.Subscribe;
import com.renwey.emi.rxbus.annotation.Tag;
import com.renwey.emi.rxbus.thread.EventThread;

public class SecondActivity extends BaseActivity {

    private TextView text;
    private TextView text2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

    }

    @Override
    public void initView() {
        this.text = (TextView) findViewById(R.id.text);
        this.text2 = (TextView) findViewById(R.id.text2);
    }

    @Override
    public void initListener() {

    }

    /**
     * 接收参数的方法，方法名可以随意取，但必须添加@Subscribe 注释声明这个方法接收值
     */
    @Subscribe(thread = EventThread.MAIN_THREAD)
    public void getYoursLover(Lover hehe) {
        text.setText("接到的指定对象\n" + hehe.toString());
        Log.d("TAG","接到的指定对象");

    }

    /**
     * 接收参数的方法，方法名可以随意取，但必须添加@Subscribe 注释声明这个方法接收值
     */
    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag("random")
            }
    )
    public void getRandomLover(Lover hehe) {
        text2.setText("接到的随机对象\n" + hehe.toString());
        Log.d("TAG","接到的随机对象");
    }



}
