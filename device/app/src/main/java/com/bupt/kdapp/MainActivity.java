package com.bupt.kdapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bupt.deck.utils.CommonHelper;
import com.bupt.deck.utils.UUIDHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity-Deck";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Show uuid in MainActivity
        TextView uuidText = findViewById(R.id.uuidText);
        uuidText.setText(UUIDHelper.getInstance(getApplicationContext()).getUniqueID());
        // uuidText.setText(PreferenceHelper.getDeviceId(getApplicationContext()));

        // Start Deck Service
        if (!CommonHelper.isServiceRunning(DeckService.class, getApplicationContext())) {
            Log.i(TAG, "onCreate: startService");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.i(TAG, "onCreate: startForegroundService");
                startForegroundService(new Intent(this, DeckService.class));
            } else {
                Log.i(TAG, "onCreate: startService");
                startService(new Intent(this, DeckService.class));
            }
        } else {
            Log.w(TAG, "onCreate: DeckService is still running, ignore start command");
        }

        // requestPermissions(new String[]{
        //                 Manifest.permission.ACCESS_FINE_LOCATION,
        //                 Manifest.permission.ACCESS_COARSE_LOCATION,
        //         },
        //         8888);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        stopService(new Intent(this, DeckService.class));
        startService(new Intent(this, DeckService.class));
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