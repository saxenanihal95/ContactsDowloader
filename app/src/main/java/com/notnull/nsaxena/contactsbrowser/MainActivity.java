package com.notnull.nsaxena.contactsbrowser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    Button mGetContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGetContact=findViewById(R.id.getContact);

        mGetContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }
}
