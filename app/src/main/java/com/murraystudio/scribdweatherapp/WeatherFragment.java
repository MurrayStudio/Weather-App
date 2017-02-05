package com.murraystudio.scribdweatherapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.murraystudio.scribdweatherapp.datamodels.WeatherData;

import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;
import zh.wang.android.yweathergetter4a.YahooWeatherInfoListener;

import static android.content.ContentValues.TAG;

/**
 * Created by sushi_000 on 2/4/2017.
 */

public class WeatherFragment extends Fragment {

    private WeatherData weatherData;
    private YahooWeather mYahooWeather;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String currentCity = "portland oregon";

    private TextView city;
    private TextView temp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        weatherData = new WeatherData();
        mYahooWeather = YahooWeather.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weather, container, false);
        rootView.setTag(TAG);

        temp = (TextView) rootView.findViewById(R.id.temperature);
        city = (TextView) rootView.findViewById(R.id.city);

        //swipe down to refresh data
        swipeRefreshLayout =  (SwipeRefreshLayout) rootView.findViewById(R.id.weather_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWeatherBySearch(currentCity);
            }
        });

        if (savedInstanceState == null) {
            updateWeatherByLocation();
        }
        else{

            currentCity = savedInstanceState.getString("currentcity");

            //gets weather data
            if(weatherData != null) {
                weatherData = savedInstanceState.getParcelable("key"); //config change so old data before change
                updateWeatherUI(null); //update UI using old state.
            }
            else{
                updateWeatherBySearch(currentCity); //we have no previous weather data so try retrieving new data
            }


        }


        return rootView;
    }


    public void updateWeatherBySearch(String placeSearchTerm){

        swipeRefreshLayout.setRefreshing(true);

        currentCity = placeSearchTerm;

        mYahooWeather.queryYahooWeatherByPlaceName(getActivity(), placeSearchTerm, new YahooWeatherInfoListener() {
            @Override
            public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
                if(weatherInfo != null) {
                    updateWeatherUI(weatherInfo);
                }
                else{
                    Toast.makeText(getActivity(), "Unable to pull weather info from provider.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateWeatherByLocation(){

        swipeRefreshLayout.setRefreshing(true);

        mYahooWeather.queryYahooWeatherByGPS(getActivity(), new YahooWeatherInfoListener() {
            @Override
            public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
                if(weatherInfo != null) {
                    currentCity = weatherInfo.getLocationCity();
                    updateWeatherUI(weatherInfo);
                }
                else{
                    Toast.makeText(getActivity(), "Unable to pull weather info using current location.", Toast.LENGTH_LONG).show();
                    updateWeatherBySearch("Portland Oregon");
                }
            }
        });
    }

    private void updateWeatherUI(WeatherInfo weatherInfo){

        //if weatherInfo is null then just use previous weatherData values
        if(weatherInfo != null) {
            weatherData.condition.temp = weatherInfo.getCurrentTemp();
            weatherData.location.name = weatherInfo.getLocationCity();
            Log.i("getDescription", weatherInfo.getForecastInfo1().getForecastText());
            Log.i("forecast1", weatherInfo.getForecastInfo1().getForecastDate());
        }
        temp.setText(Integer.toString(weatherData.condition.temp) + "°");
        city.setText(weatherData.location.name);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentcity", currentCity);

        outState.putParcelable("key", weatherData);
    }
}
