package com.ullarah.rotterbot;

import com.ullarah.rotterbot.modules.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ullarah.rotterbot.Client.pluginEnabled;

public class Messages {

    private static final String getCommandRegex = "\\^([a-zA-Z0-9]+)(?:\\s?)(.*)";
    private static final Pattern commandPattern = Pattern.compile(getCommandRegex);

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
                    String botCommand = null;
                    String[] botArgs = new String[0];
                    Matcher m = commandPattern.matcher(chanSaid);
                    if (m.find()) {
                        botCommand = m.group(1);
                        botArgs = m.group(2).replaceAll("(\\s){2,}", " ").substring(0).split(" ");
                        if (botArgs[0].equals("")) botArgs = new String[0]; // Dirty hack imo. Redo regex?
                    }
                    assert botCommand != null;
                    switch (botCommand.toUpperCase()) {
                        case "ARGS":
                            if (botArgs.length == 0)
                                botReply("You used " + botCommand + " with no arguments", chanCurr);
                            else
                                botReply("You used " + botCommand + " with: " + Arrays.toString(botArgs).
                                        toUpperCase(), chanCurr);
                            break;

                        case "T":
                            if (pluginEnabled("showerthought"))
                                botReply(chanUser + ", " + ShowerThought.randomThought(), chanCurr);
                            break;

                        case "G":
                            if (pluginEnabled("googlesearch")) if (botArgs.length == 0)
                                botReply("[GOOGLE] Yes, let me search for nothing... nothing found!", chanCurr);
                            else {
                                Boolean isSafe = true;
                                if (botArgs[0].toUpperCase().equals("!SAFE")) isSafe = false;
                                String[] result = Google.feelingLucky(Utility.stringJoin(botArgs, " ")
                                        .toUpperCase().replaceFirst("!SAFE ", ""), isSafe);
                                botReply("[GOOGLE] " + result[0] + " | " + result[1], chanCurr);
                            }
                            break;

                        case "UD":
                            if (pluginEnabled("urbandictionary")) if (botArgs.length == 0)
                                botReply("[UD] Yes, let me search for nothing... nothing found!", chanCurr);
                            else {
                                String[] result = UrbanDictionary.getWord(Utility.stringJoin(botArgs, " "));
                                botReply("[UD] " + result[0] + " | " + result[1], chanCurr);
                            }
                            break;

                        case "OSU":
                            if (pluginEnabled("osu")) try {
                                switch (botArgs.length) {
                                    case 0:
                                        botReply("[OSU] Usage: <player> [best|recent]", chanCurr);
                                        break;
                                    case 1:
                                        botReply(Osu.getUserInfo(botArgs[0]), chanCurr);
                                        break;
                                    case 2:
                                        switch (botArgs[1].toUpperCase()) {
                                            case "BEST":
                                                botReply(Osu.getUserType(botArgs[0], "get_user_best"), chanCurr);
                                                break;
                                            case "RECENT":
                                                try {
                                                    botReply(Osu.getUserType(botArgs[0], "get_user_recent"), chanCurr);
                                                } catch (IndexOutOfBoundsException ex) {
                                                    botReply("[OSU] No recent plays.", chanCurr);
                                                }
                                                break;
                                            default:
                                                botReply("[OSU] Usage: <player> [best|recent]", chanCurr);
                                                break;
                                        }
                                        break;
                                }
                            } catch (IndexOutOfBoundsException ex) {
                                botReply("[OSU] Player not found.", chanCurr);
                            }
                            break;

                        case "WA":
                            if (pluginEnabled("wolframalpha")) if (botArgs.length == 0)
                                botReply("[WOLFRAM] Yes, let me search for nothing... nothing found!", chanCurr);
                            else
                                botReply("[WOLFRAM] " + WolframAlpha.calculate(Utility.stringJoin(botArgs, " ")), chanCurr);
                            break;

                        case "YT":
                            if (pluginEnabled("youtube")) if (botArgs.length == 0)
                                botReply("[YOUTUBE] Yes, let me search for nothing... nothing found!", chanCurr);
                            else
                                botReply("[YOUTUBE] " + Youtube.getVideoResult(Utility.stringJoin(botArgs, " ")), chanCurr);
                            break;

                        case "FM":
                            if (pluginEnabled("lastfm")) if (botArgs.length == 0) {
                                botReply("[LASTFM] Usage: <user> [top]", chanCurr);
                                botReply("[LASTFM] Usage: similar <artist> [track]", chanCurr);
                                botReply("[LASTFM] Usage: chart <top|hype>", chanCurr);
                            } else switch (botArgs[0].toUpperCase()) {
                                case "SIMILAR":
                                    String[] similar = Utility.stringJoin(botArgs, " ").replaceFirst("similar", "").split("\\|");
                                    try {
                                        switch (similar.length) {
                                            case 1:
                                                botReply(LastFM.getArtistSimilar(similar[0]), chanCurr);
                                                break;
                                            case 2:
                                                botReply(LastFM.getTrackSimilar(similar[0], similar[1]), chanCurr);
                                                break;
                                            default:
                                                botReply("[LASTFM] Usage: similar <artist> [track]", chanCurr);
                                                break;
                                        }
                                    } catch (ArrayIndexOutOfBoundsException ex) {
                                        ex.printStackTrace();
                                        botReply("[LASTFM] Usage: similar <artist> [track]", chanCurr);
                                    }
                                    break;

                                case "CHART":
                                    try {
                                        switch (botArgs[1].toUpperCase()) {
                                            case "TOP":
                                                botReply(LastFM.getTopTrack(), chanCurr);
                                                break;
                                            case "HYPE":
                                                botReply(LastFM.getHypedTrack(), chanCurr);
                                                break;
                                            default:
                                                botReply("[LASTFM] Usage: chart <top|hype>", chanCurr);
                                                break;
                                        }
                                    } catch (ArrayIndexOutOfBoundsException ex) {
                                        botReply("[LASTFM] Usage: chart <top|hype>", chanCurr);
                                    }
                                    break;

                                default:
                                    try {
                                        switch (botArgs[1].toUpperCase()) {
                                            case "TOP":
                                                botReply(LastFM.getUserTopTrack(botArgs[0]), chanCurr);
                                                break;
                                            default:
                                                botReply("[LASTFM] Usage: <user> [top]", chanCurr);
                                                break;
                                        }
                                    } catch (ArrayIndexOutOfBoundsException ex) {
                                        botReply(LastFM.getUserRecentTrack(botArgs[0]), chanCurr);
                                    }
                                    break;
                            }
                            break;

                        case "HUNGRY":
                            if (pluginEnabled("bigoven")) botReply(BigOven.recipe(), chanCurr);
                            break;

                        case "RECALL":
                            if (pluginEnabled("recall")) {
                                String user = null;
                                if (botArgs.length == 0) user = chanUser;
                                if (Utility.getLastMessage(user) == null)
                                    botReply("[RECALL] They haven't said anything?", chanCurr);
                                else botReply("[RECALL] " + Utility.getLastMessage(user), chanCurr);
                            }
                            break;

                        default:
                            botReply("That's no command I've heard of " + chanUser + "?", chanCurr);
                            break;
                    }
                } else if (chanSaid.matches("s/(.*)/(.*)")) try {
                    String[] sedString = chanSaid.split("/", 3);
                    if (Utility.getLastMessage(chanUser).contains(sedString[1])) {
                        String replacedMessage = Utility.getLastMessage(chanUser).replaceAll(sedString[1], sedString[2]);
                        botReply(chanUser + Colour.BOLD + " meant to say " + Colour.RESET + replacedMessage, chanCurr);
                    }
                } catch (NullPointerException ignored) {
                }
                else if (Youtube.isVideo(chanSaid)) if (pluginEnabled("youtube"))
                    botReply("[YOUTUBE] " + Youtube.getVideoInfo(Youtube.getVideoID(chanSaid)), chanCurr);
                else if (pluginEnabled("privilege")) Privilege.checkYour(chanUser, chanCurr, chanSaid);
                if (pluginEnabled("recall")) Utility.setLastMessage(chanUser, chanSaid);
                break;

            case "JOIN":
                if (pluginEnabled("welcome")) if (chanUser.equals(Client.getNickname())) {
                    Thread.sleep(5000);
                    botReply("Howdy everybody!", chanCurr);
                } else botReply("Howdy " + chanUser + "!", chanCurr);
                break;

        }

    }

}
