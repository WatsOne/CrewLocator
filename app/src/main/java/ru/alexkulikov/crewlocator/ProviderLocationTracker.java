package ru.alexkulikov.crewlocator;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class ProviderLocationTracker implements LocationListener, LocationTracker {

    private long minDistance = 0;
    private long minUpdateTime = 3000;

    private LocationManager lm;

    public enum ProviderType{
        NETWORK,
        GPS
    };
    private String provider;

    private Location lastLocation;
    private long lastTime;

    private boolean isRunning;

    private LocationUpdateListener listener;

    public ProviderLocationTracker(Context context, ProviderType type, long minUpdateTime, long minDistance) {
        lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        this.minDistance = minDistance;
        this.minUpdateTime = minUpdateTime * 1000;

        if(type == ProviderType.NETWORK){
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else{
            provider = LocationManager.GPS_PROVIDER;
        }
    }

    @SuppressWarnings({"MissingPermission"})
    public void start(){
        if(isRunning){
            //Already running, do nothing
            return;
        }

        //The provider is on, so start getting updates.  Update current location
        isRunning = true;
        lm.requestLocationUpdates(provider, minUpdateTime, minDistance, this);
        lastLocation = null;
        lastTime = 0;
    }

    public void start(LocationUpdateListener update) {
        start();
        listener = update;

    }

    @SuppressWarnings({"MissingPermission"})
    public void stop(){
        if(isRunning){
            lm.removeUpdates(this);
            isRunning = false;
            listener = null;
        }
    }

    public boolean hasLocation() {
        return lastLocation != null && System.currentTimeMillis() - lastTime <= minUpdateTime;
    }

    @SuppressWarnings({"MissingPermission"})
    public boolean hasPossiblyStaleLocation() {
        return lastLocation != null || lm.getLastKnownLocation(provider) != null;
    }

    public Location getLocation(){
        if(lastLocation == null){
            return null;
        }
        if(System.currentTimeMillis() - lastTime > minUpdateTime){
            return null; //stale
        }
        return lastLocation;
    }

    @SuppressWarnings({"MissingPermission"})
    public Location getPossiblyStaleLocation(){
        if(lastLocation != null){
            return lastLocation;
        }
        return lm.getLastKnownLocation(provider);
    }

    public void onLocationChanged(Location newLoc) {
        long now = System.currentTimeMillis();
        if(listener != null){
            listener.onUpdate(lastLocation, lastTime, newLoc, now);
        }
        lastLocation = newLoc;
        lastTime = now;
    }

    public void onProviderDisabled(String arg0) {

    }

    public void onProviderEnabled(String arg0) {

    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }
}