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


/**
 * Author Shamus Murray
 *
 * Main Activity that handles Fragments, permissions, and any viewgroup's holding fragments
 */
public class MainActivity extends AppCompatActivity implements PlaceSelectionListener {

    private final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 0;

    private WeatherFragment weatherFragment;
    private PlaceAutocompleteFragment autocompleteFragment;
    private FragmentManager fragmentManager;

    private SharedPreferences sharedPref;

    private boolean searchOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getFragmentManager();
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        //check our location permissions to see if we can use
        //weather by location feature.
        permissionsCheck();

        //if no config changes
        if (savedInstanceState == null) {
            // Retrieve the PlaceAutocompleteFragment (search for places to get weather here)
            autocompleteFragment = new PlaceAutocompleteFragment();

            // Register a listener to receive callbacks when a place has been selected or an error has
            // occurred.
            autocompleteFragment.setOnPlaceSelectedListener(this);

            // Retrieve the WeatherFragment (displays weather data)
            weatherFragment = new WeatherFragment();

            //we add both fragments so we can hide and show them later
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, weatherFragment, "weatherFragment")
                    .add(R.id.fragment_container, autocompleteFragment, "autocompleteFragment")
                    .commit();
        }
        //config change so find our old fragments and ready our place selected listener
        else {
            weatherFragment = (WeatherFragment) fragmentManager.findFragmentByTag("weatherFragment");
            autocompleteFragment = (PlaceAutocompleteFragment) fragmentManager.findFragmentByTag("autocompleteFragment");
            autocompleteFragment.setOnPlaceSelectedListener(this);
            searchOpen = savedInstanceState.getBoolean("searchopen", false);
        }

        //if there's a config change and AutocompleteFragment was open then
        //make sure to keep it open and hide weatherFragment, otherwise don't.
        if (searchOpen == true) {
            fragmentManager
                    .beginTransaction()
                    .hide(weatherFragment)
                    .commit();
        } else {
            fragmentManager
                    .beginTransaction()
                    .hide(autocompleteFragment)
                    .commit();
        }

        //The floating action button opens up the AutocompleteFragment to
        //search for new places to get weather for.
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
                searchOpen = true;

            }
        });
    }

    //AutocompleteFragment returns selected place results here
    @Override
    public void onPlaceSelected(Place place) {
        //update weather for this selected place in weatherFragment
        weatherFragment.updateWeatherBySearch(place.getName().toString());

        fragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(autocompleteFragment)
                .show(weatherFragment)
                .commit();

        searchOpen = false;
    }

    //if AutocompleteFragment was open, it is closed now
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        searchOpen = false;
    }

    //AutocompleteFragment returns error results here
    @Override
    public void onError(Status status) {
        Log.e("MAIN ACTIVITY", "onError: Status = " + status.toString());
    }

    //We check our location permissions here.
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

    //Where we get the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        SharedPreferences.Editor editor = sharedPref.edit();

        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //store pref saying permissions have been granted
                    editor.putBoolean("permissions", true);
                    editor.commit();
                    weatherFragment.updateWeatherByLocation();
                } else {
                    //store pref saying permissions have not been granted
                    editor.putBoolean("permissions", false);
                    editor.commit();
                }
                return;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //was searchOpen before config change?
        outState.putBoolean("searchopen", searchOpen);
        super.onSaveInstanceState(outState);
    }
}
