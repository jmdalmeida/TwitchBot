package twitchbot.Viewers;

import java.util.HashMap;
import java.util.Map;

public class Viewers {

    private final Map<String, Viewer> viewers;

    public Viewers() {
        viewers = new HashMap<>();
    }

    public boolean exists(String username) {
        return viewers.containsKey(username);
    }

    public void set(String username, Permission p) {
        viewers.get(username).setPermissionLevel(p);
    }

    public Viewer getViewer(String username) {
        return viewers.get(username);
    }

    public void addNewViewer(String username) {
        Permission p = Permission.NORMAL;
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
        System.out.println("*** Listing viewers ***");
        Object[] vs = getViewers();
        for (Object o : vs) {
            Viewer v = (Viewer) o;
            System.out.println("* " + v.toString());
        }
        System.out.println("***********************");
    }

}
