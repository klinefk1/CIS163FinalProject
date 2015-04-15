package edu.gvsu.cis.klinefek.finalproject;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {

    private Button toMap, statsbtn, toRules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toMap = (Button) findViewById(R.id.Start);
        statsbtn = (Button) findViewById(R.id.stats);
        toRules = (Button) findViewById(R.id.Rules);

        toMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchMap = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(launchMap);
            }
        });

        statsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchMap = new Intent(MainActivity.this, ResultActivity.class);
                startActivity(launchMap);
            }
        });

        toRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RulesPage = new Intent(MainActivity.this, RulesPage.class);
                startActivity(RulesPage);
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
