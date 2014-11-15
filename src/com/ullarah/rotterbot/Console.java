package com.ullarah.rotterbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static com.ullarah.rotterbot.Commands.*;
import static com.ullarah.rotterbot.Commands.commandCount;
import static com.ullarah.rotterbot.Commands.commandLimit;
import static com.ullarah.rotterbot.Log.info;
import static com.ullarah.rotterbot.Messages.*;
import static com.ullarah.rotterbot.Messages.botMessage;
import static com.ullarah.rotterbot.Messages.sendRaw;

class Console implements Runnable {

    private String currentChannel = "#> ";

    void start() {

        Thread consoleThread = new Thread(this);
        consoleThread.setName("IRC Console");
        consoleThread.start();

    }

    public void run() {

        boolean consoleActive = true;

        while (consoleActive) try {
            String inputCommand;

            Scanner scanIn = new Scanner(System.in);
            inputCommand = scanIn.nextLine();

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
                info("Connection Closed.");
                Thread.sleep(2500);
                System.exit(0);
                break;

            case "RESTART":
                Client.disconnect();
                info("Connection Restarting...");
                Thread.sleep(2500);
                Client.connect();
                break;

            case "CHANNELS":
                info("Currently in: " + Client.getChannels().toString());
                break;

            case "ATTACH":
                if (args.isEmpty()) info("Attach to what channel? Use CHANNELS to see active channels.");
                else if (Client.getChannels().contains(args.get(0)))
                    currentChannel = args.get(0).toLowerCase() + "> ";
                else info("Not in channel " + args.get(0));
                break;

            case "DETACH":
                if (currentChannel.equals("#> ")) info("Not attached to any channel.");
                else currentChannel = "#> ";
                break;

            case "SAY":
                if (currentChannel.equals("#> ")) info("Attach to a channel: ATTACH <channel>");
                else {
                    String channel = currentChannel.substring(0, currentChannel.length() - 2);
                    if (args.isEmpty()) info("Cannot send empty message.");
                    else botMessage(Utility.stringJoin(args.toArray(new String[args.size()]), " "), channel);
                }
                break;

            case "ACTION":
                if (currentChannel.equals("#> ")) info("Attach to a channel: ATTACH <channel>");
                else if (args.isEmpty()) info("Cannot send empty action!");
                else botAction(Utility.stringJoin(args.toArray(new String[args.size()]), " "),
                            currentChannel.replace(">",""));
                break;

            case "PM":
                switch (args.size()) {
                    case 0:
                        info("Cannot send to nobody: PM <user> <message>");
                        break;
                    case 1:
                        info("Cannot send empty message: PM <user> <message>");
                        break;
                    default:
                        String user = args.get(0);
                        args.remove(0);
                        botMessage(Utility.stringJoin(args.toArray(new String[args.size()]), " "), user);
                        break;
                }

            case "JOIN":
                String getJoinChannel;
                String getJoinReplies;
                switch(args.size()){
                    case 0:
                        info("Usage: JOIN <channel> [naughty]");
                        break;

                    case 1:
                        getJoinChannel = args.get(0).toLowerCase();
                        if (Client.getChannels().contains(getJoinChannel))
                            info("Already in channel " + getJoinChannel);
                        else {
                            Client.botNice.put(getJoinChannel,"nice");
                            Client.getChannels().add(getJoinChannel);
                            info("JOIN " + getJoinChannel);
                            sendRaw("JOIN " + getJoinChannel);
                            commandLimit(getJoinChannel);
                        }
                        break;

                    case 2:
                        getJoinChannel = args.get(0).toLowerCase();
                        getJoinReplies = args.get(1).toLowerCase();
                        if (Client.getChannels().contains(getJoinChannel))
                            info("Already in channel " + getJoinChannel);
                        else {
                            Client.botNice.put(getJoinChannel,getJoinReplies);
                            Client.getChannels().add(getJoinChannel);
                            info("JOIN " + getJoinChannel);
                            sendRaw("JOIN " + getJoinChannel);
                            commandLimit(getJoinChannel);
                        }
                        break;

                    default:
                        info("Usage: JOIN <channel> [naughty]");
                }
                break;

            case "PART":
                String getPartChannel = args.get(0).toLowerCase();
                if (Client.getChannels().contains(getPartChannel)) {
                    Client.getChannels().remove(getPartChannel);
                    info("PART " + getPartChannel);
                    sendRaw("PART " + getPartChannel);
                    commandCount.remove(getPartChannel);
                    commandCountWarning.remove(getPartChannel);
                } else info("Not in channel " + getPartChannel);
                break;

            case "NICK":
                if (args.isEmpty()) {
                    sendRaw("NICK " + Client.getNickname());
                    info("NICK " + Client.getNickname());
                } else {
                    sendRaw("NICK " + args.get(0));
                    info("NICK " + args.get(0));
                }

            case "HELP":
                info("Commands: CHANNELS | ATTACH | DETACH | JOIN | PART | NICK | SAY | PM | RESTART | STOP");
                break;

        }

    }

}
