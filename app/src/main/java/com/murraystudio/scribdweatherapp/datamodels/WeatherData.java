package com.murraystudio.scribdweatherapp.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sushi_000 on 2/3/2017.
 */

public class WeatherData implements Parcelable{

    public String imageUrl;

    public Condition condition = new Condition();
    public Forecast forecast = new Forecast();
    public Location location = new Location();

    public WeatherData() {
        //empty
    }

    public static class Condition {
        public static String currentDescription;
        public static String date;
        public static int currentTemp;
    }

    public static class Forecast {
        public static List<Integer> tempMin = new ArrayList<>();
        public static List<Integer> tempMax = new ArrayList<>();
        public static List<String> description = new ArrayList<>();
        public static List<Integer> windSpeed = new ArrayList<>();
        public static List<String> dayOfWeek = new ArrayList<>();
        public static List<String> imageURL = new ArrayList<>();
    }

    public static class Location {
        public static String name;
    }

    public WeatherData(Parcel in) {
        imageUrl = in.readString();
    }

    public static final Creator<WeatherData> CREATOR = new Creator<WeatherData>() {
        @Override
        public WeatherData createFromParcel(Parcel in) {
            return new WeatherData(in);
        }

        @Override
        public WeatherData[] newArray(int size) {
            return new WeatherData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        //location
        parcel.writeString(Location.name);
        //condition
        parcel.writeString(Condition.currentDescription);
        parcel.writeString(Condition.date);
        parcel.writeInt(Condition.currentTemp);
        //forecast
        parcel.writeList(Forecast.tempMin);
        parcel.writeList(Forecast.tempMax);
        parcel.writeList(Forecast.description);
        parcel.writeList(Forecast.windSpeed);
        parcel.writeList(Forecast.dayOfWeek);
        parcel.writeList(Forecast.imageURL);
    }

}
