package com.ullarah.rotterbot.modules;

import com.ullarah.rotterbot.Utility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static com.ullarah.rotterbot.Client.pluginKey;

public class LastFM {

    private static JSONObject getAPI(String type, String args) {

        try {
            args = args.replaceAll(" ", "+");

            URL url = new URL("http://ws.audioscrobbler.com/2.0/?method=" + type + "&api_key=" + pluginKey("lastfm") + "&format=json" + args);
            URLConnection conn = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JSONParser jsonParser = new JSONParser();

            return (JSONObject) jsonParser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static String getUserTopTrack(String user) {

        JSONObject lastFMObject = getAPI("user.gettoptracks", "&limit=1&user=" + user);

        JSONObject topTrackObject = (JSONObject) lastFMObject.get("toptracks");
        JSONObject trackObject = (JSONObject) topTrackObject.get("track");

        JSONObject trackArtistObject = (JSONObject) trackObject.get("artist");

        String trackName = (String) trackObject.get("name");
        String trackArtist = (String) trackArtistObject.get("name");
        String trackPlays = (String) trackObject.get("playcount");

        return "[LASTFM] Top Track: " +
                Colour.BOLD + trackName + Colour.RESET +
                " by " + Colour.BOLD + trackArtist + Colour.RESET +
                " | Plays: " + Colour.BOLD + trackPlays;

    }

    public static String getUserRecentTrack(String user) {

        JSONObject lastFMObject = getAPI("user.getrecenttracks", "&limit=1&user=" + user);

        JSONObject topRecentTrackObject = (JSONObject) lastFMObject.get("recenttracks");

        Object trackObject = topRecentTrackObject.get("track");

        if (trackObject instanceof JSONObject) {

            JSONObject trackJSONObject = (JSONObject) trackObject;

            JSONObject trackArtistObject = (JSONObject) trackJSONObject.get("artist");

            String trackName = (String) trackJSONObject.get("name");
            String trackArtist = (String) trackArtistObject.get("#text");

            return "[LASTFM] Recent Track: " +
                    Colour.BOLD + trackName + Colour.RESET +
                    " by " + Colour.BOLD + trackArtist;

        } else if (trackObject instanceof JSONArray) {

            JSONArray trackJSONArray = (JSONArray) trackObject;

            JSONObject trackPlayObject = (JSONObject) trackJSONArray.get(0);

            JSONObject trackArtistObject = (JSONObject) trackPlayObject.get("artist");

            String trackName = (String) trackPlayObject.get("name");
            String trackArtist = (String) trackArtistObject.get("#text");

            return "[LASTFM] Playing Now: " +
                    Colour.BOLD + trackName + Colour.RESET +
                    " by " + Colour.BOLD + trackArtist;

        } else {

            return "[LASTFM] Cannot find recent plays.";

        }

    }

    public static String getArtistSimilar(String artist) {

        artist = artist.trim();

        JSONObject lastFMObject = getAPI("artist.getSimilar", "&artist=" + artist);

        if (lastFMObject.containsKey("similarartists")) {

            JSONObject similarArtistObject = (JSONObject) lastFMObject.get("similarartists");

            if (!similarArtistObject.containsKey("#text")) {
                JSONArray similarArtists = (JSONArray) similarArtistObject.get("artist");
                int randomSimilarArtist = Utility.randInt(0, similarArtists.size());

                JSONObject similarArtist = (JSONObject) similarArtists.get(randomSimilarArtist);

                String artistName = (String) similarArtist.get("name");

                return "[LASTFM] Similar Artist to " + artist + ": " + Colour.BOLD + artistName;
            } else return "[LASTFM] No similar artists to: " + Colour.BOLD + artist;

        } else return "[LASTFM] No similar artists to: " + Colour.BOLD + artist;

    }

    public static String getTrackSimilar(String artist, String track) {

        artist = artist.trim();
        track = track.trim();

        JSONObject lastFMObject = getAPI("track.getSimilar", "&artist=" + artist + "&track=" + track);

        if (lastFMObject.containsKey("similartracks")) {

            JSONObject similarTrackObject = (JSONObject) lastFMObject.get("similartracks");
            JSONArray similarTracks = (JSONArray) similarTrackObject.get("track");

            int randomSimilarTrack = Utility.randInt(0, similarTracks.size());

            JSONObject similarTrack = (JSONObject) similarTracks.get(randomSimilarTrack);

            JSONObject trackArtistObject = (JSONObject) similarTrack.get("artist");

            String trackName = (String) similarTrack.get("name");
            String trackArtist = (String) trackArtistObject.get("name");

            return "[LASTFM] Similar Track to " + artist + " by " + track + ": " +
                    Colour.BOLD + trackName + Colour.RESET +
                    " by " + Colour.BOLD + trackArtist;

        } else return "[LASTFM] No similar tracks to:" +
                Colour.BOLD + artist + Colour.RESET +
                " by " + Colour.BOLD + track;

    }

    public static String getHypedTrack() {

        JSONObject lastFMObject = getAPI("chart.gethypedtracks", "&limit=1");

        JSONObject topTrackObject = (JSONObject) lastFMObject.get("tracks");
        JSONObject trackObject = (JSONObject) topTrackObject.get("track");

        JSONObject trackArtistObject = (JSONObject) trackObject.get("artist");

        String trackName = (String) trackObject.get("name");
        String trackArtist = (String) trackArtistObject.get("name");

        return "[LASTFM] Hyped Chart Track: " +
                Colour.BOLD + trackName + Colour.RESET +
                " by " + Colour.BOLD + trackArtist;

    }

    public static String getTopTrack() {

        JSONObject lastFMObject = getAPI("chart.gettoptracks", "&limit=1");

        JSONObject topTrackObject = (JSONObject) lastFMObject.get("tracks");
        JSONObject trackObject = (JSONObject) topTrackObject.get("track");

        JSONObject trackArtistObject = (JSONObject) trackObject.get("artist");

        String trackName = (String) trackObject.get("name");
        String trackArtist = (String) trackArtistObject.get("name");

        return "[LASTFM] Top Chart Track: " +
                Colour.BOLD + trackName + Colour.RESET +
                " by " + Colour.BOLD + trackArtist;

    }

}
