package net.yellowstrawberry;

import me.yellowstrawberry.openneisapi.ONA;
import me.yellowstrawberry.openneisapi.objects.school.School;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Main extends ListenerAdapter {

    public static ONA ona;
    public static School school;
    public static String apiKey;


    public static void main(String[] args) {
        JDA j = JDABuilder.createLight(System.getProperty("jda"), EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(new Main())
                .build();
        j.updateCommands().addCommands(
                Commands.slash("info", "시간표 정보 라인 수정")
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                        .addOption(OptionType.STRING, "date", "날짜 (2024/09/30 형식)", true)
                        .addOption(OptionType.INTEGER, "class", "교시 (1부터 시작)", true)
                        .addOption(OptionType.STRING, "contents", "정보라인에 들어갈 거 (길면 \\n으로 줄 바꿈해줘)", true),
                Commands.slash("warning", "경고 추가")
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                        .addSubcommands(
                                new SubcommandData("add", "추가")
                                        .addOption(OptionType.STRING, "date", "날짜 (2024/09/30 형식)", true)
                                        .addOption(OptionType.STRING, "contents", "경고에 들아갈거 (길면 \\n으로 줄 바꿈해줘)", true),
                                new SubcommandData("remove", "제거")
                                        .addOption(OptionType.STRING, "date", "날짜 (2024/09/30 형식)", true)
                                        .addOption(OptionType.INTEGER, "index", "위치 (0부터 시작)", true),
                                new SubcommandData("preview", "미리보기 (embed 형식)")
                                        .addOption(OptionType.STRING, "date", "날짜 (2024/09/30 형식)", true)
                        ),
                Commands.slash("test", "테스트")
        ).queue();
        try {
            apiKey = System.getProperty("cloud");
            ona = new ONA.Builder(System.getProperty("neis")).build();
            school = ona.searchSchool("선린인터넷")[0];
            new TaskExecutor(() -> {
                int i = new Calendar.Builder().setTimeZone(TimeZone.getTimeZone("Asia/Seoul")).setInstant(new Date()).build().get(Calendar.DAY_OF_WEEK);
                if(i==1||i==7) return;

                InputStream[] streams = ImageToStream(ImageRenderer.render(new Date()));
                j.getGuildById(1225011335352418325L).getTextChannelById(1229076484086431854L).sendFiles(FileUpload.fromData(
                        streams[0], "0.png"
                )).queue(s -> s.getJDA().getGuildById(1225011335352418325L).getTextChannelById(1229076484086431854L).sendFiles(FileUpload.fromData(
                        streams[1], "1.png"
                )).queue(e -> e.getJDA().getGuildById(1225011335352418325L).getTextChannelById(1229076484086431854L).sendFiles(FileUpload.fromData(
                        streams[2], "2.png"
                )).queue()));
            }).startExecutionAt(5, 29, 56);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream[] ImageToStream(BufferedImage[] image) {
        InputStream[] streams = new InputStream[image.length];
        for(int i=0;i< image.length;i++) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(image[i], "PNG", os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            streams[i] = new ByteArrayInputStream(os.toByteArray());
        }
        return streams;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("info")) {
            if(event.getOption("date").getAsString().split("/").length==3) {
                try(ResultSet set = SQLCommunicator.executeQuery("SELECT 1 FROM `info` WHERE date=? AND class=?;", event.getOption("date").getAsString().replaceAll("/", "-"), event.getOption("class").getAsInt())) {
                    if(set.next()) {
                        SQLCommunicator.executeUpdate("UPDATE `info` SET `contents`=? WHERE date=? AND class=?;", event.getOption("contents"), event.getOption("date").getAsString().replaceAll("/", "-"), event.getOption("class").getAsInt());
                    }else {
                        SQLCommunicator.executeUpdate("INSERT INTO `info` (`date`, `class`, `contents`) VALUES (?,?,?);", event.getOption("date").getAsString().replaceAll("/", "-"), event.getOption("class").getAsInt(), event.getOption("contents").getAsString());
                    }
                } catch (SQLException e) {
                    event.reply("아 오류남;; 정은수 멘션해").queue();
                    throw new RuntimeException(e);
                }
                event.reply("굳굳").setEphemeral(true).queue();
            }else {
                event.reply("배드배드;; 날짜 형태 확인 바람").setEphemeral(true).queue();
            }
        }else if(event.getName().equals("warning")) {
            if(event.getOption("date").getAsString().split("/").length==3) {
                String s = event.getOption("date").getAsString().replaceAll("/", "-");
                List<String> data = getWarningData(s);
                boolean changed = false;

                if(event.getSubcommandName().equals("add")) {
                    data.add(event.getOption("contents").getAsString());
                    changed = true;
                }else if(event.getSubcommandName().equals("remove")) {
                    try {
                        data.remove(event.getOption("index").getAsInt());
                        changed = true;
                    }catch (IndexOutOfBoundsException e) {
                        event.reply("배드배드;; 위치 확인 바람").setEphemeral(true).queue();
                        return;
                    }
                }else if(event.getSubcommandName().equals("preview")) {
                    EmbedBuilder b = new EmbedBuilder();
                    b.setTitle("⚠️경고");
                    for(int i=0;i<data.size(); i++) {
                        b.addField(i+". "+data.get(i),"",false);
                    }
                    event.replyEmbeds(b.build()).queue();
                }


                if(changed) {
                    String a = formatWarningData(data);
                    SQLCommunicator.executeUpdate("INSERT INTO `warning` (`date`, `contents`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `date`=?, `contents`=?;", s, a, s, a);
                    event.reply("굳굳").setEphemeral(true).queue();
                }
            }else {
                event.reply("배드배드;; 날짜 형태 확인 바람").setEphemeral(true).queue();
            }
        }else if(event.getName().equals("test")) {
            InputStream[] streams = ImageToStream(ImageRenderer.render(new Date()));
            event.getJDA().getGuildById(1225011335352418325L).getTextChannelById(1229076484086431854L).sendFiles(FileUpload.fromData(
                    streams[0], "0.png"
            )).queue(s -> s.getJDA().getGuildById(1225011335352418325L).getTextChannelById(1229076484086431854L).sendFiles(FileUpload.fromData(
                    streams[1], "1.png"
            )).queue(e -> e.getJDA().getGuildById(1225011335352418325L).getTextChannelById(1229076484086431854L).sendFiles(FileUpload.fromData(
                    streams[2], "2.png"
            )).queue()));
        }
    }

    public static List<String> getWarningData(String d) {
        List<String> data = new ArrayList<>();
        try(ResultSet set = SQLCommunicator.executeQuery("SELECT * FROM `warning` WHERE `date`=?;", d)) {
            if(set.next()) {
                data.addAll(Arrays.asList(set.getString("contents").split(",___spliter___,")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public static String[] getClassData(String d) {
        String[] data = new String[7];
        try(ResultSet set = SQLCommunicator.executeQuery("SELECT * FROM `info` WHERE `date`=?;", d)) {
            while (set.next()) {
                if(set.getInt("class")<8) data[set.getInt("class")-1] = set.getString("contents");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    private static String formatWarningData(List<String> l) {
        Queue<String> q = new LinkedBlockingQueue<>(l);
        StringBuilder sb = new StringBuilder();
        sb.append(q.poll());
        while (!q.isEmpty()) {
            sb.append(",___spliter___,").append(q.poll());
        }
        return sb.toString();
    }
}