package com.insight.insight.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.insight.insight.R;


public class MyActivity extends ActionBarActivity {


    TextView textIntro1;
    TextView textIntro2;
    TextView textTerms;
    TextView textWelcome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_main_activity);


        textWelcome = (TextView) findViewById(R.id.textWelcome);
        String stringWelcome = getResources().getString(R.string.welcome_to_insight);
        SpannableString spannaWelcome = new SpannableString(stringWelcome);
        spannaWelcome.setSpan (new BackgroundColorSpan(Color.WHITE), 0, stringWelcome.length(), 0);
        textWelcome.setText(spannaWelcome);

        textIntro1 = (TextView) findViewById(R.id.textIntro1);
        String stringIntro1 = getResources().getString(R.string.stringIntro1);
        SpannableString spannaIntro1 = new SpannableString(stringIntro1);
        spannaIntro1.setSpan (new BackgroundColorSpan(Color.WHITE), 0, stringIntro1.length(), 0);
        textIntro1.setText(spannaIntro1);



        textIntro2 = (TextView) findViewById(R.id.textIntro2);
        String stringIntro2 = getResources().getString(R.string.stringIntro2);
        SpannableString spannaIntro2 = new SpannableString(stringIntro2);
        spannaIntro2.setSpan (new BackgroundColorSpan(Color.WHITE), 0, stringIntro2.length(), 0);
        textIntro2.setText(spannaIntro2);



        textTerms = (TextView) findViewById(R.id.textTerms);
        String stringTerms = getResources().getString(R.string.stringTerms);
        SpannableString content = new SpannableString(stringTerms);
        content.setSpan(new UnderlineSpan(), 0, stringTerms.length(), 0);
        textTerms.setText(content);



    }


    public void nextOnClick (View v){
        // Go to next page when the button is clicked.
        Intent intent = new Intent (v.getContext(), MyActivity2.class);
        startActivityForResult (intent, 0);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void  showTermsDialog(View view){
        AlertDialog.Builder termAlert = new AlertDialog.Builder(this);
        termAlert.setMessage(MyActivity.this.getString(R.string.terms_MSG))

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
