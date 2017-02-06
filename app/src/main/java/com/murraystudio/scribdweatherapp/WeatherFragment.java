package com.murraystudio.scribdweatherapp;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.murraystudio.scribdweatherapp.adapters.WeatherForecastAdapter;
import com.murraystudio.scribdweatherapp.datamodels.WeatherData;

import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;
import zh.wang.android.yweathergetter4a.YahooWeatherInfoListener;

import static android.content.ContentValues.TAG;

/**
 * Author Shamus Murray
 * <p>
 * Fragment that handles displaying the data collected for a certain location
 */
public class WeatherFragment extends Fragment {

    //WeatherData holds all data related to the weather for a place
    private WeatherData weatherData;

    //YahooWeather is part of the YWeatherGetter4a API wrapper for Yahoo Weather
    // that gets weather data to fill the WeatherData object.
    private YahooWeather yahooWeather;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout weatherLinearLayout;

    //default place if no location established
    private String currentCity = "Portland Oregon";

    private SharedPreferences sharedPref;

    protected RecyclerView mRecyclerView;
    protected WeatherForecastAdapter weatherForecastAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected LayoutManagerType mCurrentLayoutManagerType;

    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

    //these views are not in the recyclerview so they are handled here
    private TextView place;
    private TextView currentTemp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        weatherData = new WeatherData();
        yahooWeather = YahooWeather.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weather, container, false);
        rootView.setTag(TAG);

        //recyclerview setup
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.forecast_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        setRecyclerViewLayout();

        //view setup
        currentTemp = (TextView) rootView.findViewById(R.id.current_temperature);
        place = (TextView) rootView.findViewById(R.id.location_name);
        weatherLinearLayout = (LinearLayout) rootView.findViewById(R.id.weather_container_view);

        //swipe down to refresh forecast for the current city
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.weather_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWeatherBySearch(currentCity);
            }
        });

        //gather currentcity string from shared pref
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        currentCity = sharedPref.getString("currentcity", "Portland Oregon");

        //if no config change
        if (savedInstanceState == null) {
            //we have no weatherdata so build dummy data to fill our views and set it to our recyclerview
            //we pass null here but the adapter handles filling dummy info if null is passed
            weatherForecastAdapter = new WeatherForecastAdapter(null, getActivity());
            mRecyclerView.setAdapter(weatherForecastAdapter);

            //if we have location privileges then we try and get the current location's forecast
            //otherwise default to currentcity's forecast
            sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            if (sharedPref.getBoolean("permissions", false) == true && currentCity.equals("Portland Oregon")) {
                updateWeatherByLocation();
            } else {
                updateWeatherBySearch(currentCity);
            }
        }
        //if we had a config change
        else {
            //reset adapter with previous state information
            weatherForecastAdapter = new WeatherForecastAdapter(weatherData, getActivity());
            mRecyclerView.setAdapter(weatherForecastAdapter);

            //make the call with a null param so any non-recyclerview views get updated with previous information too
            updateWeatherBySearch(currentCity); //update UI using old state.


            //we have no previous weather data so try retrieving new data
            updateWeatherBySearch(currentCity);

        }
        return rootView;
    }

    //Make a call to Yahoo Weather API with a place search term
    public void updateWeatherBySearch(String placeSearchTerm) {

        //show progress
        swipeRefreshLayout.setRefreshing(true);

        //update currentcity to new search term
        currentCity = placeSearchTerm;

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("currentcity", currentCity);
        editor.commit();

        yahooWeather.queryYahooWeatherByPlaceName(getActivity(), placeSearchTerm, new YahooWeatherInfoListener() {
            @Override
            public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
                //we have new data in WeatherInfo so pass to updateWeatherUI to update views
                if (weatherInfo != null) {
                    updateWeatherUI(weatherInfo);
                }
                //handle any errors here
                else {
                    if (errorType.toString().equals("ParsingFailed")) {
                        Toast.makeText(getActivity(), "Can't get weather for that place.", Toast.LENGTH_LONG).show();
                        updateWeatherBySearch("Portland Oregon");
                    } else {
                        Toast.makeText(getActivity(), "Unable to pull weather info from provider.", Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });
    }

    //Make a call to Yahoo Weather API using the current location as search term
    public void updateWeatherByLocation() {

        swipeRefreshLayout.setRefreshing(true);

        yahooWeather.queryYahooWeatherByGPS(getActivity(), new YahooWeatherInfoListener() {
            @Override
            public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
                //we have new data in WeatherInfo so pass to updateWeatherUI to update views
                if (weatherInfo != null) {
                    currentCity = weatherInfo.getLocationCity();

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("currentcity", currentCity);
                    editor.commit();

                    updateWeatherUI(weatherInfo);
                }
                //handle any errors here
                else {
                    Toast.makeText(getActivity(), "Unable to pull weather info using current location.", Toast.LENGTH_LONG).show();
                    updateWeatherBySearch("Portland Oregon");
                }
            }
        });
    }

    //where we update the WeatherData object with new data via YahooWeather's WeatherInfo object.
    private void updateWeatherUI(WeatherInfo weatherInfo) {

        //if weatherInfo is null then just use previous weatherData values
        //otherwise update weatherData object with new data
        if (weatherInfo != null) {
            weatherData.condition.currentTemp = celsiusToFahrenheit(weatherInfo.getCurrentTemp());
            weatherData.location.name = weatherInfo.getLocationCity();
            weatherData.forecast.dayOfWeek.add(0, weatherInfo.getForecastInfo1().getForecastDay());
            weatherData.forecast.dayOfWeek.add(1, weatherInfo.getForecastInfo2().getForecastDay());
            weatherData.forecast.dayOfWeek.add(2, weatherInfo.getForecastInfo3().getForecastDay());
            weatherData.forecast.dayOfWeek.add(3, weatherInfo.getForecastInfo4().getForecastDay());
            weatherData.forecast.dayOfWeek.add(4, weatherInfo.getForecastInfo5().getForecastDay());

            weatherData.forecast.description.add(0, weatherInfo.getForecastInfo1().getForecastText());
            weatherData.forecast.description.add(1, weatherInfo.getForecastInfo2().getForecastText());
            weatherData.forecast.description.add(2, weatherInfo.getForecastInfo3().getForecastText());
            weatherData.forecast.description.add(3, weatherInfo.getForecastInfo4().getForecastText());
            weatherData.forecast.description.add(4, weatherInfo.getForecastInfo5().getForecastText());

            weatherData.forecast.tempMin.add(0, celsiusToFahrenheit(weatherInfo.getForecastInfo1().getForecastTempLow()));
            weatherData.forecast.tempMin.add(1, celsiusToFahrenheit(weatherInfo.getForecastInfo2().getForecastTempLow()));
            weatherData.forecast.tempMin.add(2, celsiusToFahrenheit(weatherInfo.getForecastInfo3().getForecastTempLow()));
            weatherData.forecast.tempMin.add(3, celsiusToFahrenheit(weatherInfo.getForecastInfo4().getForecastTempLow()));
            weatherData.forecast.tempMin.add(4, celsiusToFahrenheit(weatherInfo.getForecastInfo5().getForecastTempLow()));

            weatherData.forecast.tempMax.add(0, celsiusToFahrenheit(weatherInfo.getForecastInfo1().getForecastTempHigh()));
            weatherData.forecast.tempMax.add(1, celsiusToFahrenheit(weatherInfo.getForecastInfo2().getForecastTempHigh()));
            weatherData.forecast.tempMax.add(2, celsiusToFahrenheit(weatherInfo.getForecastInfo3().getForecastTempHigh()));
            weatherData.forecast.tempMax.add(3, celsiusToFahrenheit(weatherInfo.getForecastInfo4().getForecastTempHigh()));
            weatherData.forecast.tempMax.add(4, celsiusToFahrenheit(weatherInfo.getForecastInfo5().getForecastTempHigh()));

            weatherData.forecast.imageURL.add(0, weatherInfo.getForecastInfo1().getForecastConditionIconURL());
            weatherData.forecast.imageURL.add(1, weatherInfo.getForecastInfo2().getForecastConditionIconURL());
            weatherData.forecast.imageURL.add(2, weatherInfo.getForecastInfo3().getForecastConditionIconURL());
            weatherData.forecast.imageURL.add(3, weatherInfo.getForecastInfo4().getForecastConditionIconURL());
            weatherData.forecast.imageURL.add(4, weatherInfo.getForecastInfo5().getForecastConditionIconURL());
        }

        //we have weatherdata so build views and set it to our recyclerview
        weatherForecastAdapter = new WeatherForecastAdapter(weatherData, getActivity());
        mRecyclerView.setAdapter(weatherForecastAdapter);

        //update non-recycler views
        currentTemp.setText(Integer.toString(weatherData.condition.currentTemp) + "°");
        place.setText(weatherData.location.name);

        //make the view visible now that it has data
        weatherLinearLayout.setVisibility(View.VISIBLE);

        //we are done, stop progress
        swipeRefreshLayout.setRefreshing(false);
    }

    //A helper method to set the layout for the recycler view.
    //If the layout is already set, then we get the scroll position.
    private void setRecyclerViewLayout() {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        mLayoutManager = new LinearLayoutManager(getActivity());
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    //a simple method to convert temps to fahrenheit
    private int celsiusToFahrenheit(int celsius) {

        int fahrenheit = 32 + ((celsius * 9) / 5);

        return fahrenheit;
    }
}
