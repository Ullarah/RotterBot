package com.ullarah.rotterbot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

import static com.ullarah.rotterbot.Log.error;
import static com.ullarah.rotterbot.Log.info;
import static com.ullarah.rotterbot.Messages.sendRaw;

public class Client implements Runnable {

    public static final HashMap<String, String> recallMessages = new HashMap<>();
    private static final String config = "config.json";
    private static final JSONParser jsonParser = new JSONParser();
    public static BufferedWriter writer;
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

    private static Boolean getDebug() {
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
            setOnline(true);
        }

    }

    public static void disconnect() throws InterruptedException, IOException {

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

    private static void loadConfig(FileReader reader) throws IOException, ParseException {

        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

        setServer((String) jsonObject.get("server"));
        setPort((Long) jsonObject.get("port"));

        setNickname((String) jsonObject.get("nickname"));
        setRealname((String) jsonObject.get("realname"));

        setLogin((String) jsonObject.get("login"));
        setPassword((String) jsonObject.get("password"));

        setChannels((JSONArray) jsonObject.get("channels"));

        setDebug((boolean) jsonObject.get("debug"));

    }

    public static boolean pluginEnabled(String plugin) throws IOException, ParseException {

        JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(config));

        JSONObject getPlugins = ((JSONObject) jsonObject.get("plugins"));

        return (boolean) getPlugins.get(plugin);

    }

    public static String pluginKey(String plugin) throws IOException, ParseException {

        JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(config));

        JSONObject getKey = ((JSONObject) jsonObject.get("keys"));

        return (String) getKey.get(plugin);

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
            e.printStackTrace();
        }
        while (getOnline()) try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (getDebug()) info(line);
                if (line.contains("PING")) {
                    sendRaw("PONG " + line.substring(5));
                    if (getDebug()) info("PONG " + line.substring(5));
                }
                if (line.contains("376") && !line.contains("PRIVMSG")) {
                    sendRaw("PRIVMSG NickServ :IDENTIFY " + getPassword());
                    info("IDEN " + getLogin());
                }
                if (line.contains("NOTICE") && line.contains("identified") && !line.contains("PRIVMSG"))
                    for (Object channel : getChannels()) {
                        info("JOIN " + channel.toString().toLowerCase());
                        sendRaw("JOIN " + channel.toString().toLowerCase());
                        Thread.sleep(2500);
                    }
                else Messages.servMessage(line);
                writer.flush();
            }
        } catch (IOException | InterruptedException | ParseException ignored) {
        }
    }

}