package ru.alexkulikov.crewlocator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private LocationTracker tracker;

    private LocationManager locationManager;
    private ToggleButton startButton;
    private EditText secEditText;
    private EditText metrEditText;
    private RadioGroup providersRadio;
    private EditText crewName;

    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getDefault());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Дайте права на слежение", Toast.LENGTH_SHORT).show();
            return;
        }

        startButton = (ToggleButton) findViewById(R.id.start_btn);
        secEditText = (EditText) findViewById(R.id.sec_edit);
        metrEditText = (EditText) findViewById(R.id.metr_edit);
        providersRadio = (RadioGroup) findViewById(R.id.providers);
        crewName = (EditText) findViewById(R.id.crew_name);

        assert startButton != null;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        startButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                processLocation(isChecked);
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void processLocation(boolean processStart) {
        Intent intent = new Intent(this, TrackerService.class);
        if (!processStart) {
            stopService(intent);
            return;
        }

        int providerIndex = providersRadio.getCheckedRadioButtonId();

        switch (providerIndex) {
            case R.id.provider_sat:
                if (!isGpsEnabled()) {
                   return;
                }
                break;
            case R.id.provider_all:
                if (!isGpsEnabled()) {
                    return;
                }
                break;
        }

       intent.putExtra(Preferences.CREW_NAME, crewName.getText().toString());
       intent.putExtra(Preferences.MIN_TIME, Long.valueOf(secEditText.getText().toString()));
       intent.putExtra(Preferences.MIN_DISTANCE, Long.valueOf(metrEditText.getText().toString()));
       intent.putExtra(Preferences.TRACKER_TYPE, providerIndex);

        startService(intent);
    }

    private boolean isGpsEnabled() {
        boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isEnabled) {
            Toast.makeText(MainActivity.this, "Включите GPS", Toast.LENGTH_SHORT).show();
        }
        return isEnabled;
    }
}
