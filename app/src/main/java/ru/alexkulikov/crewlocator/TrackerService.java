package ru.alexkulikov.crewlocator;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TrackerService extends Service {

    private LocationTracker tracker;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extra = intent.getExtras();
        final String crewName = extra.getString(Preferences.CREW_NAME);
        long minTime = extra.getLong(Preferences.MIN_TIME);
        long minDistance = extra.getLong(Preferences.MIN_DISTANCE);
        int trackerType = extra.getInt(Preferences.TRACKER_TYPE);

        if (tracker != null) {
            tracker.stop();
            return super.onStartCommand(intent, flags, startId);
        }

        switch (trackerType) {
            case R.id.provider_net:
                tracker = new ProviderLocationTracker(this, ProviderLocationTracker.ProviderType.NETWORK, minTime, minDistance);
                break;
            case R.id.provider_sat:
                tracker = new ProviderLocationTracker(this, ProviderLocationTracker.ProviderType.GPS, minTime, minDistance);
                break;
            case R.id.provider_all:
                tracker = new FallbackLocationTracker(this, minTime, minDistance);
                break;
        }

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getDefault());

        tracker.start(new LocationTracker.LocationUpdateListener() {
            @Override
            public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
                if (newLoc != null) {
                    AsyncHttpPost asyncHttpPost = new AsyncHttpPost(getBaseContext());
                    asyncHttpPost.execute(crewName, sdf.format(new Date()), String.valueOf(newLoc.getLatitude()), String.valueOf(newLoc.getLongitude()));
                }
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        tracker.stop();
        super.onDestroy();
    }
}
