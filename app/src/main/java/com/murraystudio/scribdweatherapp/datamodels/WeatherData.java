package com.murraystudio.scribdweatherapp.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sushi_000 on 2/3/2017.
 */

public class WeatherData implements Parcelable{

    public String imageUrl;

    public Condition condition = new Condition();
    public Wind wind = new Wind();
    public Atmosphere atmosphere = new Atmosphere();
    public Forecast forecast = new Forecast();
    public Location location = new Location();
    public Astronomy astronomy = new Astronomy();
    public Units units = new Units();

    public String lastUpdate;

    public WeatherData() {

    }

    public static class Condition {
        public String description;
        public int code;
        public String date;
        public static int temp;
    }

    public  class Forecast {
        public int tempMin;
        public int tempMax;
        public String description;
        public int code;
    }

    public static class Atmosphere {
        public int humidity;
        public float visibility;
        public float pressure;
        public int rising;
    }

    public class Wind {
        public int chill;
        public int direction;
        public int speed;
    }

    public class Units {
        public String speed;
        public String distance;
        public String pressure;
        public String temperature;
    }

    public static class Location {
        public static String name;
        public String region;
        public String country;
    }

    public class Astronomy {
        public String sunRise;
        public String sunSet;
    }

    public WeatherData(Parcel in) {
        imageUrl = in.readString();
        lastUpdate = in.readString();
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
        parcel.writeString(Location.name);
        parcel.writeInt(Condition.temp);
    }

}
