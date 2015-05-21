package com.insight.insight.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.insight.insight.R;

public class MyActivity2 extends ActionBarActivity {

    TextView textWelcome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_main_activity2);

        //Hiding actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        Button gps_btn = (Button) findViewById(R.id.gps_btn);
        Button notif_btn = (Button) findViewById(R.id.notif_btn);
        textWelcome = (TextView) findViewById(R.id.textWelcome);
        String stringWelcome = getResources().getString(R.string.welcome_to_insight);
        SpannableString spannaWelcome = new SpannableString(stringWelcome);
        spannaWelcome.setSpan (new BackgroundColorSpan(Color.WHITE), 0, stringWelcome.length(), 0);
        textWelcome.setText(spannaWelcome);


        gps_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MyActivity2.this, GooglePlayServicesActivity.class));

            }
        });

        notif_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MyActivity2.this, NotificationActivity.class));
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //"Help" Dialog window (popup)
    public void  showHelpDialog(View view){
        AlertDialog.Builder termAlert = new AlertDialog.Builder(this);
        termAlert.setMessage(MyActivity2.this.getString(R.string.help_MSG))

                .setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                    }
                })
                .create();
        termAlert.show();

    }

    //"About" Dialog window (popup)
    public void  showAboutDialog(View view){
        AlertDialog.Builder termAlert = new AlertDialog.Builder(this);
        termAlert.setMessage(MyActivity2.this.getString(R.string.about_MSG))

                .setPositiveButton("Continue...", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                    }
                })
                .create();
        termAlert.show();

    }

}
