package twitchbot.Commands;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import twitchbot.Modules.BotModule;
import twitchbot.TwitchAPI;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;
import twitchbot.Viewers.Viewers;

public class DefaultCommands extends BotModule {

    public DefaultCommands(TwitchBot bot) {
        super(bot);
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        cmds.put("!uptime", new ChatFunction(Permission.NORMAL, true) {
            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                try {
                    Date uptime = TwitchAPI.getUptime(channel.substring(1));
                    if(uptime == null){
                        bot.botMessage("The channel is offline.");
                        return;
                    }
                    long elapsedTime = System.currentTimeMillis() - uptime.getTime();
                    final long hr = TimeUnit.MILLISECONDS.toHours(elapsedTime);
                    final long min = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
                    bot.botMessage("The channel has been live for " + String.format("%02d hours and %02d minutes", hr, min) + ".");
                } catch (IOException ex) {
                    bot.botMessage("Couldn't retrieve uptime. Try again later.");
                }
            }

        });
        cmds.put("!debug", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                ((Viewers) bot.getModuleManager().getModule("Viewers")).listViewers();
            }

        });
        cmds.put("!quit", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                bot.quitAndExit();
            }

        });
        cmds.put("!mute", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                bot.mute();
            }

        });
        cmds.put("!unmute", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                bot.unmute();
            }

        });
        return cmds;
    }

}
