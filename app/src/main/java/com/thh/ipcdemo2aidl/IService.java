package com.thh.ipcdemo2aidl;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.thh.ipcdemo2aidl.aidl.Book;
import com.thh.ipcdemo2aidl.aidl.IBookManager;
import com.thh.ipcdemo2aidl.aidl.IOnNewBookArrivedListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by TangHui on 2015/10/21.
 */
public class IService extends Service {

    private AtomicBoolean mIsServiceDestory = new AtomicBoolean(false);
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();

    private Binder mBinder = new IBookManager.Stub() {

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);
            Log.i("thhi", "[IService registerListener] registerListener size:"+mListenerList.getRegisteredCallbackCount());
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void unRegisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.unregister(listener);
            Log.i("thhi", "[IService unRegisterListener] current size:" + mListenerList.getRegisteredCallbackCount());
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            // 第二种远程调用的验证方式
            int check = checkCallingOrSelfPermission("com.thh.ipcdemo2aidl.permission.BOOK_SERVICE");
            if (check == PackageManager.PERMISSION_DENIED){
                Log.i("thhi","[IService onBind] check == PackageManager.PERMISSION_DENIED");
                return false;
            }

            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
            if (packages!=null && packages.length > 0){
                packageName = packages[0];
            }
            if (!packageName.startsWith("com.thh")){
                return false;
            }
            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "IOS"));
        new Thread(new ServiceWorker()).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // 第一种远程调用的验证方式
//        int check = checkCallingOrSelfPermission("com.thh.ipcdemo2aidl.permission.BOOK_SERVICE");
//        if (check == PackageManager.PERMISSION_DENIED){
//            Log.i("thhi","[IService onBind] check == PackageManager.PERMISSION_DENIED");
//            return null;
//        }
        return mBinder;
    }

    private class ServiceWorker implements Runnable {

        @Override
        public void run() {
            while (!mIsServiceDestory.get()){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId, "new Book # " + bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
            Log.i("thhi", "[IService onNewBookArrived] notify listener:"+listener);
            listener.onNewBookArrived(book);
        }
        mListenerList.finishBroadcast();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsServiceDestory.set(true);
    }
}
