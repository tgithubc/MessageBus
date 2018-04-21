package com.tgithubc.messagebus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tgithubc.messagebus.IObserver.ITest2Observer;
import com.tgithubc.messagebus.IObserver.ITest1Observer;
import com.tgithubc.messagebus.lib.MessageBus;
import com.tgithubc.messagebus.lib.message.Decorate;
import com.tgithubc.messagebus.lib.message.RunThread;

public class MainActivity extends AppCompatActivity implements ITest1Observer, ITest2Observer {

    private TextView mTest1, mTest2, mTest3;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTest1 = (TextView) findViewById(R.id.text1);
        mTest2 = (TextView) findViewById(R.id.text2);
        mTest3 = (TextView) findViewById(R.id.text3);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MessageBus.instance().getDefault(ITest2Observer.class).test21(222);
                MessageBus.instance().getDefault(ITest1Observer.class).test12("ITest1Observer test12");

                MessageBus.instance().getSticky(ITest1Observer.class).test11(111);
                MessageBus.instance().removeStickyMessage(ITest1Observer.class);
                MessageBus.instance().getSticky(ITest1Observer.class).test11("ITest1Observer test11");
                MessageBus.instance().getSticky(ITest2Observer.class).test21("ITest2Observer test21");

                // test 先发消息再注册
                MessageBus.instance().register(MainActivity.this);
            }
        });
        //MessageBus.instance().register(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageBus.instance().unRegister(this);
    }

    @Override
    @Decorate(isSticky = true)
    public void test11(String string) {
        final String testMsg = string + ",currentThread : " + Thread.currentThread().getName();
        Log.d("TestBus", "ob1:" + testMsg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTest1.setText(testMsg);
            }
        });
    }

    @Override
    @Decorate(isSticky = true)
    public void test21(String string) {
        Log.d("TestBus", "ob2:" + string + ",currentThread : " + Thread.currentThread().getName());
    }

    @Override
    public void test21(int string) {
        Log.d("TestBus", "ob2:" + string + ",currentThread : " + Thread.currentThread().getName());
    }

    @Override
    @Decorate(isSticky = true)
    public void test11(int string) {
        final String testMsg = string + ",currentThread : " + Thread.currentThread().getName();
        Log.d("TestBus", "ob1:" + testMsg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTest2.setText(testMsg);
            }
        });
    }

    @Override
    @Decorate(isSticky = true, runThread = RunThread.BACKGROUND)
    public void test12(String string) {
        final String testMsg = string + ",currentThread : " + Thread.currentThread().getName();
        Log.d("TestBus", "ob1:" + testMsg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTest3.setText(testMsg);
            }
        });
    }
}
