package com.murraystudio.scribdweatherapp.datamodels;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Shamus Murray
 *
 * Holds forecast data for a place. Parcelable so it can
 * be passed between config changes.
 */
public class WeatherData {

    public Condition condition = new Condition();
    public Forecast forecast = new Forecast();
    public Location location = new Location();

    public static class Condition {
        public static int currentTemp;
    }

    public static class Forecast {
        public static List<Integer> tempMin = new ArrayList<>();
        public static List<Integer> tempMax = new ArrayList<>();
        public static List<String> description = new ArrayList<>();
        public static List<String> dayOfWeek = new ArrayList<>();
        public static List<String> imageURL = new ArrayList<>();
    }

    public static class Location {
        public static String name;
    }

}
