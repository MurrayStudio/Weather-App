package com.murraystudio.scribdweatherapp;

import android.app.Fragment;
import android.os.Bundle;
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

public class CityPickFragment extends Fragment {

    WeatherData weatherData;

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

        temp = (TextView) rootView.findViewById(R.id.temperature);


        weatherData = new WeatherData();


        YahooWeather mYahooWeather = YahooWeather.getInstance();

        mYahooWeather.queryYahooWeatherByPlaceName(getActivity(), "7010 n olin, portland oregon", new YahooWeatherInfoListener() {
            @Override
            public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
                Log.i("PORTLAND TEMP: ", Integer.toString(weatherInfo.getCurrentTemp()));

                weatherData.condition.temp = weatherInfo.getCurrentTemp();

                temp.setText(Integer.toString(weatherData.condition.temp));
            }
        });


        return rootView;
    }
}
