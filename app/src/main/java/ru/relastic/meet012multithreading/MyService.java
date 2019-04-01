package ru.relastic.meet012multithreading;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;

public class MyService extends Service {
    private static final String KEY_SERVICE_VALUE="key_string_value";
    private static final String PREFERENCIES_KEY = "preferencies_key";

    private Messenger messenger;

    ArrayList<Messenger> clients = new ArrayList<>();

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle args = intent.getBundleExtra("args");
        IBinder iBinder = args.getBinder("binder");

        Messenger client = new Messenger(iBinder);
        clients.add(client);
        try {
            sendValue(getValue());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return new IDataInterface.Stub() {
            @Override
            public String getServiceValue() throws RemoteException {
                return getValue();
            }

            @Override
            public void setServiceValue(String value) throws RemoteException {
                sendValue(value);
            }
        };
    }
    private String getValue(){
        SharedPreferences sp = getSharedPreferences(PREFERENCIES_KEY, Context.MODE_PRIVATE);
        return sp.getString(KEY_SERVICE_VALUE,"");
    }
    private void sendValue (String value) throws RemoteException {
        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCIES_KEY,
                Context.MODE_PRIVATE).edit();
        editor.putString(KEY_SERVICE_VALUE, value);
        editor.commit();
        for (Messenger msgr : clients) {
            Message msg = new Message();
            msg.what = MainActivity.WHAT_MESSAGE_DATA;
            msg.getData().putString(MainActivity.KEY_FRAGMENT_DATA, value);
            msgr.send(msg);
        }
    }
}
