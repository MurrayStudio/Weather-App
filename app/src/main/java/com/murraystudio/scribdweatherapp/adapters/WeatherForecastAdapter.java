package com.murraystudio.scribdweatherapp.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.murraystudio.scribdweatherapp.R;
import com.murraystudio.scribdweatherapp.datamodels.WeatherData;

/**
 * Author Shamus Murray
 *
 * Adapter for recyclerview. Displays views for 5 day forecast
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
        public TextView description;
        public TextView tempMin;
        public TextView tempMax;
        public ImageView conditionImage;

        public MyViewHolder(View v, final RecyclerView.Adapter teamAdapter) {
            super(v);

            day = (TextView) v.findViewById(R.id.day);
            description = (TextView) v.findViewById(R.id.description);
            tempMin = (TextView) v.findViewById(R.id.minimum_temperature);
            tempMax = (TextView) v.findViewById(R.id.maximum_temperature);
            conditionImage = (ImageView) v.findViewById(R.id.conditionImage);
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

        //if null or size 0 weatherData then setup some dummy weather until new data can be loaded
        if(weatherData == null || weatherData.forecast.description.size() <= 0){
            holder.day.setText("---");
            holder.description.setText("--------");
            holder.tempMin.setText("--°");
            holder.tempMax.setText("--°");
        }
        //our weatherData has data, assign it to appropriate views
        else {
            //position 0 is today
            if(position == 0) {
                holder.day.setText("Today");
            }
            else{
                holder.day.setText(weatherData.forecast.dayOfWeek.get(position));
            }
            holder.description.setText(weatherData.forecast.description.get(position));
            holder.tempMin.setText(Integer.toString(weatherData.forecast.tempMin.get(position)) + "°");
            holder.tempMax.setText(Integer.toString(weatherData.forecast.tempMax.get(position)) + "°");

            //Use Glide to load images from URL and display in imageview
            String mediaURL = weatherData.forecast.imageURL.get(position);
            Glide.with(activity)
                    .load(mediaURL)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.conditionImage);
        }
    }

    @Override
    public int getItemCount() {
        //forecast is always 5 days
        return 5;
    }
}
