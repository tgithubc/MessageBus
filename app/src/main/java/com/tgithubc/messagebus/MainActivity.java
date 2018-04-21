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
                MessageBus.instance().get(ITest1Observer.class).test11(222);
                MessageBus.instance().get(ITest1Observer.class).test12("ITest1Observer test12");
                MessageBus.instance().get(ITest1Observer.class).test11("ITest1Observer test11");
            }
        });
        MessageBus.instance().register(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageBus.instance().unRegister(this);
    }

    @Override
    @Decorate(runThread = RunThread.BACKGROUND)
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
    public void test21(String string) {
        Log.d("TestBus", "ob2:" + string + ",currentThread : " + Thread.currentThread().getName());
    }

    @Override
    public void test21(int string) {
        Log.d("TestBus", "ob2:" + string + ",currentThread : " + Thread.currentThread().getName());
    }

    @Override
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
    @Decorate(delayedTime = 3000)
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
