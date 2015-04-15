package edu.gvsu.cis.klinefek.finalproject;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity2 extends ActionBarActivity {


    private TextView ffaRules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2);

        ffaRules = (TextView) findViewById(R.id.ffaRules);

        ffaRules.setText("In this game, it is a complete free for all. That means every assassin for themselves. The objective is to kill all your friends before they kill you first.  You can kill them anywhere you would like, as long as you follow a few simple rules.\n\n"
                +"1. No killing while someone is driving\n\n"
                +"2. No killing among places everyone agrees are safe zones (if any)\n\n"
                +"3. To get a kill, you must hit your friend in the chest. An assassin can defend him or herself. If the assassin is holding his or her phone in their hand AND makes a movement to defend him or herself while an assassination attempt is going on, they have blocked the current attempt. Both assassins will walk away as if nothing happened.\n\n"
                +" If there are any disputes whether you think they got killed or not, you can settle it in two ways.\n\n"
                +"1. Describe the Assassination attempt (or kill) to fellow assassins to see what their opinions are\n\n"
                +"2. Rock, Paper, Scissors (Best 2/3) The winner of free for all is not who gets the most kills, but whoever the last person alive is. Use this to your advantage.");
        ffaRules.setMovementMethod(new ScrollingMovementMethod());
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
