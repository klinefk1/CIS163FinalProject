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
    private int currentkills, mode, totalkills, ffakills, bhkills, totalwins, ffawins, bhwins; //1 for free-for-all, 2 for bounty hunter
    private SharedPreferences prefs;

    private TextView killsInGame, winLoss, gamemode, totkills, ffakill,bhkill, totwin, ffawin, bhwin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finalscreen);

        //sets up all of the layout text fields
        killsInGame = (TextView) findViewById(R.id.numkills);
        winLoss = (TextView) findViewById(R.id.winlossmessage);
        gamemode = (TextView) findViewById(R.id.gamemode);
        totkills = (TextView) findViewById(R.id.totalkills);
        ffakill = (TextView) findViewById(R.id.ffakills);
        bhkill = (TextView) findViewById(R.id.bhkills);
        totwin = (TextView) findViewById(R.id.numwins);
        ffawin = (TextView) findViewById(R.id.ffawins);
        bhwin = (TextView) findViewById(R.id.bhwins);

        //gets the info from maps activity
        Intent fromMap = getIntent();
        win = fromMap.getBooleanExtra("win", false);
        currentkills = fromMap.getIntExtra("kills", 0);
        mode = fromMap.getIntExtra("mode", 0);

        //manager to save all of the data locally
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        checkNewGame();

        //changes things based on the game mode you played
        if(mode == 1)
        {
            gamemode.setText("Gamemode: Free For All");
            ffakills+=currentkills;
            if(win)
            {
                ffawins++;
            }
        }
        else{
            gamemode.setText("Gamemode: Bounty Hunter");
            bhkills+=currentkills;
            if(win)
            {
                bhwins++;
            }
        }

        //changes text based of if you won or lost the game played
        if(win) {
            winLoss.setText("You win!");
            totalwins++; //you won so you get +1 to the total wins
            totwin.setText("Total Wins: "+totalwins);
        }
        else{
            winLoss.setText("You lose!");
            totwin.setText("Total Wins: "+totalwins);
        }

        //sets text to current games wins
        killsInGame.setText("Kills: "+ currentkills);
        totkills.setText("Total Kills: "+ totalkills);
        ffakill.setText("Free For All: "+ ffakills);
        bhkill.setText("Bounty Hunter: "+ bhkills);
        ffawin.setText("Free For All: "+ ffawins);
        bhwin.setText("Bounty Hunter: "+ bhwins);
    }

    @Override
    protected void onResume() {
        super.onResume();
        totalkills= prefs.getInt("totalkills", 0);
        currentkills = prefs.getInt("currentkills", 1);
        totalwins = prefs.getInt("totalwins", 2);
        ffawins = prefs.getInt("ffawins", 3);
        bhwins = prefs.getInt("bhwins", 4);
        ffakills = prefs.getInt("ffakills", 5);
        bhkills = prefs.getInt("bhkills", 6);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor ped = prefs.edit();
        ped.putInt ("totalkills", totalkills);
        ped.putInt("currentkills", currentkills);
        ped.putInt("totalwins", totalwins);
        ped.putInt("ffawins", ffawins);
        ped.putInt("bhwins", bhwins);
        ped.putInt("ffakills", ffakills);
        ped.putInt("bhkills", bhkills);
        ped.commit();
    }

    public void checkNewGame()
    {
        if (prefs.contains("totalkills")) {
            totalkills = prefs.getInt("totalkills", 0)+currentkills;
        }
        else{
            totalkills = currentkills;
        }
        if(prefs.contains("totalwins"))
        {
            totalwins = prefs.getInt("totalwins", 2);
        }
        else{
            totalwins = 0;
        }
        if(prefs.contains("ffawins"))
        {
            ffawins = prefs.getInt("ffawins", 3);
        }
        else{
            ffawins = 0;
        }
        if(prefs.contains("bhwins"))
        {
            bhwins = prefs.getInt("bhwins", 4);
        }
        else{
            bhwins = 0;
        }
        if(prefs.contains("ffakills"))
        {
            ffakills = prefs.getInt("ffakills", 5);
        }
        else{
            ffakills = 0;
        }
        if(prefs.contains("bhkills"))
        {
            bhkills = prefs.getInt("bhkills", 6);
        }
        else{
            bhkills = 0;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /* outState is the name of the incoming parameter */
        outState.putInt("totalkills", totalkills);
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
