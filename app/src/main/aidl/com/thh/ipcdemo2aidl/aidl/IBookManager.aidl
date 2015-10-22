// IBookManager.aidl
package com.thh.ipcdemo2aidl.aidl;

// Declare any non-default types here with import statements
import com.thh.ipcdemo2aidl.aidl.Book;
import com.thh.ipcdemo2aidl.aidl.IOnNewBookArrivedListener;

interface IBookManager {

    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(IOnNewBookArrivedListener listener);
    void unRegisterListener(IOnNewBookArrivedListener listener);
}
