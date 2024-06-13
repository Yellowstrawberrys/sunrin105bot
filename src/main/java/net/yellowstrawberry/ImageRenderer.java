package net.yellowstrawberry;

import me.yellowstrawberry.openneisapi.objects.food.Food;

import java.awt.*;
import java.awt.image.BufferedImage;
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
            163, // 요일 위쪽
            207, // 날씨 설명 위쪽

            433, // 시간표 위쪽
            40, // 시간표 내용 위쪽
            45+20, // 시간표 교시 왼쪽
            45+56, // 시간표 과목 왼쪽
            45+113, // 시간표 | 왼쪽
            45+126, // 시간표 안내사항 왼쪽

            759+55, // 급식, 주의사항 위쪽
            45+35, // 급식 왼쪽
            326+35, // 주의사항 왼쪽
    };

    private static final float[] sizes = {
            32, // 요일 폰트 사이즈
            20, // 날씨 설명 폰트 사이즈

            280, // 시간표 전체 높이
            11, // 시간표 교시 폰트 사이즈
            17, // 시간표 과목 폰트 사이즈
            13, // 사간표 그외 폰트 사이즈

            16 // 급식, 주의사항 폰트 사이즈
    };

    private static final Font[] fonts = {
            // TODO: Regular, Medium, SemiBold, ExtraBold
    };

    private static final Color[] colors = {
            new Color(234, 234, 234),
            new Color(187, 187, 187),
            new Color(149, 149, 149)
    };


    static  {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
    }


    private BufferedImage loadBackground() {
        // TODO;
        return null;
    }

    public BufferedImage render() {
        BufferedImage image = loadBackground();
        Graphics2D g = image.createGraphics();

        Date date = new Date();


        drawHeader(image.getWidth(), g, date);
        drawTimetable(g);
        drawMeal(g, date);
        drawWarning(g, date);

        return image;
    }

    private void drawHeader(int width, Graphics2D g, Date date) {
        String s = DATE_FORMAT.format(date);
        g.setFont(fonts[3].deriveFont(sizes[0]));
        g.setColor(colors[0]);
        g.drawString(s, getCenterOffset(width, g.getFontMetrics().stringWidth(s)), offsets[0]);

        g.setFont(fonts[0].deriveFont(sizes[1]));
        g.setColor(colors[1]);
        int off = 0;
        for(String a : getWeatherMessage().split("\n")) {
            g.drawString(a, getCenterOffset(width, g.getFontMetrics().stringWidth(a)), offsets[1]+off);
            off += (int) (sizes[1]+4);
        }
    }


    private void drawTimetable(Graphics2D g) {
        String[][] table = TimeTable.getPeriod();
        for (int i=0; i<table.length; i++) {
            int offset = offsets[3]*i;
            for (int j=0; j<4; j++) {
                g.setColor(colors[j%2==0?0:2]);
                g.setFont(fonts[j==1?2:1].deriveFont(sizes[3+j]));
                g.drawString(table[i][j], offsets[3+j], offset+getCenterOffset(offsets[3], g.getFontMetrics().getHeight()));
            }
        }
    }

    private void drawMeal(Graphics2D g, Date date) {
        try {
            int offset = 0;
            g.setColor(colors[0]);
            g.setFont(fonts[1]);
            for(Food food : ona.getMealOfDay(school, date).getFood()) {
                g.drawString(food.getName(), offsets[9], offsets[8]+offset);
                offset += g.getFontMetrics().getHeight();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawWarning(Graphics2D g, Date date) {
        int offset = 0;
        g.setColor(colors[0]);
        g.setFont(fonts[1]);
        for(String s : getWarnings(date)) {
            g.drawString(s, offsets[10], offsets[8]+offset);
            offset += g.getFontMetrics().getHeight();
        }
    }

    private String[] getWarnings(Date date) {
        // TODO: DB에서 데이터 쌔벼오기
        return null;
    }

    private String getWeatherMessage() {
        // TODO: 기상청에서 데이터 쌔벼오기
        return "";
    }

    private int getCenterOffset(int l, int x) {
        return (l / 2) - x;
    }
}
