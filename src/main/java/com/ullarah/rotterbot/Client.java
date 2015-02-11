package com.ullarah.rotterbot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ullarah.rotterbot.Commands.commandLimit;
import static com.ullarah.rotterbot.Messages.*;
import static com.ullarah.rotterbot.Utility.levelType.*;
import static com.ullarah.rotterbot.Utility.showLevelMessage;

public class Client implements Runnable {

    private final Thread clientThread = new Thread(this);
    public static final HashMap<String, ArrayList<String>> chanUserList = new HashMap<>();
    public static final HashMap<String, Boolean> botPlugins = new HashMap<>();
    public static final HashMap<String, String> botAttitude = new HashMap<>();
    public static final HashMap<String, String> botMessages = new HashMap<>();
    public static final JSONParser jsonParser = new JSONParser();
    private static final HashMap<String, String> botAPIKeys = new HashMap<>();
    private static final String getLogRegex = "^(?:[:](\\S+) )?(\\S+)(?: (?!:)(.+?))?(?: [:](.+))?$";
    private static final Pattern logPattern = Pattern.compile(getLogRegex);
    private static final String config = "config.json";
    public static BufferedWriter writer;
    public static long pingtime = System.currentTimeMillis();
    public static ScheduledExecutorService queueDeliveryExecutor;
    public static ScheduledExecutorService commandLimitExecutor;
    private static BufferedReader reader;
    private static Boolean online;
    private static Boolean debug;
    private static String server;
    private static Long port;
    private static String nickname;
    private static String realname;
    private static String login;
    private static String password;
    private static JSONArray channels;
    private static Socket socket;
    private static ScheduledExecutorService pingTimeoutExecutor;

    private static Boolean getOnline() {
        return online;
    }

    private static void setOnline(Boolean online) {
        Client.online = online;
    }

    private static String getServer() {
        return server;
    }

    private static void setServer(String server) {
        Client.server = server;
    }

    private static Long getPort() {
        return port;
    }

    private static void setPort(Long port) {
        Client.port = port;
    }

    public static String getNickname() {
        return nickname;
    }

    private static void setNickname(String nickname) {
        Client.nickname = nickname;
    }

    private static String getRealname() {
        return realname;
    }

    private static void setRealname(String realname) {
        Client.realname = realname;
    }

    static String getLogin() {
        return login;
    }

    private static void setLogin(String login) {
        Client.login = login;
    }

    static String getPassword() {
        return password;
    }

    private static void setPassword(String password) {
        Client.password = password;
    }

    public static JSONArray getChannels() {
        return channels;
    }

    private static void setChannels(JSONArray channels) {
        Client.channels = channels;
    }

    private static Socket getSocket() {
        return socket;
    }

    private static void setSocket(Socket socket) {
        Client.socket = socket;
    }

    public static void main(String args[]) throws IOException {
        new Client().start();
        new Console().start();
    }

    public static Boolean getDebug() {
        return debug;
    }

    private static void setDebug(Boolean debug) {
        Client.debug = debug;
    }

    public static void connect() throws IOException {

        try {
            loadConfig(new FileReader(config));
        } catch (NullPointerException | ParseException | IOException e) {
            showLevelMessage(ERROR, "Configuration file not found or corrupted.");
            System.exit(0);
        }

        setSocket(new Socket(getServer(), getPort().intValue()));
        writer = new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        showLevelMessage(INFO, "CONN " + getServer());

        if (getSocket().isConnected()) {
            sendRawMessage("NICK " + getNickname());
            showLevelMessage(INFO, "NICK " + getNickname());
            sendRawMessage("USER " + getLogin() + " \"\" \"\" :" + getRealname());
            showLevelMessage(INFO, "USER " + getLogin());
            writer.flush();
            runPingTimeout();
            setOnline(true);
        }

    }

    public static void disconnect() throws InterruptedException, IOException {

        pingTimeoutExecutor.shutdown();
        queueDeliveryExecutor.shutdown();
        commandLimitExecutor.shutdown();

        showLevelMessage(INFO, "Disconnecting...");
        Thread.sleep(2500);
        for (Object channel : Client.getChannels()) {
            sendRawMessage("PART " + channel);
            showLevelMessage(INFO, "PART " + channel);
            Thread.sleep(2500);
        }
        sendRawMessage("QUIT");
        Thread.sleep(5000);
        Client.getSocket().close();
        Thread.sleep(2500);

    }

    private static void reconnect() throws InterruptedException, IOException {

        pingTimeoutExecutor.shutdown();
        queueDeliveryExecutor.shutdown();
        commandLimitExecutor.shutdown();

        showLevelMessage(INFO, "Reconnecting...");
        Thread.sleep(1000);
        for (Object channel : Client.getChannels()) {
            sendRawMessage("PART " + channel);
            showLevelMessage(INFO, "PART " + channel);
            Thread.sleep(1000);
        }
        sendRawMessage("QUIT");
        Thread.sleep(2500);
        Client.getSocket().close();
        Thread.sleep(2500);
        connect();

    }

    @SuppressWarnings("unchecked")
    private static void loadConfig(FileReader reader) throws IOException, ParseException {

        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
        reader.close();

        setServer((String) jsonObject.get("server"));
        setPort((Long) jsonObject.get("port"));

        setNickname((String) jsonObject.get("nickname"));
        setRealname((String) jsonObject.get("realname"));

        setLogin((String) jsonObject.get("login"));
        setPassword((String) jsonObject.get("password"));

        JSONArray channelList = new JSONArray();

        JSONObject getChannels = (JSONObject) jsonObject.get("channels");
        for (Object channel : getChannels.keySet()) {
            channelList.add(channel);
            botAttitude.put((String) channel, (String) getChannels.get(channel));
            System.out.println(botAttitude.toString());
        }

        setChannels(channelList);

        setDebug((boolean) jsonObject.get("debug"));

        JSONObject getPlugins = (JSONObject) jsonObject.get("plugins");
        for (Object plugin : getPlugins.keySet()) botPlugins.put((String) plugin, (Boolean) getPlugins.get(plugin));

        JSONObject getAPIKeys = (JSONObject) jsonObject.get("keys");
        for (Object keys : getAPIKeys.keySet()) botAPIKeys.put((String) keys, (String) getAPIKeys.get(keys));

    }

    public static boolean getPluginEnabled(String plugin) {

        return botPlugins.get(plugin);

    }

    public static String pluginKey(String plugin) {

        return botAPIKeys.get(plugin);

    }

    private static void runPingTimeout() {

        Runnable getCurrentPing = () -> {
            long diffPing = System.currentTimeMillis() - pingtime;
            if (diffPing >= 600000) try {
                if (getDebug()) showLevelMessage(DEBUG, "Ping Timeout. Reconnecting.");
                pingtime = System.currentTimeMillis();
                reconnect();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            else if (getDebug()) showLevelMessage(DEBUG, "Ping Valid.");
        };

        pingTimeoutExecutor = Executors.newScheduledThreadPool(1);
        pingTimeoutExecutor.scheduleAtFixedRate(getCurrentPing, 0, 5, TimeUnit.MINUTES);

    }

    void start() {

        clientThread.setName("IRC Connection");
        clientThread.start();

    }

    public void run() {
        try {
            connect();
        } catch (IOException e) {
            try {
                reconnect();
            } catch (InterruptedException | IOException e1) {
                showLevelMessage(ERROR, "Cannot connect to: " + getServer());
            }
        }
        while (getOnline()) try {

            String logCurrentLine;
            while ((logCurrentLine = reader.readLine()) != null) {

                Matcher logLine = logPattern.matcher(logCurrentLine);

                if (getDebug()) showLevelMessage(DEBUG, logCurrentLine);

                if(logLine.find())
                    servMessage(
                            logLine.group(0), //raw
                            logLine.group(1), //prefix
                            logLine.group(2), //type
                            logLine.group(3), //destination
                            logLine.group(4)  //message
                    );

                writer.flush();

            }
        } catch (IOException | InterruptedException | ParseException ignored) {
        }
    }

    static void joinPresetChannels() throws InterruptedException, IOException {

        for (Object channel : getChannels()) {
            showLevelMessage(INFO, "JOIN " + channel.toString().toLowerCase());
            sendRawMessage("JOIN " + channel.toString().toLowerCase());
            commandLimit(channel.toString().toLowerCase());
            Thread.sleep(2500);
        }

        deliverQueue();

    }

    static void refreshUserList(String channel, String[] users) {

        ArrayList<String> chanUsers = new ArrayList<>();

        for (String user : users) {
            String[] userNameValid = user.split("[a-zA-Z0-9]+");
            chanUsers.add(userNameValid.length == 0 ? user.toLowerCase() : user.substring(1).toLowerCase());
        }

        chanUserList.put(channel, chanUsers);

    }

}
