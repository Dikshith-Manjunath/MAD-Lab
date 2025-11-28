package com.example.myapplication.sensors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationProvider {

    public interface Callback {
        void onAddressResolved(Address address);

        void onError(String message);
    }

    private final FusedLocationProviderClient fusedClient;
    private final Geocoder geocoder;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LocationProvider(Context context) {
        fusedClient = LocationServices.getFusedLocationProviderClient(context);
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    @SuppressLint("MissingPermission")
    public void requestAddress(Callback callback) {
        CancellationTokenSource tokenSource = new CancellationTokenSource();
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.getToken())
                .addOnSuccessListener(location -> handleLocation(location, callback))
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null ? e.getMessage() : "Unable to determine location"));
    }

    private void handleLocation(Location location, Callback callback) {
        if (location == null) {
            callback.onError("Location unavailable");
            return;
        }
        executor.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    callback.onAddressResolved(addresses.get(0));
                } else {
                    callback.onError("Address not found");
                }
            } catch (IOException e) {
                callback.onError("Geocoder error: " + e.getMessage());
            }
        });
    }
}
