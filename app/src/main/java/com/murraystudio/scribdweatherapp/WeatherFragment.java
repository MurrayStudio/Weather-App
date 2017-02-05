package com.murraystudio.scribdweatherapp;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
 * Created by sushi_000 on 2/4/2017.
 */

public class WeatherFragment extends Fragment {

    private WeatherData weatherData;
    private YahooWeather mYahooWeather;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout weatherLinearLayout;

    private String currentCity = "portland oregon";

    protected RecyclerView mRecyclerView;
    protected WeatherForecastAdapter weatherForecastAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected LayoutManagerType mCurrentLayoutManagerType;

    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

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

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.forecast_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        setRecyclerViewLayout();

        temp = (TextView) rootView.findViewById(R.id.current_temperature);
        city = (TextView) rootView.findViewById(R.id.location_name);

        weatherLinearLayout = (LinearLayout) rootView.findViewById(R.id.weather_container_view);

        //swipe down to refresh data
        swipeRefreshLayout =  (SwipeRefreshLayout) rootView.findViewById(R.id.weather_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWeatherBySearch(currentCity);
            }
        });

        if (savedInstanceState == null) {
            //we have no weatherdata so build dummy views and set it to our recyclerview
            weatherForecastAdapter = new WeatherForecastAdapter(null, getActivity());
            mRecyclerView.setAdapter(weatherForecastAdapter);

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            if(sharedPref.getBoolean("permissions", false) == true) {

                Log.i("TRUE", "TRUE");

                updateWeatherByLocation();
            }
            else{
                updateWeatherBySearch("Mountain View California");

                Log.i("False", "False");
            }
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
        //otherwise update weatherData object with new data
        if(weatherInfo != null) {
            weatherData.condition.currentTemp = weatherInfo.getCurrentTemp();
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

            weatherData.forecast.tempMin.add(0, weatherInfo.getForecastInfo1().getForecastTempLow());
            weatherData.forecast.tempMin.add(1, weatherInfo.getForecastInfo2().getForecastTempLow());
            weatherData.forecast.tempMin.add(2, weatherInfo.getForecastInfo3().getForecastTempLow());
            weatherData.forecast.tempMin.add(3, weatherInfo.getForecastInfo4().getForecastTempLow());
            weatherData.forecast.tempMin.add(4, weatherInfo.getForecastInfo5().getForecastTempLow());

            weatherData.forecast.tempMax.add(0, weatherInfo.getForecastInfo1().getForecastTempHigh());
            weatherData.forecast.tempMax.add(1, weatherInfo.getForecastInfo2().getForecastTempHigh());
            weatherData.forecast.tempMax.add(2, weatherInfo.getForecastInfo3().getForecastTempHigh());
            weatherData.forecast.tempMax.add(3, weatherInfo.getForecastInfo4().getForecastTempHigh());
            weatherData.forecast.tempMax.add(4, weatherInfo.getForecastInfo5().getForecastTempHigh());

            weatherData.forecast.imageURL.add(0, weatherInfo.getForecastInfo1().getForecastConditionIconURL());
            weatherData.forecast.imageURL.add(1, weatherInfo.getForecastInfo2().getForecastConditionIconURL());
            weatherData.forecast.imageURL.add(2, weatherInfo.getForecastInfo3().getForecastConditionIconURL());
            weatherData.forecast.imageURL.add(3, weatherInfo.getForecastInfo4().getForecastConditionIconURL());
            weatherData.forecast.imageURL.add(4, weatherInfo.getForecastInfo5().getForecastConditionIconURL());
        }

        //we have weatherdata so build views and set it to our recyclerview
        weatherForecastAdapter = new WeatherForecastAdapter(weatherData, getActivity());
        mRecyclerView.setAdapter(weatherForecastAdapter);

        temp.setText(Integer.toString(weatherData.condition.currentTemp) + "°");
        city.setText(weatherData.location.name);

        weatherLinearLayout.setVisibility(View.VISIBLE);

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentcity", currentCity);
        outState.putParcelable("key", weatherData);
    }

    /*
*
* A helper method to set the layout for the recycler view.
* If the layout is already set, then we get the scroll position.
*
 */
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
}
