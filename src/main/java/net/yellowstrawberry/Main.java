package net.yellowstrawberry;

import me.yellowstrawberry.openneisapi.ONA;
import me.yellowstrawberry.openneisapi.objects.school.School;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.TimeZone;

public class Main extends ListenerAdapter {

    public static ONA ona;
    public static School school;
    public static String apiKey;

    public static void main(String[] args) {
//        Weather.requestWeather();
        JDA j = JDABuilder.createLight(System.getProperty("jda"), EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(new Main())
                .build();
        j.updateCommands().addCommands(
                Commands.slash("info", "시간표 정보 라인 수정")
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                        .addSubcommands(
                            new SubcommandData("add", "추가")
                                    .addOption(OptionType.STRING, "date", "날짜 (2024/09/30 형식)", true)
                                    .addOption(OptionType.INTEGER, "class", "교시 (1부터 시작)", true)
                                    .addOption(OptionType.STRING, "contents", "정보라인에 들어갈 거 (길면 \\n으로 줄 바꿈해줘)", true),
                            new SubcommandData("remove", "제거")
                                    .addOption(OptionType.STRING, "date", "날짜 (2024/09/30 형식)", true)
                                    .addOption(OptionType.INTEGER, "class", "교시 (1부터 시작)", true)
                                    .addOption(OptionType.INTEGER, "index", "위치 (0부터 시작)"),
                            new SubcommandData("preview", "미리보기 (embed 형식)")
                        ),
                Commands.slash("warning", "경고 추가")
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                        .addSubcommands(
                                new SubcommandData("add", "추가")
                                        .addOption(OptionType.STRING, "date", "날짜 (2024/09/30 형식)", true)
                                        .addOption(OptionType.STRING, "contents", "경고에 들아갈거 (길면 \\n으로 줄 바꿈해줘)"),
                                new SubcommandData("remove", "제거")
                                        .addOption(OptionType.STRING, "date", "날짜 (2024/09/30 형식)", true)
                                        .addOption(OptionType.INTEGER, "index", "위치 (0부터 시작)", true)
                        )
        ).queue();
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