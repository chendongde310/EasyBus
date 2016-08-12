package com.renwey.emi.myrxbus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.renwey.emi.myrxbus.BaseActivity;
import com.renwey.emi.myrxbus.R;
import com.renwey.emi.myrxbus.bean.Lover;
import com.renwey.emi.rxbus.annotation.Produce;
import com.renwey.emi.rxbus.annotation.Tag;
import com.renwey.emi.rxbus.thread.EventThread;


public class MainActivity extends BaseActivity {


    private Button button2;
    private Button button3;
    private EditText editText;
    private Button button1;
    private Lover lover;
    private Lover randomLover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    @Override
    public void initView() {
        this.button1 = (Button) findViewById(R.id.button1);
        this.editText = (EditText) findViewById(R.id.editText);
        this.button3 = (Button) findViewById(R.id.button3);
        this.button2 = (Button) findViewById(R.id.button2);
    }

    @Override
    public void initListener() {
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lover = new Lover(editText.getText().toString(), 20);

                //过去看看
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (System.currentTimeMillis() % 2 == 0)
                    randomLover = new Lover("丑逼", (int) (Math.random() * 100));
                else randomLover = new Lover("美丽", (int) (Math.random() * 100));

                //过去看看
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });


        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "直接看代码", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 传递对象方法，方法名可以随意取，但必须添加@Produce 注释声明这个方法传递值
     *
     * @return 返回的值代表需要传递的对象
     */
    @Produce(thread = EventThread.MAIN_THREAD)
    public Lover postLoverForMe() {
        return this.lover;
    }


    /**
     * 传递带标签的对象方法，方法名可以随意取，但必须添加@Produce 注释声明这个方法传递值
     * thread = 线程控制
     * tags = 给对象申明标签，用于区分相同数据类型的不同对象 ，可以理解为Map里value的key
     *
     * @return 返回的值代表需要传递的对象
     */
    @Produce(
            thread = EventThread.MAIN_THREAD,
            tags = {
                    @Tag("random")
            }
    )
    public Lover postRandomLover() {
        return this.randomLover;
    }


}
