package ru.alexkulikov.crewlocator;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.security.Provider;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Дайте права на слежение", Toast.LENGTH_SHORT).show();
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(this);

        startButton = (Button) findViewById(R.id.start_btn);
        assert startButton != null;
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processLocation();
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void processLocation() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Включите GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        String lat = "";
        String lon = "";

        Location lastLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location lastLocationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastLocationGPS != null) {
            lat = String.valueOf(lastLocationGPS.getLatitude());
            lon = String.valueOf(lastLocationGPS.getLongitude());
        }
        if (lastLocationNet != null) {
            lat = String.valueOf(lastLocationNet.getLatitude());
            lon = String.valueOf(lastLocationNet.getLongitude());
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        String json = "{\"client_id\":\"SPIRT\",\"date\": \"2016-07-01T19:17:49.2662422+00:00\",\"latitude\":"+lat+",\"longitude\":"+lon+"}";

        AsyncHttpPost asyncHttpPost = new AsyncHttpPost(this);
        asyncHttpPost.execute(json);
    }
}
