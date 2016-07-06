package ru.alexkulikov.crewlocator;

import android.Manifest;
import android.content.Context;
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
        if (tracker != null && !processStart) {
            tracker.stop();
            return;
        }

        long minSec = Long.valueOf(secEditText.getText().toString());
        long minMetr = Long.valueOf(metrEditText.getText().toString());

        final String name = crewName.getText().toString();

        int providerIndex = providersRadio.getCheckedRadioButtonId();
        switch (providerIndex) {
            case R.id.provider_net:
                tracker = new ProviderLocationTracker(this, ProviderLocationTracker.ProviderType.NETWORK, minSec, minMetr);
                break;
            case R.id.provider_sat:
                if (isGpsEnabled()) {
                    tracker = new ProviderLocationTracker(this, ProviderLocationTracker.ProviderType.GPS, minSec, minMetr);
                }
                break;
            case R.id.provider_all:
                if (isGpsEnabled()) {
                    tracker = new FallbackLocationTracker(this, minSec, minMetr);
                }
                break;
        }

        if (tracker == null) {
            return;
        }

        tracker.start(new LocationTracker.LocationUpdateListener() {
            @Override
            public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
                if (newLoc != null) {
                    AsyncHttpPost asyncHttpPost = new AsyncHttpPost(MainActivity.this);
                    asyncHttpPost.execute(name, sdf.format(new Date()), String.valueOf(newLoc.getLatitude()), String.valueOf(newLoc.getLongitude()));
                }
            }
        });
    }

    private boolean isGpsEnabled() {
        boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isEnabled) {
            Toast.makeText(MainActivity.this, "Включите GPS", Toast.LENGTH_SHORT).show();
        }
        return isEnabled;
    }
}
