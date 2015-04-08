package edu.gvsu.cis.klinefek.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class ResultActivity extends ActionBarActivity {

    private boolean win;            //true is win, false is lost
    private int numberOfKills;      //number of kills in this game - to be added to total kills
    private int mode;               //1 for free-for-all, 2 for bounty hunter
    private SharedPreferences prefs;

    private TextView killsInGame;
    private TextView winLoss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finalscreen);

        Intent fromMap = getIntent();
        win = fromMap.getBooleanExtra("win", false);
        numberOfKills = fromMap.getIntExtra("kills", 0);
        mode = fromMap.getIntExtra("mode", 0);

        killsInGame = (TextView) findViewById(R.id.kills);
        winLoss = (TextView) findViewById(R.id.winlossmessage);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState != null) {
            numberOfKills = savedInstanceState.getInt("kills");
        }
        else{
            numberOfKills = 0;
        }

        if(win) {
            winLoss.setText("You win!");
        }
        else{
            winLoss.setText("You lose!");
        }
        killsInGame.setText(Integer.toString(numberOfKills));
        //TODO store variables representing total wins for each mode, total losses for each mode, and lifetime kills
    }

    @Override
    protected void onResume() {
        super.onResume();
        numberOfKills= prefs.getInt("kills", 0);
        killsInGame.setText(numberOfKills+"");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor ped = prefs.edit();
        ped.putInt ("kills", numberOfKills);
        ped.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /* outState is the name of the incoming parameter */
        outState.putInt("kills", numberOfKills);
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
