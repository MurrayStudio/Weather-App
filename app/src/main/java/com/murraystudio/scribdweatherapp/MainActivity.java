package com.murraystudio.scribdweatherapp;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

public class MainActivity extends AppCompatActivity implements PlaceSelectionListener {

    private WeatherFragment weatherFragment;
    private PlaceAutocompleteFragment autocompleteFragment;

    private FragmentManager fragmentManager;

    private final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 0;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getFragmentManager();

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        permissionsCheck();

        if (savedInstanceState == null) {
            // Retrieve the PlaceAutocompleteFragment.
            autocompleteFragment = new PlaceAutocompleteFragment();
            // Register a listener to receive callbacks when a place has been selected or an error has
            // occurred.
            autocompleteFragment.setOnPlaceSelectedListener(this);

            weatherFragment = new WeatherFragment();
            //getFragmentManager().beginTransaction().replace(R.id.fragment_container, weatherFragment).commit();

            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, weatherFragment, "weatherFragment")
                    .add(R.id.fragment_container, autocompleteFragment, "autocompleteFragment")
                    .commit();
        } else {
            weatherFragment = (WeatherFragment) fragmentManager.findFragmentByTag("weatherFragment");
            autocompleteFragment = (PlaceAutocompleteFragment) fragmentManager.findFragmentByTag("autocompleteFragment");
            autocompleteFragment.setOnPlaceSelectedListener(this);
        }

        if(sharedPref.getBoolean("searchopen", false) == true) {
            fragmentManager
                    .beginTransaction()
                    .hide(weatherFragment)
                    .commit();
        }
        else{
            fragmentManager
                    .beginTransaction()
                    .hide(autocompleteFragment)
                    .commit();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                        .hide(weatherFragment)
                        .show(autocompleteFragment)
                        .addToBackStack("Search Cities")
                        .commit();

                //keep note that we have opened the search fragment
                SharedPreferences.Editor editor = sharedPref.edit();;
                editor.putBoolean("searchopen", true);
                editor.commit();

            }
        });
    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.i("MAIN ACTIVITY", "Place Selected: " + place.getAddress());
        weatherFragment.updateWeatherBySearch(place.getName().toString());

        fragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(autocompleteFragment)
                .show(weatherFragment)
                .commit();

        //keep note that we have closed  the search fragment
        SharedPreferences.Editor editor = sharedPref.edit();;
        editor.putBoolean("searchopen", false);
        editor.commit();
    }

    @Override
    public void onError(Status status) {
        Log.e("MAIN ACTIVITY", "onError: Status = " + status.toString());
    }

    private void permissionsCheck() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        SharedPreferences.Editor editor = sharedPref.edit();

        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    editor.putBoolean("permissions", true);
                    editor.commit();

                    weatherFragment.updateWeatherByLocation();

                    Log.i("PERMISSION TRUE", "PERMISSION TRUE");

                } else {

                    editor.putBoolean("permissions", false);
                    editor.commit();

                    Log.i("PERMISSION False", "PERMISSION False");
                }
                return;
            }
        }
    }
}
