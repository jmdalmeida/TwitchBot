package twitchbot.Viewers;

import java.util.HashMap;
import java.util.Map;
import org.jibble.pircbot.User;
import twitchbot.Config.Configuration;
import twitchbot.Modules.BotModule;
import twitchbot.Modules.PriorityLevel;
import twitchbot.TwitchBot;

public class Viewers extends BotModule {

    private final Map<String, Viewer> viewers;

    public Viewers(TwitchBot bot) {
        super(bot, PriorityLevel.NORMAL);
        viewers = new HashMap<>();
    }

    public boolean exists(String username) {
        return viewers.containsKey(username);
    }

    public void setPermission(String username, Permission p) {
        viewers.get(username).setPermissionLevel(p);
    }

    public Viewer getViewer(String username) {
        return viewers.get(username);
    }

    public void addNewViewer(String username) {
        if (viewers.containsKey(username)) {
            return;
        }
        Permission p = username.equalsIgnoreCase(Configuration.getInstance().getValue("BROADCASTER_username")) ? Permission.BROADCASTER : Permission.NORMAL;
        String nick = username.toLowerCase();
        Viewer v = new Viewer(nick, p, System.nanoTime());
        viewers.put(nick, v);
        System.out.println("Viewers list: Adding " + nick + ".");
    }

    public void removeViewer(String sourceNick) {
        viewers.remove(sourceNick);
        System.out.println("Viewers list: Removing " + sourceNick + ".");
    }

    public Object[] getViewers() {
        return viewers.values().toArray();
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
            if (!username.equalsIgnoreCase(Configuration.getInstance().getValue("BROADCASTER_username"))) {
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
