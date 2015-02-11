package com.ullarah.rotterbot;

import com.ullarah.rotterbot.modules.Colour;
import com.ullarah.rotterbot.modules.Youtube;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ullarah.rotterbot.Client.*;
import static com.ullarah.rotterbot.Commands.*;
import static com.ullarah.rotterbot.Utility.levelType.CHAT;
import static com.ullarah.rotterbot.Utility.levelType.INFO;
import static com.ullarah.rotterbot.Utility.showLevelMessage;
import static com.ullarah.rotterbot.modules.Privilege.*;
import static com.ullarah.rotterbot.modules.TitleExtractor.*;

public class Messages {

    public static final HashMap<String, String> recallMessage = new HashMap<>();

    public static final HashMap<String, ArrayList> tellMessage = new HashMap<>();
    public static final HashMap<String, HashMap> tellUser = new HashMap<>();

    public static final List<String> ignoreUserList = new ArrayList<>();

    private static final String getCommandRegex = "\\^([a-zA-Z0-9]+)(?:\\s?)(.*)";
    public static final Pattern commandPattern = Pattern.compile(getCommandRegex);

    private static final String getMinecraftRegex = ".{12}\\[.+\\].{3}(.*).+:";
    public static final Pattern minecraftPattern = Pattern.compile(getMinecraftRegex);

    private static final String getURLRegex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
    public static final Pattern urlPattern = Pattern.compile(getURLRegex);

    private static final String getSedRegex = "s/(.*)/(.*)";
    private static final Pattern sedPattern = Pattern.compile(getSedRegex);

    public static void sendRawMessage(String s) throws IOException {
        writer.write(s + "\r\n");
        writer.flush();
    }

    public static void sendMessage(String s, String c) throws IOException {
        sendRawMessage("PRIVMSG " + c + " :" + s + "\r\n");
    }

    public static void sendAction(String s, String c) throws IOException {
        sendRawMessage("PRIVMSG " + c + " :\u0001ACTION " + s + "\u0001" + "\r\n");
    }

    public static void servMessage(String raw, String prefix, String type, String destination, String message)
            throws IOException, InterruptedException, ParseException {

        String user = "";
        if (prefix != null) user = prefix.split("!")[0];

        switch (type) {

            case "PING":
                sendRawMessage("PONG " + raw.substring(5));
                if (getDebug()) showLevelMessage(INFO, "PONG " + raw.substring(5));
                pingtime = System.currentTimeMillis();
                break;

            case "353":
                refreshUserList(raw.split(" ", 6)[4], raw.split(" ", 6)[5].split(" "));
                break;

            case "376":
                sendRawMessage("PRIVMSG NickServ :IDENTIFY " + getPassword());
                showLevelMessage(INFO, "IDEN " + getLogin());
                break;

            case "NOTICE":
                if (raw.contains("identified")) joinPresetChannels();
                break;

            case "PRIVMSG":
                pingtime = System.currentTimeMillis();
                showLevelMessage(CHAT, "[" + destination + "] " + "[" + user + "] " + message);

                if (message.equals("ping")) {
                    sendMessage("pong", destination);
                    break;
                }

                if (commandPattern.matcher(message).find()) {

                    if(!commandCountWarning.containsKey(destination)) commandCountWarning.put(destination, false);
                    if(!commandCount.containsKey(destination)) commandCount.put(destination, 0);

                    if (!commandCountWarning.get(destination)) {
                        if (commandCount.get(destination) >= 5) {
                            commandCountWarning.put(destination, true);
                            sendMessage(Colour.BOLD + Colour.RED + "Just wait a damn minute! Geez...", destination);
                        } else {
                            if (commandCount.containsKey(destination)) {
                                commandCount.put(destination, commandCount.get(destination) + 1);
                            }
                            else {
                                commandCount.put(destination, 1);
                            }
                            getCommand(destination, user, message);
                        }
                    }
                    break;
                }

                if (message.toLowerCase().contains(getNickname().toLowerCase())) if (getPluginEnabled("replies"))
                    if (!ignoreUserList.contains(user.toLowerCase())) {
                        Matcher isMinecraft = minecraftPattern.matcher(message);
                        if( isMinecraft.find()){
                            user = isMinecraft.group(1);
                            messageQueue(getRandomMessage("replies", user, destination), destination);
                        } else {
                            messageQueue(getRandomMessage("replies", user, destination), destination);
                        }
                        break;
                    }

                if (message.startsWith("s/") && sedPattern.matcher(message).find()) try {
                    String[] sedString = message.split("/", 3);
                    if (Utility.getLastMessage(user).contains(sedString[1])) {
                        String replacedMessage = Utility.getLastMessage(user).replaceAll(sedString[1], sedString[2]);
                        messageQueue(user + Colour.RESET + Colour.BOLD + " meant to say " +
                                Colour.RESET + replacedMessage, destination);
                    }
                    break;
                } catch (NullPointerException ignored) {
                }

                if (Youtube.isVideo(message) && getPluginEnabled("youtube")) {
                    messageQueue("[YOUTUBE] " + Youtube.getVideoInfo(Youtube.getVideoID(message)), destination);
                    break;
                }

                if(urlPattern.matcher(message).find()){
                    ArrayList<String> urls = getLinks(message);
                    for(String link : urls) messageQueue("[URL] " + getPageTitle(link), destination);
                    break;
                }

                if (getPluginEnabled("privilege")) {
                    checkYour(user, destination, message);
                    break;
                }

                if ((!commandPattern.matcher(message).find() || !sedPattern.matcher(message).find())
                        && getPluginEnabled("recall")) {
                    Utility.setLastMessage(user, message);
                    break;
                }
                break;

            case "JOIN":
                sendRawMessage("NAMES " + destination);
                if (getPluginEnabled("welcome")) if (user.equals(getNickname())) {
                    Thread.sleep(2500);
                    messageQueue("Howdy everybody!", destination);
                } else if (tellUser.containsKey(user.toLowerCase())) {
                    messageQueue("Hey " + user + "? Got a message for you!", destination);
                    for (Object note : tellMessage.get(user)) messageQueue("[TELL] " + note, destination);
                    tellMessage.remove(user);
                    tellUser.remove(user);
                } else if (!ignoreUserList.contains(user.toLowerCase()))
                    messageQueue(getRandomMessage("join", user, destination), destination);
                break;

            case "PART":
                sendRawMessage("NAMES " + destination);
                break;

            case "QUIT":
                sendRawMessage("NAMES " + destination);
                break;

        }

    }

    private static String getRandomMessage(String type, String user, String channel) throws IOException, ParseException {

        FileReader botReplies = new FileReader("messages.json");
        JSONObject jsonObject = (JSONObject) jsonParser.parse(botReplies);
        botReplies.close();

        JSONArray jsonReply;

        switch (type.toUpperCase()) {

            case "REPLIES":
                JSONObject jsonReplyObject = (JSONObject) jsonObject.get("replies");
                if (botAttitude.get(channel).equals("nice")) {
                    jsonReply = (JSONArray) jsonReplyObject.get("nice");
                    return jsonReply.get(Utility.randInt(0, jsonReply.size() - 1)).toString().replaceAll("#s", user);
                } else {
                    jsonReply = (JSONArray) jsonReplyObject.get("naughty");
                    return jsonReply.get(Utility.randInt(0, jsonReply.size() - 1)).toString().replaceAll("#s", user);
                }

            case "JOIN":
                jsonReply = (JSONArray) jsonObject.get("join");
                return jsonReply.get(Utility.randInt(0, jsonReply.size() - 1)).toString().replaceAll("#s", user);

            default:
                return "Uhhh...";

        }

    }

    private static ArrayList<String> getLinks(String text) {
        ArrayList<String> links = new ArrayList<>();
        Matcher m = urlPattern.matcher(text);
        while(m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")"))
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            links.add(urlStr);
        }
        return links;
    }

    public static void messageQueue(final String s, final String c) {
        botMessages.put(s,c);
    }

    public static void deliverQueue() {

        Runnable resetCommandLimit = () -> {
            if(!botMessages.isEmpty()) {
                Iterator it = botMessages.entrySet().iterator();
                while (it.hasNext()) try {
                    Entry m = (Entry) it.next();
                    String s = (String) m.getKey();
                    String c = (String) m.getValue();
                    sendMessage(s, c);
                    it.remove();
                    Thread.sleep(2000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                if (getDebug()) showLevelMessage(INFO, "Messages sent from queue");
            } else if (getDebug()) showLevelMessage(INFO, "Message queue empty");
        };

        queueDeliveryExecutor = Executors.newScheduledThreadPool(1);
        queueDeliveryExecutor.scheduleAtFixedRate(resetCommandLimit, 0, 5, TimeUnit.SECONDS);

    }

}
