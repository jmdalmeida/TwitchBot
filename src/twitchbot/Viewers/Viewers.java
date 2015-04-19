package twitchbot.Viewers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitchbot.Config.Configuration;
import twitchbot.Modules.BotModule;
import twitchbot.TwitchAPI;
import twitchbot.TwitchBot;

public class Viewers extends BotModule {

    private Map<String, Viewer> viewers;

    public Viewers(TwitchBot bot) {
        super(bot);
        setupViewers();
    }

    private void setupViewers() {
        viewers = new HashMap<>();
        try {
            Viewer[] currentViewers = TwitchAPI.getViewers(bot.getChannel());
            for (Viewer v : currentViewers) {
                if (v.getUsername().equalsIgnoreCase(Configuration.getInstance().getProperty("broadcaster"))) {
                    v.setPermissionLevel(Permission.BROADCASTER);
                }
                viewers.put(v.getUsername().toLowerCase(), v);
                System.out.println("API - Viewers list: Adding " + v.toString() + ".");
            }
        } catch (IOException ex) {
            Logger.getLogger(Viewers.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean exists(String username) {
        return viewers.containsKey(username.toLowerCase());
    }

    public void setPermission(String username, Permission p) {
        viewers.get(username.toLowerCase()).setPermissionLevel(p);
    }

    public Viewer getViewer(String username) {
        return viewers.get(username.toLowerCase());
    }

    public void addNewViewer(String username) {
        if (exists(username)) {
            return;
        }
        Permission p = username.equalsIgnoreCase(Configuration.getInstance().getProperty("broadcaster")) ? Permission.BROADCASTER : Permission.NORMAL;
        String nick = username.toLowerCase();
        Viewer v = new Viewer(nick, p);
        viewers.put(nick, v);
        System.out.println("Viewers list: Adding " + nick + ".");
    }

    public void removeViewer(String sourceNick) {
        viewers.remove(sourceNick);
        System.out.println("Viewers list: Removing " + sourceNick + ".");
    }

    public Viewer[] getViewers() {
        return viewers.values().toArray(new Viewer[0]);
    }

    public Viewer[] getMods() {
        List<Viewer> mods = new ArrayList<>();
        for (Viewer v : getViewers()) {
            if (v.getPermissionLevel().getValue() >= Permission.MODERATOR.getValue()) {
                mods.add(v);
            }
        }
        return mods.toArray(new Viewer[0]);
    }

    public void listViewers() {
        System.out.println("**** Listing viewers ****");
        Object[] vs = getViewers();
        for (Object o : vs) {
            Viewer v = (Viewer) o;
            System.out.println("* " + v.toString());
        }
        System.out.println("*************************");
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname) {
        addNewViewer(sender);
    }

    @Override
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        removeViewer(sourceNick);
    }

    @Override
    public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        //#Channel +o user
        String[] params = mode.split(" ");
        String chn = params[0];
        String operation = params[1];
        String username = params[2];
        if (chn.equalsIgnoreCase("#" + bot.getChannel()) && viewers.containsKey(username)) {
            if (!username.equalsIgnoreCase(Configuration.getInstance().getProperty("broadcaster"))) {
                switch (operation) {
                    case "+o":
                        setPermission(username, Permission.MODERATOR);
                        break;
                    case "-o":
                        setPermission(username, Permission.NORMAL);
                        break;
                }
            } else {
                setPermission(username, Permission.BROADCASTER);
            }
        }
    }

}
