package net.yellowstrawberry;

import me.yellowstrawberry.openneisapi.ONA;
import me.yellowstrawberry.openneisapi.objects.school.School;

import java.io.IOException;

public class Main {

    public static ONA ona;
    public static School school;

    public static void main(String[] args) {
        try {
            ona = new ONA.Builder("토큰").build();
            school = ona.searchSchool("선린인터넷고등학교")[0];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 귀찮 귀찮 귀찮
    }
}