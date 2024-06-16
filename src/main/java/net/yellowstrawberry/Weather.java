package net.yellowstrawberry;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static net.yellowstrawberry.Main.apiKey;

// 만들기 귀찮았닥호
public class Weather {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.KOREAN);
    public static final OkHttpClient client = new OkHttpClient();

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
    }

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

        public static UV fromData(int sky) {
            if(sky < 2) {
                return High;
            }else if(sky < 4) {
                return Medium;
            }else return Low;
        }
    }
    public static enum WeatherType {
        SUN,
        RAIN,
        SNOW,
        CLOUD
    }
    public static record WeatherInformation(WeatherType type, int max, int min, int humidity, UV uv, int dust, int udust, int rain) {}

    public static WeatherInformation getWeatherInformation(Date date) {
        //TODO: 구현해
        int size=0, min = 100, max= -100, rain = 0, sky = 0, hum = 0;
        float wind = 0;
        boolean snow = false;
        String sd = DATE_FORMAT.format(date);
        for(Object o1 : requestWeather(sd)) {
            JSONObject o2 = (JSONObject) o1;
            if(!o2.getString("baseTime").equals("0800") || !o2.getString("fcstDate").equals(sd)) continue;
            if(ftToInt(o2.getString("fcstTime")) < 800) continue;
            size++;
            switch (o2.getString("category")) {
                case "POP" -> {
                    // 강수 확률
                    int per = Integer.parseInt(o2.getString("fcstValue"));
                    if(rain < per) rain = per;
//                    System.out.println("강수 확률: "+o2.getString("fcstValue")+" @ "+o2.getString("fcstTime"));
                }
                case "SNO" -> {
                    // snow
                    if(!o2.getString("fcstValue").equals("적설없음")) snow = true;
//                    System.out.println("눈: "+o2.getString("fcstValue")+" @ "+o2.getString("fcstTime"));
                }
                case "REH" -> {
                    // 습도
                    hum += Integer.parseInt(o2.getString("fcstValue"));
//                    System.out.println("습도: "+o2.getString("fcstValue")+" @ "+o2.getString("fcstTime"));
                }
                case "WSD" -> {
                    // 풍속
                    wind += Float.parseFloat(o2.getString("fcstValue"));
//                    System.out.println("풍속: "+o2.getString("fcstValue")+" @ "+o2.getString("fcstTime"));
                }
                case "SKY" -> {
                    // 하늘상태
                    sky += Integer.parseInt(o2.getString("fcstValue"));
//                    System.out.println("하늘상태: "+o2.getString("fcstValue")+" @ "+o2.getString("fcstTime"));
                }
                case "TMP" -> {
                    // tmp
                    int tmp = Integer.parseInt(o2.getString("fcstValue"));
                    if(tmp > max) max = tmp;
                    if(tmp < min) min = tmp;
//                    System.out.println("온도: "+o2.getString("fcstValue")+" @ "+o2.getString("fcstTime"));
                }
            }
        }

        return new WeatherInformation(snow?WeatherType.SNOW:(rain > 30 ? WeatherType.RAIN : (sky/size > 6 ? WeatherType.CLOUD : WeatherType.SUN)), max, min, hum/size, UV.fromData(sky/size), -1, -1, rain/size);
    }

    public static JSONArray requestWeather(String date) {
        Request.Builder requestBuilder = new Request.Builder();
        try (Response r = client.newCall(requestBuilder.url("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?" +
                        "ServiceKey=%s&base_date=%s&base_time=%s".formatted(URLEncoder.encode(apiKey, StandardCharsets.UTF_8), date, "0800") +
                        "&pageNo=1&numOfRows=2000&dataType=JSON&nx=60&ny=126")
                .get()
                .build()).execute()) {
            JSONObject o = new JSONObject(r.body().string());
            return o.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int ftToInt(String ft) {
        if(ft.charAt(0) == '0') return Integer.parseInt(ft.substring(1));
        else return Integer.parseInt(ft);
    }
}
