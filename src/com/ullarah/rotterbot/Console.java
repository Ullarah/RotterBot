package com.ullarah.rotterbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static com.ullarah.rotterbot.Log.info;
import static com.ullarah.rotterbot.Messages.botReply;
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
                    else botReply(Utility.stringJoin(args.toArray(new String[args.size()]), " "), channel);
                }
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
                        botReply(Utility.stringJoin(args.toArray(new String[args.size()]), " "), user);
                        break;
                }

            case "JOIN":
                if (Client.getChannels().contains(args.get(0).toLowerCase()))
                    info("Already in channel " + args.get(0).toLowerCase());
                else {
                    Client.getChannels().add(args.get(0).toLowerCase());
                    info("JOIN " + args.get(0).toLowerCase());
                    sendRaw("JOIN " + args.get(0).toLowerCase());
                }
                break;

            case "PART":
                if (Client.getChannels().contains(args.get(0).toLowerCase())) {
                    Client.getChannels().remove(args.get(0).toLowerCase());
                    info("PART " + args.get(0).toLowerCase());
                    sendRaw("PART " + args.get(0).toLowerCase());
                } else info("Not in channel " + args.get(0).toLowerCase());
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
