package net.yellowstrawberry;

import me.yellowstrawberry.openneisapi.ONA;
import me.yellowstrawberry.openneisapi.objects.school.School;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class Main {

    public static ONA ona;
    public static School school;
    public static String apiKey;

    public static void main(String[] args) {
//        Weather.requestWeather();
        try {
            System.out.println(System.getProperty("cloud"));
            apiKey = System.getProperty("cloud");
            ona = new ONA.Builder(System.getProperty("neis")).build();
            school = ona.searchSchool("선린인터넷")[0];
            int i= 0;
            for(BufferedImage ig : ImageRenderer.render(new Calendar.Builder().setTimeZone(TimeZone.getTimeZone("Asia/Seoul")).setDate(2024, 5, 16).build().getTime())) {
                ImageIO.write(ig, "PNG", new File("./wow%d.png".formatted(i++)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 귀찮 귀찮 귀찮
    }
}