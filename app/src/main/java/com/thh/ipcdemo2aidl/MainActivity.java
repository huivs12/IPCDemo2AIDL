package com.thh.ipcdemo2aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.thh.ipcdemo2aidl.aidl.Book;
import com.thh.ipcdemo2aidl.aidl.IBookManager;
import com.thh.ipcdemo2aidl.aidl.IOnNewBookArrivedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MESSAGE_NEW_BOOK_ARRIVED = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.i("thhi", "[MainActivity mHandler handlerMessage]: MESSAGE_NEW_BOOK_ARRIVED, msg:"+msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private IBookManager iBookManager;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBookManager = IBookManager.Stub.asInterface(service);
            try {
                List<Book> bookList = iBookManager.getBookList();
                Log.i("thhi", "bookList:" + bookList);
                iBookManager.addBook(new Book(3, "Window Phone"));
                Log.i("thhi", "addBook after:" + iBookManager.getBookList());
                iBookManager.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iBookManager = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, IService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {

        @Override
        public void onNewBookArrived(Book book) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, book).sendToTarget();
        }
    };

    @Override
    protected void onDestroy() {
        if (iBookManager != null && iBookManager.asBinder().isBinderAlive()) {
            try {
                iBookManager.unRegisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
