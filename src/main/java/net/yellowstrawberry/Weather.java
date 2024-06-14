package net.yellowstrawberry;

import java.util.Date;

public class Weather {
    public static enum UV {
        High("높음"),
        Medium("보통"),
        Low("낮음");

        private final String name;
        UV(String kr) {
            this.name = kr;
        }

        public String kr() {
            return name;
        }
    }
    public static record WeatherInformation(int max, int min, int humidity, UV uv, int dust, int udust) {}

    public static WeatherInformation getWeatherInformation(Date date) {
        //TODO: 구현해
        return new WeatherInformation(32, 28, 60, UV.High, 25, 10 );
    }
}
