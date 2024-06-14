package net.yellowstrawberry;

import me.yellowstrawberry.openneisapi.ONA;
import me.yellowstrawberry.openneisapi.objects.school.School;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static ONA ona;
    public static School school;

    public static void main(String[] args) {
        try {
            ona = new ONA.Builder(args[0]).build();
            school = ona.searchSchool("선린인터넷")[0];
            int i= 0;
            for(BufferedImage ig : ImageRenderer.render()) {
                ImageIO.write(ig, "PNG", new File("./wow%d.png".formatted(i++)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 귀찮 귀찮 귀찮
    }
}