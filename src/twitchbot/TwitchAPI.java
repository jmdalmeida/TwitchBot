package twitchbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import twitchbot.Config.Configuration;
import twitchbot.Viewers.Permission;
import twitchbot.Viewers.Viewer;

public class TwitchAPI {

    private static final JSONParser PARSER = new JSONParser();

    private static String readJsonFromUrl(String targetURL) throws IOException {
        String result = "";
        URI uri = URI.create(targetURL).normalize();
        URL url = uri.toURL();
        URLConnection mdb = url.openConnection();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(mdb.getInputStream()))) {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                result += inputLine;
            }
        }
        return result;
    }

    public static Viewer[] getViewers(String channel) throws IOException {
        List<Viewer> viewers = new ArrayList<>();

        String jsonStr = readJsonFromUrl("https://tmi.twitch.tv/group/user/" + channel + "/chatters");
        try {
            Object obj = PARSER.parse(jsonStr);
            JSONObject jsonObj = (JSONObject) obj;
            JSONObject a2 = (JSONObject) jsonObj.get("chatters");

            JSONArray aMods = (JSONArray) a2.get("moderators");
            aMods.addAll((JSONArray) a2.get("staff"));
            aMods.addAll((JSONArray) a2.get("admins"));
            aMods.addAll((JSONArray) a2.get("global_mods"));
            JSONArray aViewers = (JSONArray) a2.get("viewers");

            aMods.stream().forEach((o) -> {
                viewers.add(new Viewer((String) o, Permission.MODERATOR));
            });
            aViewers.stream().forEach((o) -> {
                viewers.add(new Viewer((String) o, Permission.NORMAL));
            });
        } catch (ParseException ex) {
            Logger.getLogger(TwitchAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return viewers.toArray(new Viewer[viewers.size()]);
    }

    private static String[] getSubscribers(String channel) throws IOException {
        // TODO: Figure a way to gather subscribers list from a channel
        List<Viewer> subs = new ArrayList<>();

        String client_id = Configuration.getInstance().getProperty("client_id");
        String jsonStr = readJsonFromUrl("https://api.twitch.tv/kraken/channels/" + channel + "/subscriptions?client_id=" + client_id);

        return new String[subs.size()];
    }

    public static Date getUptime(String channel) throws IOException {
        Date uptime = null;
        String client_id = Configuration.getInstance().getProperty("client_id");
        String jsonStr = readJsonFromUrl("https://api.twitch.tv/kraken/streams/" + channel + "?client_id=" + client_id);
        try {
            Object obj = PARSER.parse(jsonStr);
            JSONObject jsonObj = (JSONObject) obj;
            JSONObject streamObj = (JSONObject) jsonObj.get("stream");
            if (streamObj != null) {
                String date = (String) streamObj.get("created_at");
                DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                uptime = utcFormat.parse(date);
            }
        } catch (ParseException | java.text.ParseException ex) {
            Logger.getLogger(TwitchAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uptime;
    }

}
