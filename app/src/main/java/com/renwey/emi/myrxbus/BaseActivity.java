package com.renwey.emi.myrxbus;

import android.app.Activity;
import android.os.Bundle;

import com.renwey.emi.rxbus.RxBus;

/**
 * 作者：陈东  —  www.renwey.com
 * 日期：2016/8/11 - 20:26
 * 注释： 你可以把注册方法写在BaseActivity里，但是你的数据会一直保存在rxbus里，因为你传的是Context是BaseActivity的上下文
 */
public abstract class BaseActivity extends Activity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //注册RxBus，会开寻找所有带@Produce注释的方法，并拿到数据
        RxBus.get().register(this);

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initView();
        initListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册，会清空数据池里的数据，如果你需要降低数据的持久性，就必须传递当前页面Activity的context
        RxBus.get().unregister(this);
    }

   public abstract void initView();

    public abstract void initListener();
}
