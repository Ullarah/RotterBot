package com.ullarah.rotterbot;

import com.ullarah.rotterbot.modules.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import static com.ullarah.rotterbot.Client.*;
import static com.ullarah.rotterbot.Messages.*;
import static com.ullarah.rotterbot.Utility.levelType.INFO;
import static com.ullarah.rotterbot.Utility.showLevelMessage;

class Commands {

    public static final HashMap<String, Integer> commandCount = new HashMap<>();
    public static final HashMap<String, Boolean> commandCountWarning = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static void getCommand(String chanCurr, String chanUser, String chanSaid)
            throws IOException, ParseException {

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
                messageQueue(botArgs.length == 0 ? "You used " + botCommand + " with no arguments"
                        : "You used " + botCommand + " with: " + Arrays.toString(botArgs).toUpperCase(), chanCurr);
                break;

            case "T":
                if (getPluginEnabled("showerthought"))
                    messageQueue(ShowerThought.randomThought(), chanCurr);
                break;

            case "G":
                Boolean isSafe = true;
                if (getPluginEnabled("googlesearch")) if (botArgs.length == 0)
                    messageQueue("[GOOGLE] Yes, let me search for nothing... nothing found!", chanCurr);
                else {
                    if (botArgs[0].toUpperCase().equals("!SAFE")) isSafe = false;
                    messageQueue(Google.feelingLucky(Utility.stringJoin(botArgs, " ")
                            .toUpperCase().replaceFirst("!SAFE ", ""), isSafe), chanCurr);
                }
                break;

            case "UD":
                if (getPluginEnabled("urbandictionary"))
                    messageQueue(botArgs.length == 0 ? "[UD] Yes, let me search for nothing... nothing found!"
                            : UrbanDictionary.getWord(Utility.stringJoin(botArgs, " ")), chanCurr);
                break;

            case "OSU":
                if (getPluginEnabled("osu")) try {
                    switch (botArgs.length) {

                        case 0:
                            messageQueue("[OSU] Usage: <player> [best|recent]", chanCurr);
                            break;

                        case 1:
                            messageQueue(Osu.getUserInfo(botArgs[0]), chanCurr);
                            break;

                        case 2:
                            switch (botArgs[1].toUpperCase()) {

                                case "BEST":
                                    messageQueue(Osu.getUserType(botArgs[0], "get_user_best"), chanCurr);
                                    break;

                                case "RECENT":
                                    try {
                                        messageQueue(Osu.getUserType(botArgs[0], "get_user_recent"), chanCurr);
                                    } catch (IndexOutOfBoundsException ex) {
                                        messageQueue("[OSU] No recent plays.", chanCurr);
                                    }
                                    break;

                                default:
                                    messageQueue("[OSU] Usage: <player> [best|recent]", chanCurr);
                                    break;

                            }
                            break;

                        case 3:
                            if (botArgs[1].equals("vs")) messageQueue(Osu.getVersus(botArgs[0], botArgs[2]), chanCurr);
                            break;

                    }
                } catch (IndexOutOfBoundsException ex) {
                    messageQueue("[OSU] Player not found.", chanCurr);
                }
                break;

            case "WA":
                if (getPluginEnabled("wolframalpha"))
                    messageQueue(botArgs.length == 0 ? "[WOLFRAM] Yes, let me search for nothing... nothing found!"
                            : "[WOLFRAM] " + WolframAlpha.calculate(Utility.stringJoin(botArgs, " ")), chanCurr);
                break;

            case "YT":
                if (getPluginEnabled("youtube"))
                    messageQueue(botArgs.length == 0 ? "[YOUTUBE] Yes, let me search for nothing... nothing found!"
                            : "[YOUTUBE] " + Youtube.getVideoResult(Utility.stringJoin(botArgs, " ")), chanCurr);
                break;

            case "FM":
                if (getPluginEnabled("lastfm")) if (botArgs.length == 0) {
                    messageQueue("[LASTFM] Usage: <user> [top]", chanCurr);
                    messageQueue("[LASTFM] Usage: similar <artist> [track]", chanCurr);
                    messageQueue("[LASTFM] Usage: chart <top|hype>", chanCurr);
                } else switch (botArgs[0].toUpperCase()) {

                    case "SIMILAR":
                        String[] similar = Utility.stringJoin(botArgs, " ").replaceFirst("similar", "").split("\\|");
                        try {
                            switch (similar.length) {

                                case 1:
                                    messageQueue(LastFM.getArtistSimilar(similar[0]), chanCurr);
                                    break;

                                case 2:
                                    messageQueue(LastFM.getTrackSimilar(similar[0], similar[1]), chanCurr);
                                    break;

                                default:
                                    messageQueue("[LASTFM] Usage: similar <artist> [track]", chanCurr);
                                    break;

                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            messageQueue("[LASTFM] Usage: similar <artist> [track]", chanCurr);
                        }
                        break;

                    case "CHART":
                        try {
                            switch (botArgs[1].toUpperCase()) {

                                case "TOP":
                                    messageQueue(LastFM.getTopTrack(), chanCurr);
                                    break;

                                case "HYPE":
                                    messageQueue(LastFM.getHypedTrack(), chanCurr);
                                    break;

                                default:
                                    messageQueue("[LASTFM] Usage: chart <top|hype>", chanCurr);
                                    break;

                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            messageQueue("[LASTFM] Usage: chart <top|hype>", chanCurr);
                        }
                        break;

                    default:
                        try {
                            switch (botArgs[1].toUpperCase()) {

                                case "TOP":
                                    messageQueue(LastFM.getUserTopTrack(botArgs[0]), chanCurr);
                                    break;

                                default:
                                    messageQueue("[LASTFM] Usage: <user> [top]", chanCurr);
                                    break;

                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            messageQueue(LastFM.getUserRecentTrack(botArgs[0]), chanCurr);
                        }
                        break;
                }
                break;

            case "HUNGRY":
                if (getPluginEnabled("bigoven")) messageQueue(BigOven.recipe(), chanCurr);
                break;

            case "RECALL":
                if (getPluginEnabled("recall"))
                    messageQueue(botArgs.length == 0 ? Utility.getLastMessage(chanUser) == null
                            ? "[RECALL] You haven't said anything?"
                            : "[RECALL] " + Utility.getLastMessage(chanUser)
                            : Utility.getLastMessage(botArgs[0]) == null ? "[RECALL] They haven't said anything?"
                            : "[RECALL] " + Utility.getLastMessage(botArgs[0]), chanCurr);
                break;

            case "IGNORE":
                if (botArgs.length == 0) {
                    messageQueue("[IGNORE] Ignore whom?", chanCurr);
                } else if (ignoreUserList.contains(botArgs[0].toLowerCase())) {
                    ignoreUserList.remove(botArgs[0].toLowerCase());
                    messageQueue("[IGNORE] Oh, " + botArgs[0] + " wants to talk to me again?", chanCurr);
                } else {
                    ignoreUserList.add(botArgs[0].toLowerCase());
                    messageQueue("[IGNORE] Sure thing. I'll ignore " + botArgs[0] + " from now on!", chanCurr);
                }
                break;

            case "LOBOTOMY":
                if (getPluginEnabled("replies")) {
                    sendAction("drools", chanCurr);
                    botPlugins.put("replies", false);
                } else {
                    sendAction("blinks rapidly", chanCurr);
                    sendMessage("Wha? What happened?", chanCurr);
                    botPlugins.put("replies", true);
                }
                break;

            case "TELL":
                if (getPluginEnabled("tell")) {

                    sendRawMessage("NAMES " + chanCurr);
                    botArgs = Utility.stringJoin(botArgs, " ").split(" ", 2);

                    try {
                        switch (botArgs.length) {
                            case 1:
                                messageQueue("[TELL] Usage: <user> <message>", chanCurr);
                                break;

                            case 2:
                                if (chanUserList.get(chanCurr).contains(botArgs[0].toLowerCase()))
                                    messageQueue(botArgs[0].toLowerCase().equals(chanUser.toLowerCase())
                                            ? "[TELL] Talking to yourself again " + chanUser + "?"
                                            : botArgs[0].toLowerCase().equals(getNickname().toLowerCase())
                                            ? "[TELL] I'm right here! What's up " + chanUser + "?"
                                            : "[TELL] Really? They're right there, tell them yourself.", chanCurr);
                                else if (tellMessage.get(botArgs[0]) == null || tellMessage.get(botArgs[0]).size() < 5) {
                                    if (tellMessage.containsKey(botArgs[0]))
                                        tellMessage.get(botArgs[0]).add("<" + chanUser + "> " + botArgs[1]);
                                    else {
                                        final String[] finalBotArgs = botArgs;
                                        final String finalChanUser = chanUser;
                                        tellMessage.put(botArgs[0], new ArrayList<String>() {{
                                            add("<" + finalChanUser + "> " + finalBotArgs[1]);
                                        }});
                                    }
                                    tellUser.put(botArgs[0], tellMessage);
                                    messageQueue("[TELL] Okay, I'll try my best to let them know next time they're online.", chanCurr);
                                } else messageQueue("[TELL] I can only send 5 messages to a user.", chanCurr);
                                break;

                            default:
                                messageQueue("[TELL] Usage: <user> <message>", chanCurr);
                                break;
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        messageQueue("[TELL] Usage: <user> <message>", chanCurr);
                    }

                }
                break;

            case "HELP":
                sendMessage("Commands: T | G | UD | OSU | WA | YT | FM | HUNGRY | RECALL | IGNORE | LOBOTOMY | TELL", chanCurr);
                break;

            default:
                sendMessage("That's no command I've heard of " + chanUser + "?", chanCurr);
                break;
        }

    }

    public static void commandLimit(final String chanCurr) {

        Runnable resetCommandLimit = () -> {
            commandCount.remove(chanCurr);
            commandCountWarning.remove(chanCurr);
            if (getDebug()) showLevelMessage(INFO, "Command count reset for: " + chanCurr);
        };

        commandLimitExecutor = Executors.newScheduledThreadPool(1);
        commandLimitExecutor.scheduleAtFixedRate(resetCommandLimit, 0, 2, TimeUnit.MINUTES);

    }

}
