/*
 * ���ε{����l�i�J�I
 * */
package com.nchu.motoguider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class Initial extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        
        new Handler().postDelayed(new Runnable() 
        {
            @Override
            public void run()
            {
                final Intent intent = new Intent(Initial.this, Speech.class);
                startActivity(intent);
                finish();
             }
            }, 2000); /* ���2�������Speech.class */    
    }
}