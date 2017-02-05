package com.murraystudio.scribdweatherapp.datamodels;

/**
 * Created by sushi_000 on 2/4/2017.
 */

public class CityData {

    private String woeid;
    private String cityName;
    private String country;

    public CityData() {}

    public CityData(String woeid, String cityName, String country) {
        this.woeid = woeid;
        this.cityName = cityName;
        this.country = country;
    }

    public String getWoeid() {
        return woeid;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public void setWoeid(String woeid) {
        this.woeid = woeid;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return cityName + "," + country;
    }
}
