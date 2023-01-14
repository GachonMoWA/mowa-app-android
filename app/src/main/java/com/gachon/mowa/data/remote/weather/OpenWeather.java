package com.gachon.mowa.data.remote.weather;

import com.gachon.mowa.data.remote.weather.field.Coord;
import com.gachon.mowa.data.remote.weather.field.Main;
import com.gachon.mowa.data.remote.weather.field.Weather;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpenWeather {
    @SerializedName("coord")
    private Coord coord;

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("main")
    private Main main;

    public Coord getCoord() {
        return coord;
    }

    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }
}
