package net.yellowstrawberry;

import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class TimeTable {
    public static String[][] getPeriod(Date date) {
        //TODO: 컴시간 알리미 parser 쓰기
        try(Response res = Weather.client.newCall(
                new Request.Builder()
                        .url("http://localhost:4000")
                        .get()
                        .build()
        ).execute()) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            JSONArray a = new JSONObject(res.body().string()).getJSONArray("data").getJSONArray(c.get(Calendar.DAY_OF_WEEK));
            String[][] r = new String[a.length()][4];

            for(int i =0; i<a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                if(o.getString("subject").isBlank()) continue;
                r[i][0] = (i+1)+"교시";
                r[i][1] = o.getString("subject");
                r[i][2] = "|";
                r[i][3] = "제공예정";
            }

            return r;
        } catch (IOException e) {
            e.printStackTrace();
            return new String[][]{{"오류", "오류", "|", "정은수 멘션 때려 감사"}};
        }
    }
}
