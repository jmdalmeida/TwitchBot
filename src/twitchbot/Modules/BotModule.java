package twitchbot.Modules;

import java.util.HashMap;
import java.util.Map;
import org.jibble.pircbot.User;
import twitchbot.Commands.ChatFunction;
import twitchbot.TwitchBot;

public abstract class BotModule {

    protected final TwitchBot bot;
    protected final PriorityLevel priority;

    public BotModule(TwitchBot bot, PriorityLevel priority) {
        this.bot = bot;
        this.priority = priority;
    }

    /**
     *
     * @return Module commands list to append to the bot command list. If null,
     */
    public Map<String, ChatFunction> getModuleCommands() {
        return new HashMap<>();
    }

    public void onMessage(String channel, String sender, String login, String hostname, String message) {
    }

    public void onJoin(String channel, String sender, String login, String hostname) {
    }

    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
    }

    public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
    }

    public void onUserList(String channel, User[] users) {
    }

}
