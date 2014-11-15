package com.ullarah.rotterbot;

import com.ullarah.rotterbot.modules.Colour;
import com.ullarah.rotterbot.modules.Privilege;
import com.ullarah.rotterbot.modules.Youtube;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import static com.ullarah.rotterbot.Client.*;
import static com.ullarah.rotterbot.Commands.*;

public class Messages {

    public static final HashMap<String, String> recallMessage = new HashMap<>();

    public static final HashMap<String, ArrayList> tellMessage = new HashMap<>();
    public static final HashMap<String, HashMap> tellUser = new HashMap<>();

    private static final String getCommandRegex = "\\^([a-zA-Z0-9]+)(?:\\s?)(.*)";
    public static final Pattern commandPattern = Pattern.compile(getCommandRegex);

    private static final String getSedRegex = "s/(.*)/(.*)";
    private static final Pattern sedPattern = Pattern.compile(getSedRegex);

    private static String chanUser = null;
    private static String chanCurr = null;
    private static String chanSaid = null;

    public static void sendRaw(String raw) throws IOException {
        Client.writer.write(raw + "\r\n");
        Client.writer.flush();
    }

    public static void botMessage(String s, String c) throws IOException {
        sendRaw("PRIVMSG " + c + " :" + s);
    }

    public static void botAction(String s, String c) throws IOException {
        sendRaw("PRIVMSG " + c + " :\u0001ACTION " + s + "\u0001");
    }

    public static void servMessage(String msg) throws IOException, InterruptedException, ParseException {

        String[] chanRaw = msg.split(" ", 4);

        try {
            chanUser = chanRaw[0].split("!")[0].substring(1);
            chanCurr = chanRaw[2];
            chanSaid = chanRaw[3].substring(1);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        assert chanSaid != null;

        switch (chanRaw[1]) {

            case "PRIVMSG":
                pingtime = System.currentTimeMillis();

                if (chanSaid.equals("ping")) botMessage("pong", chanCurr);

                if (commandPattern.matcher(chanSaid).find()) {
                    if (commandCountWarning.size() == 0 && commandCount.size() == 0) {
                        commandCountWarning.put(chanCurr, false);
                        commandCount.put(chanCurr, 0);
                    }
                    if (!commandCountWarning.get(chanCurr)) if (commandCount.get(chanCurr) >= 5) {
                        commandCountWarning.put(chanCurr, true);
                        botMessage(Colour.BOLD + Colour.RED + "Just wait a damn minute! Geez...", chanCurr);
                    } else {
                        commandCount.put(chanCurr, commandCount.containsKey(chanCurr)
                                ? commandCount.get(chanCurr) + 1 : 1);
                        getCommand(chanCurr, chanUser, chanSaid);
                    }
                }

                if (chanSaid.toLowerCase().contains(getNickname().toLowerCase())) if (getPluginEnabled("insults"))
                    botMessage(getRandomMessage("insults", chanUser), chanCurr);

                if (chanSaid.startsWith("s/") && sedPattern.matcher(chanSaid).find()) try {
                    String[] sedString = chanSaid.split("/", 3);
                    if (Utility.getLastMessage(chanUser).contains(sedString[1])) {
                        String replacedMessage = Utility.getLastMessage(chanUser).replaceAll(sedString[1], sedString[2]);
                        botMessage(chanUser + Colour.RESET + Colour.BOLD + " meant to say " +
                                Colour.RESET + replacedMessage, chanCurr);
                    }
                } catch (NullPointerException ignored) {
                }

                if (Youtube.isVideo(chanSaid) && getPluginEnabled("youtube"))
                    botMessage("[YOUTUBE] " + Youtube.getVideoInfo(Youtube.getVideoID(chanSaid)), chanCurr);

                if (getPluginEnabled("privilege")) Privilege.checkYour(chanUser, chanCurr, chanSaid);

                if ((!commandPattern.matcher(chanSaid).find() || !sedPattern.matcher(chanSaid).find())
                        && getPluginEnabled("recall"))
                    Utility.setLastMessage(chanUser, chanSaid);
                break;

            case "JOIN":
                sendRaw("NAMES " + chanCurr);
                if (getPluginEnabled("welcome")) if (chanUser.equals(Client.getNickname())) {
                    Thread.sleep(2500);
                    botMessage("Howdy everybody!", chanCurr);
                } else if (tellUser.containsKey(chanUser.toLowerCase())) {
                    botMessage("Hey " + chanUser + "? Got a message for you!", chanCurr);
                    for( Object message : tellMessage.get(chanUser)) botMessage("[TELL] " + message, chanCurr);
                    tellMessage.remove(chanUser);
                    tellUser.remove(chanUser);
                } else botMessage("Howdy " + chanUser + "!", chanCurr);

                break;

            case "PART":
                sendRaw("NAMES " + chanCurr);
                break;

            case "QUIT":
                sendRaw("NAMES " + chanCurr);
                break;

        }

    }

    public static String getRandomMessage(String type, String user) throws IOException, ParseException {

        FileReader botReplies = new FileReader("messages.json");
        JSONObject jsonObject = (JSONObject) jsonParser.parse(botReplies);
        botReplies.close();

        JSONArray jsonReply = (JSONArray) jsonObject.get(type);
        String gotInsult = (String) jsonReply.get(Utility.randInt(0,jsonReply.size()-1));

        return gotInsult.replaceAll("#s", user);

    }

}
