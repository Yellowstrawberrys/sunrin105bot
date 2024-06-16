package net.yellowstrawberry;

import me.yellowstrawberry.openneisapi.objects.food.Food;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static net.yellowstrawberry.Main.ona;
import static net.yellowstrawberry.Main.school;

public class ImageRenderer {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM월 dd일 (E)", Locale.KOREAN);

    private static final int[] offsets = {
            154, // 요일 위쪽 0
            198, // 날씨 설명 위쪽 1

            433, // 시간표 위쪽 2
            40, // 시간표 내용 위쪽 3
            45+20, // 시간표 교시 왼쪽 4
            45+56, // 시간표 과목 왼쪽 5
            45+113, // 시간표 | 왼쪽 6
            45+126, // 시간표 안내사항 왼쪽 7

            759+55, // 급식, 주의사항 위쪽 8
            45+35, // 급식 왼쪽 9
            326+35, // 주의사항 왼쪽 10

            45, // 날씨 설명, 요일 왼쪽 11

            102+12, // 날씨 온도 위쪽 12
            386+14, // 날씨 온도 왼쪽 13
            102+56, // 날씨 정보 시작 위쪽 14
            418 // 날씨 정보 왼쪽 15
    };

    private static final float[] sizes = {
            32, // 요일 폰트 사이즈 0
            20, // 날씨 설명 폰트 사이즈 1

            280, // 시간표 전체 높이 2
            11, // 시간표 교시 폰트 사이즈 3
            17, // 시간표 과목 폰트 사이즈 4
            13, // 사간표 그외 폰트 사이즈 5

            16, // 급식, 주의사항 폰트 사이즈 6

            32, // 날씨 온도 폰트 사이즈 7
            15, // 날씨 정보 폰트 사이즈 8

            21,// 날씨 정보 위쪽 9
    };

    private static final Font[] fonts = {
            // TODO: Regular, Medium, SemiBold, ExtraBold
            loadFont(new File(ImageRenderer.class.getResource("/fonts/WantedSans-Regular.ttf").getFile())),
            loadFont(new File(ImageRenderer.class.getResource("/fonts/WantedSans-Medium.ttf").getFile())),
            loadFont(new File(ImageRenderer.class.getResource("/fonts/WantedSans-SemiBold.ttf").getFile())),
            loadFont(new File(ImageRenderer.class.getResource("/fonts/WantedSans-ExtraBold.ttf").getFile()))
    };

    private static final Color[] colors = {
            new Color(234, 234, 234),
            new Color(187, 187, 187),
            new Color(149, 149, 149)
    };

    private static Font loadFont(File file) {
        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, file).deriveFont(12f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
            return customFont;
        }catch (Exception e) {e.printStackTrace(); throw new RuntimeException(e);}
    }


    static  {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
    }


    private static BufferedImage loadBackground(Weather.WeatherType weatherType) {
        try {
            return ImageIO.read(ImageRenderer.class.getResourceAsStream("/bg/%s.png".formatted(weatherType.name())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage[] render(Date date) {
        Weather.WeatherInformation info = Weather.getWeatherInformation(date);
        BufferedImage image = loadBackground(info.type());
        Graphics2D g = image.createGraphics();

        drawHeader(g, date);
        drawWeather(g, info);
        drawTimetable(g, date);
        drawMeal(g, date);
        drawWarning(g, date);
        System.out.println(info);

        return new BufferedImage[]{
                image.getSubimage(0, 0, 634, 364),
                image.getSubimage(0, 364, 634, 364),
                image.getSubimage(0, 364*2, 634, 364),
        };
    }

    private static void drawHeader(Graphics2D g, Date date) {
        String s = DATE_FORMAT.format(date);
        g.setFont(fonts[3].deriveFont(sizes[0]));
        g.setColor(colors[0]);
        g.drawString(s, 45, offsets[0]+g.getFontMetrics().getAscent());

        g.setFont(fonts[0].deriveFont(sizes[1]));
        g.setColor(colors[1]);
        int off = 0;
        for(String a : getWeatherMessage().split("\n")) {
            g.drawString(a, 45, offsets[1]+off+g.getFontMetrics().getAscent());
            off += g.getFontMetrics().getAscent();
        }
    }

    private static void drawWeather(Graphics2D g, Weather.WeatherInformation info) {
        g.setFont(fonts[0].deriveFont(sizes[7]));
        g.setColor(colors[0]);
        g.drawString("%d° / %d°".formatted(info.max(), info.min()), offsets[13], offsets[12]+g.getFontMetrics().getAscent());

        g.setFont(fonts[0].deriveFont(sizes[8]));
        g.setColor(colors[2]);

        g.drawString(info.uv().kr(), offsets[15], offsets[14]+g.getFontMetrics().getAscent());
        g.drawString("%d%%".formatted(info.humidity()), offsets[15], offsets[14]+sizes[9]+g.getFontMetrics().getAscent());
        g.drawString("지원예정", offsets[15], offsets[14]+sizes[9]*2+g.getFontMetrics().getAscent());
    }


    private static void drawTimetable(Graphics2D g, Date date) {
        String[][] table = TimeTable.getPeriod(date);
        for (int i=0; i<table.length; i++) {
            int offset = offsets[3]*i;
            for (int j=0; j<4; j++) {
                g.setColor(colors[j%2==1?0:2]);
                g.setFont(fonts[j==1?2:1].deriveFont(sizes[3+j]));
                g.drawString(table[i][j], offsets[4+j], offsets[2]+offset+getCenterOffset(offsets[3], g.getFontMetrics().getHeight())+g.getFontMetrics().getAscent());
            }
        }
    }

    private static void drawMeal(Graphics2D g, Date date) {
        try {
            int offset = 0;
            g.setColor(colors[0]);
            g.setFont(fonts[1].deriveFont(sizes[6]));
            for(Food food : ona.getMealOfDay(school, date).getFood()) {
                g.drawString(food.getName(), offsets[9], offsets[8]+offset+g.getFontMetrics().getAscent());
                offset += g.getFontMetrics().getHeight();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void drawWarning(Graphics2D g, Date date) {
        int offset = 0;
        g.setColor(colors[0]);
        g.setFont(fonts[1].deriveFont(sizes[6]));
        for(String s : getWarnings(date)) {
            g.drawString(s, offsets[10], offsets[8]+offset+g.getFontMetrics().getAscent());
            offset += g.getFontMetrics().getHeight();
        }
    }

    private static String[] getWarnings(Date date) {
        // TODO: DB에서 데이터 쌔벼오기
        return new String[]{"나는 문어, 꿈을 꾸는 무너 ^^"};
    }

    private static String getWeatherMessage() {
        // TODO: 기상청에서 데이터 쌔벼오기
        return "햇빛이 짱짱한 날이에요. 그냥 가서\n" +
                "skrr이나 쳐 하세요;;";
    }

    private static int getCenterOffset(int l, int x) {
        return (l / 2) - (x/2);
    }
}
