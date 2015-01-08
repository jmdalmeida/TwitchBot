package twitchbot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;
import twitchbot.Commands.ChatCommands;
import twitchbot.Commands.DefaultCommands;
import twitchbot.Config.Configuration;
import twitchbot.Modules.BotModule;
import twitchbot.Modules.Clock.Clock;
import twitchbot.Modules.Topic.Topic;
import twitchbot.Modules.TextFilter.TextFilter;
import twitchbot.Viewers.Viewers;

public class TwitchBot extends PircBot {

    //Modules
    private Map<String, BotModule> modules;

    private final String channel;
    private long connectedTimestamp;

    public TwitchBot() {
        connectedTimestamp = System.nanoTime();
        setupModules();
        ((ChatCommands) modules.get("ChatCommands")).setupCommands(modules.values().toArray());
        this.setName(Configuration.getInstance().getValue("BOT_username"));
        this.setVerbose(true);
        try {
            this.connect("irc.twitch.tv", 6667, Configuration.getInstance().getValue("BOT_oauth"));
        } catch (IOException | IrcException ex) {
            Logger.getLogger(TwitchBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        channel = Configuration.getInstance().getValue("CHANNEL_name");
        this.joinChannel("#" + channel);
    }

    public String getChannel() {
        return channel;
    }

    private void setupModules() {
        modules = new HashMap<>();
        modules.put("DefaultCommands", new DefaultCommands(this));
        modules.put("ChatCommands", new ChatCommands(this));
        modules.put("TextFilter", new TextFilter(this));
        modules.put("Viewers", new Viewers(this));
        modules.put("Topic", new Topic(this));
        modules.put("Clock", new Clock(this));
    }

    public BotModule getModule(String key) {
        return modules.get(key);
    }

    public void quitAndExit() {
        this.sendMessage("#" + channel, "I'm out, cya!");
        this.partChannel(channel);
        this.quitServer();
        try {
            Thread.sleep(1000); //give it time to send the message
        } catch (InterruptedException ex) {
            Logger.getLogger(TwitchBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.dispose();
    }

    public long getConnectedTimestamp() {
        return connectedTimestamp;
    }

    public boolean isMe(String s) {
        return getName().equals(s);
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        modules.values().stream().forEach((m) -> {
            m.onMessage(channel, sender, login, hostname, message);
        });
    }

    @Override
    protected void onUserList(String channel, User[] users) {
        modules.values().stream().forEach((m) -> {
            m.onUserList(channel, users);
        });
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        if (sender.equalsIgnoreCase(getName())) {
            this.sendMessage(channel, "Up and running!");
        }
        modules.values().stream().forEach((m) -> {
            m.onJoin(channel, sender, login, hostname);
        });
    }

    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        modules.values().stream().forEach((m) -> {
            m.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
        });
    }

    @Override
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        modules.values().stream().forEach((m) -> {
            m.onUserMode(targetNick, sourceNick, sourceLogin, sourceHostname, mode);
        });
    }

}
