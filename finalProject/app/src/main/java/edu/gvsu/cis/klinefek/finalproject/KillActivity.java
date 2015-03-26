package edu.gvsu.cis.klinefek.finalproject;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class KillActivity extends ActionBarActivity {


    //allows me to test the map...delete later
    private Button kill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.killscreen);

        kill = (Button) findViewById(R.id.killButton);

        kill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //need to select person killed either based on location or select from a list

                //need to wait a designated amount of time for other player to confirm (5 min?), then
                //if they do, launch the intent
                Intent launchMap = new Intent();
                launchMap.putExtra("kill", true);
                setResult(RESULT_OK, launchMap);
                finish();
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
