package com.ullarah.rotterbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static com.ullarah.rotterbot.Commands.*;
import static com.ullarah.rotterbot.Messages.*;
import static com.ullarah.rotterbot.Utility.levelType.INFO;
import static com.ullarah.rotterbot.Utility.showLevelMessage;

class Console implements Runnable {

    private static String currentChannel = "#> ";

    void start() {

        Thread consoleThread = new Thread(this);
        consoleThread.setName("IRC Console");
        consoleThread.start();

    }

    public void run() {

        boolean consoleActive = true;

        while (consoleActive) try {
            Scanner scanIn = new Scanner(System.in);
            String inputCommand = scanIn.nextLine();

            String[] input = inputCommand.replaceAll("(\\s){2,}", " ").split(" ");
            ArrayList<String> inputList = new ArrayList<>(input.length);
            Collections.addAll(inputList, input);

            command(inputList);

            System.out.print(currentChannel);
        } catch (NoSuchElementException | IOException | InterruptedException ex) {
            consoleActive = false;
            run();
        }

    }

    @SuppressWarnings("unchecked")
    private void command(ArrayList<String> args) throws IOException, InterruptedException {

        String cmd = args.get(0);
        args.remove(0);

        switch (cmd.toUpperCase()) {

            case "STOP":
                Client.disconnect();
                showLevelMessage(INFO, "Connection Closed.");
                Thread.sleep(2500);
                System.exit(0);
                break;

            case "RESTART":
                Client.disconnect();
                showLevelMessage(INFO, "Connection Restarting...");
                Thread.sleep(2500);
                Client.connect();
                break;

            case "CHANNELS":
                showLevelMessage(INFO, "Currently in: " + Client.getChannels().toString());
                break;

            case "ATTACH":
                if (args.isEmpty())
                    showLevelMessage(INFO, "Attach to what channel? Use CHANNELS to see active channels.");
                else if (Client.getChannels().contains(args.get(0)))
                    currentChannel = args.get(0).toLowerCase() + "> ";
                else showLevelMessage(INFO, "Not in channel " + args.get(0));
                break;

            case "DETACH":
                if (currentChannel.equals("#> ")) showLevelMessage(INFO, "Not attached to any channel.");
                else currentChannel = "#> ";
                break;

            case "SAY":
                if (currentChannel.equals("#> ")) showLevelMessage(INFO, "Attach to a channel: ATTACH <channel>");
                else {
                    String channel = currentChannel.substring(0, currentChannel.length() - 2);
                    if (args.isEmpty()) showLevelMessage(INFO, "Cannot send empty message.");
                    else messageQueue(Utility.stringJoin(args.toArray(new String[args.size()]), " "), channel);
                }
                break;

            case "ACTION":
                if (currentChannel.equals("#> ")) showLevelMessage(INFO, "Attach to a channel: ATTACH <channel>");
                else if (args.isEmpty()) showLevelMessage(INFO, "Cannot send empty action!");
                else sendAction(Utility.stringJoin(args.toArray(new String[args.size()]), " "),
                            currentChannel.replace(">", ""));
                break;

            case "PM":
                switch (args.size()) {
                    case 0:
                        showLevelMessage(INFO, "Cannot send to nobody: PM <user> <message>");
                        break;
                    case 1:
                        showLevelMessage(INFO, "Cannot send empty message: PM <user> <message>");
                        break;
                    default:
                        String user = args.get(0);
                        args.remove(0);
                        messageQueue(Utility.stringJoin(args.toArray(new String[args.size()]), " "), user);
                        break;
                }
                break;

            case "JOIN":
                String getJoinChannel;
                String getJoinReplies;
                switch (args.size()) {
                    case 0:
                        showLevelMessage(INFO, "Usage: JOIN <channel> [naughty]");
                        break;

                    case 1:
                        getJoinChannel = args.get(0).toLowerCase();
                        if (Client.getChannels().contains(getJoinChannel))
                            showLevelMessage(INFO, "Already in channel " + getJoinChannel);
                        else {
                            Client.botAttitude.put(getJoinChannel, "nice");
                            Client.getChannels().add(getJoinChannel);
                            showLevelMessage(INFO, "JOIN " + getJoinChannel);
                            sendRawMessage("JOIN " + getJoinChannel);
                            commandLimit(getJoinChannel);
                        }
                        break;

                    case 2:
                        getJoinChannel = args.get(0).toLowerCase();
                        getJoinReplies = args.get(1).toLowerCase();
                        if (Client.getChannels().contains(getJoinChannel))
                            showLevelMessage(INFO, "Already in channel " + getJoinChannel);
                        else {
                            Client.botAttitude.put(getJoinChannel, getJoinReplies);
                            Client.getChannels().add(getJoinChannel);
                            showLevelMessage(INFO, "JOIN " + getJoinChannel);
                            sendRawMessage("JOIN " + getJoinChannel);
                            commandLimit(getJoinChannel);
                        }
                        break;

                    default:
                        showLevelMessage(INFO, "Usage: JOIN <channel> [naughty]");
                }
                break;

            case "PART":
                String getPartChannel = args.get(0).toLowerCase();
                if (Client.getChannels().contains(getPartChannel)) {
                    Client.getChannels().remove(getPartChannel);
                    showLevelMessage(INFO, "PART " + getPartChannel);
                    sendRawMessage("PART " + getPartChannel);
                    commandCount.remove(getPartChannel);
                    commandCountWarning.remove(getPartChannel);
                } else showLevelMessage(INFO, "Not in channel " + getPartChannel);
                break;

            case "NICK":
                if (args.isEmpty()) {
                    sendRawMessage("NICK " + Client.getNickname());
                    showLevelMessage(INFO, "NICK " + Client.getNickname());
                } else {
                    sendRawMessage("NICK " + args.get(0));
                    showLevelMessage(INFO, "NICK " + args.get(0));
                }
                break;

            case "HELP":
                showLevelMessage(INFO,
                        "Commands: CHANNELS | ATTACH | DETACH | JOIN | PART | NICK | SAY | PM | RESTART | STOP");
                break;

        }

    }

}
