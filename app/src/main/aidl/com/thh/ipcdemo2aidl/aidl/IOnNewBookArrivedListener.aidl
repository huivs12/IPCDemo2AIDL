// IOnNewBookArrivedListener.aidl
package com.thh.ipcdemo2aidl.aidl;

// Declare any non-default types here with import statements
import com.thh.ipcdemo2aidl.aidl.Book;

interface IOnNewBookArrivedListener {

   void onNewBookArrived(in Book book);
}
