package com.murraystudio.scribdweatherapp.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.murraystudio.scribdweatherapp.R;
import com.murraystudio.scribdweatherapp.datamodels.WeatherData;

/**
 * Created by sushi_000 on 2/5/2017.
 */

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.MyViewHolder> {

    private WeatherData weatherData;
    private Activity activity;

    public WeatherForecastAdapter(WeatherData weatherData, Activity activity) {
        this.weatherData = weatherData;
        this.activity = activity;
    }



    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView day;

        public MyViewHolder(View v, final RecyclerView.Adapter teamAdapter) {
            super(v);

            day = (TextView) v.findViewById(R.id.day);

        }
    }


    @Override
    public WeatherForecastAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v, this);
        return vh;
    }

    @Override
    public void onBindViewHolder(WeatherForecastAdapter.MyViewHolder holder, int position) {

        holder.day.setText(weatherData.location.name);

    }

    @Override
    public int getItemCount() {
        //forecast is always 5 days
        return 5;
    }
}
