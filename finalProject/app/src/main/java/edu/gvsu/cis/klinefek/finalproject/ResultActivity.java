package edu.gvsu.cis.klinefek.finalproject;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class ResultActivity extends ActionBarActivity {

    private boolean win;            //whether the player won or lost
    private int numberOfKills;      //number of kills in this game - to be added to total kills
    private int mode;               //1 for free-for-all, 2 for bounty hunter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finalscreen);

        Intent fromMap = getIntent();
        win = fromMap.getBooleanExtra("win", false);
        numberOfKills = fromMap.getIntExtra("numberOfKills", 0);
        mode = fromMap.getIntExtra("mode", 0);

        //TODO store variables representing total wins for each mode, total losses for each mode, and lifetime kills

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
}
