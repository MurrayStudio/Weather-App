package com.murraystudio.scribdweatherapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    TextView temp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weather, container, false);
        rootView.setTag(TAG);

        if (savedInstanceState != null) {
            // Restore last state
            currentCity = savedInstanceState.getString("currentcity");
        }

        temp = (TextView) rootView.findViewById(R.id.temperature);

        weatherData = new WeatherData();
        mYahooWeather = YahooWeather.getInstance();

        //swipe down to refresh data
        swipeRefreshLayout =  (SwipeRefreshLayout) rootView.findViewById(R.id.weather_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWeather(currentCity);
            }
        });


        updateWeather(currentCity);

        return rootView;
    }


    public void updateWeather(String placeSearchTerm){

        swipeRefreshLayout.setRefreshing(true);

        currentCity = placeSearchTerm;

        mYahooWeather.queryYahooWeatherByPlaceName(getActivity(), placeSearchTerm, new YahooWeatherInfoListener() {
            @Override
            public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
                Log.i("PORTLAND TEMP: ", Integer.toString(weatherInfo.getCurrentTemp()));

                weatherData.condition.temp = weatherInfo.getCurrentTemp();

                temp.setText(Integer.toString(weatherData.condition.temp));

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentcity", currentCity);
    }

}
