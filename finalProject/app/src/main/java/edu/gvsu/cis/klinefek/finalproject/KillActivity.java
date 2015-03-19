package edu.gvsu.cis.klinefek.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Created by katherineklinefelter on 3/19/15.
 */
public class KillActivity extends ActionBarActivity{
    //if the player being killed confirms the kill in a reasonable amount of time (5 min?)
    //it changes confirmed kill in MapsActivity to true by passing extra

    private Button kill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.killscreen);

        kill = (Button) findViewById(R.id.killButton);

        kill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //this should happen only if the other player confirms

                Intent launchMap = new Intent(KillActivity.this, MapsActivity.class);
                launchMap.putExtra("kill", true);
                startActivity(launchMap);
            }
        });

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
