package com.ullarah.rotterbot;

import com.ullarah.rotterbot.modules.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;

import static com.ullarah.rotterbot.Client.pluginEnabled;
import static com.ullarah.rotterbot.Messages.botReply;

class Commands {

    public static void getCommand(String chanCurr, String chanUser, String chanSaid)
            throws IOException, ParseException {

        String botCommand = null;
        String[] botArgs = new String[0];

        Matcher m = Messages.commandPattern.matcher(chanSaid);

        if (m.find()) {
            botCommand = m.group(1);
            botArgs = m.group(2).replaceAll("(\\s){2,}", " ").substring(0).split(" ");
            if (botArgs[0].equals("")) botArgs = new String[0]; // Dirty hack imo. Redo regex?
        }

        assert botCommand != null;

        switch (botCommand.toUpperCase()) {
            case "ARGS":
                botReply(botArgs.length == 0 ? "You used " + botCommand + " with no arguments"
                        : "You used " + botCommand + " with: " + Arrays.toString(botArgs).toUpperCase(), chanCurr);
                break;

            case "T":
                if (pluginEnabled("showerthought"))
                    botReply(ShowerThought.randomThought(), chanCurr);
                break;

            case "G":
                Boolean isSafe = true;
                if (pluginEnabled("googlesearch")) if (botArgs.length == 0)
                    botReply("[GOOGLE] Yes, let me search for nothing... nothing found!", chanCurr);
                else {
                    if (botArgs[0].toUpperCase().equals("!SAFE")) isSafe = false;
                    botReply(Google.feelingLucky(Utility.stringJoin(botArgs, " ")
                            .toUpperCase().replaceFirst("!SAFE ", ""), isSafe), chanCurr);
                }
                break;

            case "UD":
                if (pluginEnabled("urbandictionary"))
                    botReply(botArgs.length == 0 ? "[UD] Yes, let me search for nothing... nothing found!"
                            : UrbanDictionary.getWord(Utility.stringJoin(botArgs, " ")), chanCurr);
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
                if (pluginEnabled("wolframalpha"))
                    botReply(botArgs.length == 0 ? "[WOLFRAM] Yes, let me search for nothing... nothing found!"
                            : "[WOLFRAM] " + WolframAlpha.calculate(Utility.stringJoin(botArgs, " ")), chanCurr);
                break;

            case "YT":
                if (pluginEnabled("youtube"))
                    botReply(botArgs.length == 0 ? "[YOUTUBE] Yes, let me search for nothing... nothing found!"
                            : "[YOUTUBE] " + Youtube.getVideoResult(Utility.stringJoin(botArgs, " ")), chanCurr);
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
                if (pluginEnabled("recall"))
                    botReply(botArgs.length == 0 ? Utility.getLastMessage(chanUser) == null
                            ? "[RECALL] You haven't said anything?"
                            : "[RECALL] " + Utility.getLastMessage(chanUser)
                            : Utility.getLastMessage(botArgs[0]) == null ? "[RECALL] They haven't said anything?"
                            : "[RECALL] " + Utility.getLastMessage(botArgs[0]), chanCurr);
                break;

            default:
                botReply("That's no command I've heard of " + chanUser + "?", chanCurr);
                break;
        }

    }

}
