package twitchbot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;
import twitchbot.Commands.ChatCommands;
import twitchbot.Config.Configuration;
import twitchbot.Viewers.Permission;
import twitchbot.Viewers.Viewer;
import twitchbot.WordFilter.WordFilter;

public class TwitchBot extends PircBot {

    private final ChatCommands commands;
    private final Map<String, Viewer> viewers;
    private WordFilter wordFilter;

    private final String channel;
    private long connectedTimestamp;

    public TwitchBot() {
        connectedTimestamp = System.nanoTime();
        this.setName(Configuration.getInstance().getValue("BOT_username"));
        this.setVerbose(true);
        try {
            this.connect("irc.twitch.tv", 6667, Configuration.getInstance().getValue("BOT_oauth"));
        } catch (IOException | IrcException ex) {
            Logger.getLogger(TwitchBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        channel = Configuration.getInstance().getValue("CHANNEL_name");
        this.joinChannel("#" + channel);
        commands = new ChatCommands(this);
        viewers = new HashMap<>();
        setupWordFilter();
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        commands.runCommand(channel, sender, login, hostname, message);
        String[] msg = message.split(" ");
        if (wordFilter.isRunning() && !wordFilter.validateWords(msg)) {
            this.sendMessage(channel, "/" + Configuration.getInstance().getValue("WORDFILTER_action") + " " + sender);
            this.sendMessage(channel, Configuration.getInstance().getValue("WORDFILTER_output").replace("$sender$", sender));
        }
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        addNewViewer(sender);
        if (!sender.equalsIgnoreCase(Configuration.getInstance().getValue("BOT_username"))) {
            sendMessage(channel, "Hello " + sender + ", how are you?");
        } else {
            this.sendMessage(channel, "Up and running!");
        }
    }

    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        removeViewer(sourceNick);
    }

    @Override
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        //#Channel +o user
        String[] params = mode.split(" ");
        String chn = params[0];
        String operation = params[1];
        String username = params[2];
        if (chn.equalsIgnoreCase("#" + channel) && viewers.containsKey(username)) {
            if (!username.equalsIgnoreCase(Configuration.getInstance().getValue("BROADCASTER_username"))) {
                switch (operation) {
                    case "+o":
                        viewers.get(username).setPermissionLevel(Permission.MODERATOR);
                        break;
                    case "-o":
                        viewers.get(username).setPermissionLevel(Permission.NORMAL);
                        break;
                }
            } else {
                viewers.get(username).setPermissionLevel(Permission.BROADCASTER);
            }
        }
    }

    private void setupWordFilter() {
        wordFilter = new WordFilter(Configuration.getInstance().getValue("WORDFILTER_status"));
    }

    public void addNewViewer(String username) {
        Permission p = Permission.NORMAL;
        String nick = username.toLowerCase();
        Viewer v = new Viewer(nick, p, System.nanoTime());
        viewers.put(nick, v);
        System.out.println("Users list: Adding " + nick + "(" + p.toString() + ")");
    }

    public void removeViewer(String sourceNick) {
        viewers.remove(sourceNick);
        System.out.println("Users list: Removing " + sourceNick + "");
    }

    public void quitAndExit() {
        this.sendMessage("#" + channel, "I'm out, cya!");
        this.partChannel(channel);
        this.quitServer();
        try {
            Thread.sleep(1000); //give it time to send message
        } catch (InterruptedException ex) {
            Logger.getLogger(TwitchBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.dispose();
    }

    public Map<String, Viewer> getViewers() {
        return viewers;
    }

    public long getConnectedTimestamp() {
        return connectedTimestamp;
    }

}
