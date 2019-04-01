package ru.relastic.meet012multithreading;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    public final static int WHAT_MESSAGE_DATA = 1000;
    public final static String KEY_FRAGMENT_DATA = "key_fragment_data";
    private final static int WHAT_CONNECTED = 2000;
    private final static String ESTABLISH_CONNECTION = "establish_connection";


    private static boolean isFirstStart = false;
    private MyFragment myFragment1, myFragment2;
    private TextView mTextView;
    private EditText  mEditText;
    private Button mButton;
    private IDataInterface dataInterface;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dataInterface = IDataInterface.Stub.asInterface(service);
            Message msg = new Message();
            msg.what = WHAT_CONNECTED;
            myHandler.sendMessage(msg);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dataInterface = null;
        }
    };
    private MyHandler myHandler = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isFirstStart = (savedInstanceState==null);
        initViews();
        initListeners();
        init();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mTextView = new TextView(getApplicationContext());
        mTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setBackgroundColor(Color.MAGENTA);
        mTextView.setText("Это область фрагмента");
        myFragment1.getLayout().addView(mTextView);

        mEditText = new EditText(getApplicationContext());
        mEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mEditText.setGravity(Gravity.CENTER);
        mEditText.setBackgroundColor(Color.WHITE);
        mEditText.setText("Это область фрагмента");
        myFragment2.getLayout().addView(mEditText);
        mButton = new Button(getApplicationContext());
        mButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mButton.setGravity(Gravity.CENTER);
        mButton.setText("Обновить данные");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dataInterface.setServiceValue(mEditText.getText().toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        myFragment2.getLayout().addView(mButton);
    }


    private void initViews() {

    }
    private void initListeners() {

    }
    private void init(){
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction;
        if (isFirstStart) {
            fragmentTransaction = fragmentManager.beginTransaction();

            Bundle bundle;
            myFragment1 = new MyFragment();
            bundle = myFragment1.getArguments();
            if (bundle == null) {bundle = new Bundle();}
            bundle.putInt(MyFragment.KEY_BACKGROUNDCOLOR,Color.YELLOW);
            myFragment1.setArguments(bundle);
            fragmentTransaction.add(R.id.layout_top, myFragment1, "myFragment1");

            myFragment2 = new MyFragment();
            bundle = myFragment2.getArguments();
            if (bundle == null) {bundle = new Bundle();}
            bundle.putInt(MyFragment.KEY_BACKGROUNDCOLOR,Color.LTGRAY);
            bundle.putInt(MyFragment.KEY_LAYOUT_ORIENTATION,LinearLayout.VERTICAL);
            myFragment2.setArguments(bundle);
            fragmentTransaction.add(R.id.layout_lower, myFragment2, "myFragment2");

            fragmentTransaction.commit();
        }else {
            myFragment1 = (MyFragment) fragmentManager.findFragmentByTag("myFragment1");
            myFragment2 = (MyFragment) fragmentManager.findFragmentByTag("myFragment2");
        }
        Intent intent = new Intent(this, MyService.class);

        Bundle bundle = new Bundle();
        Messenger msgr = new Messenger(myHandler);
        bundle.putBinder("binder", msgr.getBinder());

        intent.putExtra("args", bundle);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindService(mConnection);
        super.onPause();
    }

    class MyHandler extends Handler {
        MyHandler() {
            super(Looper.getMainLooper());
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_MESSAGE_DATA:
                    mTextView.setText(msg.getData().getString(KEY_FRAGMENT_DATA));
                    break;
                case WHAT_CONNECTED:
                    try {
                        mEditText.setText(dataInterface.getServiceValue());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
