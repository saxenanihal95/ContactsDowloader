package com.notnull.nsaxena.contactsbrowser;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final CompositeDisposable disposables = new CompositeDisposable();

    private boolean csv_status;

    private String ABSOLULTE_PATH=Environment.getExternalStorageDirectory().getAbsolutePath();

    Button mGetContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGetContact=findViewById(R.id.getContact);

        mGetContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAsyncTask();
            }
        });
    }

    private void startAsyncTask() {
        disposables.add(sampleObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override public void onComplete() {
                        Snackbar snackbar = Snackbar
                                .make(findViewById(android.R.id.content), "successfully saved the file", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }

                    @Override
                    public void onNext(String string) {
                    }

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }


                }));;
    }

    Observable<String> sampleObservable() {
        return Observable.defer(new Callable<ObservableSource<? extends String>>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public ObservableSource<? extends String> call() throws Exception {

                CSVWriter writer = null;
                try {
                    writer = new CSVWriter(new FileWriter(ABSOLULTE_PATH+ "/contacts.csv"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                String displayName;
                String number;
                long _id;
                String columns[] = new String[]{ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME};
                //writer.writeColumnNames(); // Write column header
                Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                        columns,
                        null,
                        null,
                        ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
                startManagingCursor(cursor);
                if (cursor.moveToFirst()) {
                    do {
                        _id = Long.parseLong(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
                        displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)).trim();
                        number = getPrimaryNumber(_id);
                        if (number!=null && displayName!=null&&writer!=null)
                        {
                            writer.writeNext((displayName + "/" + number).split("/"));
                        }
                    } while (cursor.moveToNext());
                    csv_status = true;
                } else {
                    csv_status = false;
                }
                try {
                   if(writer != null)
                        writer.close();
                } catch (IOException e) {
                    Log.w("Test", e.toString());
                }

                if(csv_status == true) {

                    zip( ABSOLULTE_PATH+"/contacts.csv",ABSOLULTE_PATH+"/contacts.zip");
                } else {
                    Toast.makeText(getApplicationContext(), "Information not available to create CSV.", Toast.LENGTH_SHORT).show();
                }
                return Observable.just("completed");
            }

        });
    }

    private String getPrimaryNumber(long _id) {
        String primaryNumber = null;
        Cursor cursor=null;
        try {
            cursor = getContentResolver().query( Phone.CONTENT_URI,
                    new String[]{Phone.NUMBER, Phone.TYPE},
                    Phone.CONTACT_ID +" = "+ _id, // We need to add more selection for phone type
                    null,
                    null);
            if(cursor != null) {
                while(cursor.moveToNext()){
                    switch(cursor.getInt(cursor.getColumnIndex(Phone.TYPE))){
                        case Phone.TYPE_MOBILE :
                            primaryNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                            break;
                        case Phone.TYPE_HOME :
                            primaryNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                            break;
                        case Phone.TYPE_WORK :
                            primaryNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                            break;
                        case Phone.TYPE_OTHER :
                    }
                    if(primaryNumber != null)
                        break;
                }
            }
        } catch (Exception e) {
            Log.i("test", "Exception " + e.toString());
        } finally {
            if(cursor != null) {
                cursor.deactivate();
                cursor.close();
            }
        }
        return primaryNumber;
    }

    private void zip(String _files, String zipFileName) {
        int BUFFER = 2048;
        try {
            BufferedInputStream origin = null;
            System.out.println("file location : " +_files);
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];
            Log.v("Compress", "Adding: " + _files);
            FileInputStream fi = new FileInputStream(_files);
            origin = new BufferedInputStream(fi, BUFFER);

            ZipEntry entry = new ZipEntry(_files.substring(_files.lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;

            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();


            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
