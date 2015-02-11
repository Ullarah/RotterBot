package com.ullarah.rotterbot.modules;

import com.ullarah.rotterbot.Utility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import static com.ullarah.rotterbot.Client.pluginKey;
import static com.ullarah.rotterbot.Utility.urlDecode;
import static com.ullarah.rotterbot.Utility.urlEncode;

public class LastFM {

    private static JSONObject getAPI(String type, String args) {

        try {
            URL url = new URL("http://ws.audioscrobbler.com/2.0/?method=" + type + "&api_key=" + pluginKey("lastfm") + "&format=json" + args);
            URLConnection conn = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JSONParser jsonParser = new JSONParser();

            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            reader.close();

            return jsonObject;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static String getUserTopTrack(String user) throws UnsupportedEncodingException {

        JSONObject lastFMObject = getAPI("user.gettoptracks", "&limit=1&user=" + urlEncode(user));

        JSONObject topTrackObject = (JSONObject) lastFMObject.get("toptracks");
        JSONObject trackObject = (JSONObject) topTrackObject.get("track");

        JSONObject trackArtistObject = (JSONObject) trackObject.get("artist");

        String trackName = urlDecode((String) trackObject.get("name"));
        String trackArtist = urlDecode((String) trackArtistObject.get("name"));
        String trackPlays = urlDecode((String) trackObject.get("playcount"));

        return "[LASTFM] Top Track: " +
                Colour.BOLD + trackName + Colour.RESET +
                " by " + Colour.BOLD + trackArtist + Colour.RESET +
                " | Plays: " + Colour.BOLD + trackPlays;

    }

    public static String getUserRecentTrack(String user) throws UnsupportedEncodingException {

        JSONObject lastFMObject = getAPI("user.getrecenttracks", "&limit=1&user=" + urlEncode(user));

        JSONObject topRecentTrackObject = (JSONObject) lastFMObject.get("recenttracks");

        Object trackObject = topRecentTrackObject.get("track");

        if (trackObject instanceof JSONObject) {

            JSONObject trackJSONObject = (JSONObject) trackObject;

            JSONObject trackArtistObject = (JSONObject) trackJSONObject.get("artist");

            String trackName = urlDecode((String) trackJSONObject.get("name"));
            String trackArtist = urlDecode((String) trackArtistObject.get("#text"));

            return "[LASTFM] Recent Track: " +
                    Colour.BOLD + trackName + Colour.RESET +
                    " by " + Colour.BOLD + trackArtist;

        } else if (trackObject instanceof JSONArray) {

            JSONArray trackJSONArray = (JSONArray) trackObject;

            JSONObject trackPlayObject = (JSONObject) trackJSONArray.get(0);

            JSONObject trackArtistObject = (JSONObject) trackPlayObject.get("artist");

            String trackName = urlDecode((String) trackPlayObject.get("name"));
            String trackArtist = urlDecode((String) trackArtistObject.get("#text"));

            return "[LASTFM] Playing Now: " +
                    Colour.BOLD + trackName + Colour.RESET +
                    " by " + Colour.BOLD + trackArtist;

        } else {

            return "[LASTFM] Cannot find recent plays.";

        }

    }

    public static String getArtistSimilar(String artist) throws UnsupportedEncodingException {

        artist = artist.trim();

        String nothingFound = "[LASTFM] No similar artists to: " + Colour.BOLD + urlDecode(artist);

        JSONObject lastFMObject = getAPI("artist.getSimilar", "&artist=" + urlEncode(artist));

        if (lastFMObject.containsKey("similarartists")) {

            JSONObject similarArtistObject = (JSONObject) lastFMObject.get("similarartists");

            if (!similarArtistObject.containsKey("#text")) {
                JSONArray similarArtists = (JSONArray) similarArtistObject.get("artist");
                int randomSimilarArtist = Utility.randInt(0, similarArtists.size());

                JSONObject similarArtist = (JSONObject) similarArtists.get(randomSimilarArtist);

                String artistName = urlDecode((String) similarArtist.get("name"));

                return "[LASTFM] Similar Artist to " + urlDecode(artist) + ": " + Colour.BOLD + urlDecode(artistName);
            } else return nothingFound;

        } else return "[LASTFM] No similar artists to: " + Colour.BOLD + urlDecode(artist);

    }

    public static String getTrackSimilar(String artist, String track) throws UnsupportedEncodingException {

        artist = artist.trim();
        track = track.trim();

        String nothingFound = "[LASTFM] No similar tracks to: " +
                Colour.BOLD + urlDecode(artist) + Colour.RESET +
                " by " + Colour.BOLD + urlDecode(track);

        JSONObject lastFMObject = getAPI("track.getSimilar",
                "&artist=" + urlEncode(artist) +
                        "&track=" + urlEncode(track));

        if (lastFMObject.containsKey("similartracks")) {

            JSONObject similarTrackObject = (JSONObject) lastFMObject.get("similartracks");

            if (!similarTrackObject.containsKey("#text")){
                JSONArray similarTracks = (JSONArray) similarTrackObject.get("track");

                int randomSimilarTrack = Utility.randInt(0, similarTracks.size());

                JSONObject similarTrack = (JSONObject) similarTracks.get(randomSimilarTrack);

                JSONObject trackArtistObject = (JSONObject) similarTrack.get("artist");

                String trackName = urlDecode((String) similarTrack.get("name"));
                String trackArtist = urlDecode((String) trackArtistObject.get("name"));

                return "[LASTFM] Similar Track to " + artist + " by " + track + ": " +
                        Colour.BOLD + trackName + Colour.RESET +
                        " by " + Colour.BOLD + trackArtist;
            } else return nothingFound;



        } else return nothingFound;

    }

    public static String getHypedTrack() throws UnsupportedEncodingException {

        JSONObject lastFMObject = getAPI("chart.gethypedtracks", "&limit=1");

        JSONObject topTrackObject = (JSONObject) lastFMObject.get("tracks");
        JSONObject trackObject = (JSONObject) topTrackObject.get("track");

        JSONObject trackArtistObject = (JSONObject) trackObject.get("artist");

        String trackName = urlDecode((String) trackObject.get("name"));
        String trackArtist = urlDecode((String) trackArtistObject.get("name"));

        return "[LASTFM] Hyped Chart Track: " +
                Colour.BOLD + trackName + Colour.RESET +
                " by " + Colour.BOLD + trackArtist;

    }

    public static String getTopTrack() throws UnsupportedEncodingException {

        JSONObject lastFMObject = getAPI("chart.gettoptracks", "&limit=1");

        JSONObject topTrackObject = (JSONObject) lastFMObject.get("tracks");
        JSONObject trackObject = (JSONObject) topTrackObject.get("track");

        JSONObject trackArtistObject = (JSONObject) trackObject.get("artist");

        String trackName = urlDecode((String) trackObject.get("name"));
        String trackArtist = urlDecode((String) trackArtistObject.get("name"));

        return "[LASTFM] Top Chart Track: " +
                Colour.BOLD + trackName + Colour.RESET +
                " by " + Colour.BOLD + trackArtist;

    }

}
