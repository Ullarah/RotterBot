package com.ullarah.rotterbot;

import com.ullarah.rotterbot.modules.Colour;
import com.ullarah.rotterbot.modules.Privilege;
import com.ullarah.rotterbot.modules.Youtube;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import static com.ullarah.rotterbot.Client.*;
import static com.ullarah.rotterbot.Commands.*;

public class Messages {

    public static final HashMap<String, String> recallMessage = new HashMap<>();
    public static final HashMap<String, String> tellMessage = new HashMap<>();

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

    public static void botReply(String s, String c) throws IOException {
        sendRaw("PRIVMSG " + c + " :" + s);
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
                if (chanSaid.equals("ping")) botReply("pong", chanCurr);

                if (commandPattern.matcher(chanSaid).find()) {
                    if (commandCountWarning.size() == 0 && commandCount.size() == 0) {
                        commandCountWarning.put(chanCurr, false);
                        commandCount.put(chanCurr, 0);
                    }
                    if (!commandCountWarning.get(chanCurr)) if (commandCount.get(chanCurr) >= 5) {
                        commandLimit(chanCurr);
                        commandCountWarning.put(chanCurr, true);
                        botReply(Colour.BOLD + Colour.RED + "Just wait a damn minute! Geez...", chanCurr);
                    } else {
                        commandCount.put(chanCurr, commandCount.containsKey(chanCurr)
                                ? commandCount.get(chanCurr) + 1 : 1);
                        getCommand(chanCurr, chanUser, chanSaid);
                    }
                }

                if (sedPattern.matcher(chanSaid).find()) try {
                    String[] sedString = chanSaid.split("/", 3);
                    if (Utility.getLastMessage(chanUser).contains(sedString[1])) {
                        String replacedMessage = Utility.getLastMessage(chanUser).replaceAll(sedString[1], sedString[2]);
                        botReply(chanUser + Colour.RESET + Colour.BOLD + " meant to say " +
                                Colour.RESET + replacedMessage, chanCurr);
                    }
                } catch (NullPointerException ignored) {
                }

                if (Youtube.isVideo(chanSaid) && pluginEnabled("youtube"))
                    botReply("[YOUTUBE] " + Youtube.getVideoInfo(Youtube.getVideoID(chanSaid)), chanCurr);

                if (pluginEnabled("privilege")) Privilege.checkYour(chanUser, chanCurr, chanSaid);

                if ((!commandPattern.matcher(chanSaid).find() || !sedPattern.matcher(chanSaid).find())
                        && pluginEnabled("recall"))
                    Utility.setLastMessage(chanUser, chanSaid);
                break;

            case "JOIN":
                sendRaw("NAMES " + chanCurr);
                if (pluginEnabled("welcome")) if (chanUser.equals(Client.getNickname())) {
                    Thread.sleep(2500);
                    botReply("Howdy everybody!", chanCurr);
                } else if (tellMessage.containsKey(chanUser.toLowerCase())) {
                    botReply("Hey " + chanUser + "? Got a message for you!", chanCurr);
                    botReply("[TELL] " + tellMessage.get(chanUser.toLowerCase()), chanCurr);
                    tellMessage.remove(chanUser);
                } else botReply("Howdy " + chanUser + "!", chanCurr);

                break;

            case "PART":
                sendRaw("NAMES " + chanCurr);
                break;

            case "QUIT":
                sendRaw("NAMES " + chanCurr);
                break;

        }

    }

}
