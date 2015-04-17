package twitchbot.Modules;

import java.util.HashMap;
import java.util.Map;
import org.jibble.pircbot.User;
import twitchbot.Commands.ChatFunction;
import twitchbot.TwitchBot;

public abstract class BotModule {

    protected final TwitchBot bot;

    public BotModule(TwitchBot bot) {
        this.bot = bot;
    }

    /**
     *
     * @return Module commands list to append to the bot command list.
     */
    public Map<String, ChatFunction> getModuleCommands() {
        return new HashMap<>();
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
    }

    protected void onJoin(String channel, String sender, String login, String hostname) {
    }

    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
    }

    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
    }

    protected void onUserList(String channel, User[] users) {
    }

    protected void onDisconnect() {
    }
    
}
