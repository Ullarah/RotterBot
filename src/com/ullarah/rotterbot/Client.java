package com.ullarah.rotterbot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ullarah.rotterbot.Commands.commandLimit;
import static com.ullarah.rotterbot.Log.error;
import static com.ullarah.rotterbot.Log.info;
import static com.ullarah.rotterbot.Messages.sendRaw;

public class Client implements Runnable {

    public static final HashMap<String, ArrayList<String>> chanUserList = new HashMap<>();
    public static final HashMap<String, Boolean> botPlugins = new HashMap<>();
    public static final HashMap<String, String> botAPIKeys = new HashMap<>();
    public static final HashMap<String, String> botNice = new HashMap<>();

    private static final String config = "config.json";
    public static final JSONParser jsonParser = new JSONParser();
    public static BufferedWriter writer;
    public static BufferedReader reader;
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
    public static long pingtime = System.currentTimeMillis();

    private static ScheduledExecutorService pingTimeoutExecutor;
    public static ScheduledExecutorService commandCountExecutor;

    public static String line;

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

    private static String getLogin() {
        return login;
    }

    private static void setLogin(String login) {
        Client.login = login;
    }

    private static String getPassword() {
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
            error("Configuration file not found or corrupted.");
            System.exit(0);
        }

        setSocket(new Socket(getServer(), getPort().intValue()));
        writer = new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        info("CONN " + getServer());

        if (getSocket().isConnected()) {
            sendRaw("NICK " + getNickname());
            info("NICK " + getNickname());
            sendRaw("USER " + getLogin() + " \"\" \"\" :" + getRealname());
            info("USER " + getLogin());
            writer.flush();
            runPingTimeout();
            setOnline(true);
        }

    }

    public static void disconnect() throws InterruptedException, IOException {

        pingTimeoutExecutor.shutdown();
        commandCountExecutor.shutdown();

        info("Disconnecting...");
        Thread.sleep(2500);
        for (Object channel : Client.getChannels()) {
            sendRaw("PART " + channel);
            info("PART " + channel);
            Thread.sleep(2500);
        }
        sendRaw("QUIT");
        Thread.sleep(5000);
        Client.getSocket().close();
        Thread.sleep(2500);

    }

    public static void reconnect() throws InterruptedException, IOException {

        pingTimeoutExecutor.shutdown();
        commandCountExecutor.shutdown();

        info("Reconnecting...");
        Thread.sleep(1000);
        for (Object channel : Client.getChannels()) {
            sendRaw("PART " + channel);
            info("PART " + channel);
            Thread.sleep(1000);
        }
        sendRaw("QUIT");
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

        JSONObject getChannels = (JSONObject) jsonObject.get("channels");
        for( Object channel : getChannels.keySet() ) {
            getChannels().add(channel);
            botNice.put((String) channel,(String)getChannels.get(channel));
        }

        setDebug((boolean) jsonObject.get("debug"));

        JSONObject getPlugins = (JSONObject) jsonObject.get("plugins");
        for( Object plugin : getPlugins.keySet() ) botPlugins.put((String) plugin, (Boolean) getPlugins.get(plugin));

        JSONObject getAPIKeys = (JSONObject) jsonObject.get("keys");
        for( Object keys : getAPIKeys.keySet() ) botAPIKeys.put((String) keys, (String) getAPIKeys.get(keys));

    }

    public static boolean getPluginEnabled(String plugin) throws IOException, ParseException {

        return botPlugins.get(plugin);

    }

    public static String pluginKey(String plugin) throws IOException, ParseException {

        return botAPIKeys.get(plugin);

    }

    void start() {

        Thread clientThread = new Thread(this);
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
                error("Cannot connect to: " + getServer());
            }
        }
        while (getOnline()) try {

            while ((line = reader.readLine()) != null) {

                if (getDebug()) info(line);

                if (line.contains("PING")) {
                    sendRaw("PONG " + line.substring(5));
                    if (getDebug()) info("PONG " + line.substring(5));
                    pingtime = System.currentTimeMillis();
                }

                if (line.contains("376") && !line.contains("PRIVMSG")) {
                    sendRaw("PRIVMSG NickServ :IDENTIFY " + getPassword());
                    info("IDEN " + getLogin());
                }

                if (line.contains("353") && !line.contains("PRIVMSG"))
                    refreshUserList(line.split(" ", 6)[4], line.split(" ", 6)[5].split(" "));

                if (line.contains("NOTICE") && line.contains("identified") && !line.contains("PRIVMSG"))
                    for (Object channel : getChannels()) {
                        info("JOIN " + channel.toString().toLowerCase());
                        sendRaw("JOIN " + channel.toString().toLowerCase());
                        commandLimit(channel.toString().toLowerCase());
                        Thread.sleep(2500);
                    }
                else Messages.servMessage(line);

                writer.flush();
            }
        } catch (IOException | InterruptedException | ParseException ignored) {
        }
    }

    public void refreshUserList(String channel, String[] users){

        ArrayList<String> chanUsers = new ArrayList<>();

        for( String user : users ) {
            String[] userNameValid = user.split("[a-zA-Z0-9]+");
            chanUsers.add(userNameValid.length == 0 ? user.toLowerCase() : user.substring(1).toLowerCase());
        }

        chanUserList.put(channel,chanUsers);

    }

    public static void runPingTimeout() {

        Runnable getCurrentPing = new Runnable() {
            public void run() {
                long diffPing = System.currentTimeMillis() - pingtime;
                if(diffPing >= 600000) try {
                    if (getDebug()) info("Ping Timeout. Reconnecting.");
                    pingtime = System.currentTimeMillis();
                    reconnect();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
                else if (getDebug()) info("Ping Valid.");
            }
        };

        pingTimeoutExecutor = Executors.newScheduledThreadPool(1);
        pingTimeoutExecutor.scheduleAtFixedRate(getCurrentPing, 0, 5, TimeUnit.MINUTES);

    }

}
