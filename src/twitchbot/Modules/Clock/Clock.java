package twitchbot.Modules.Clock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import twitchbot.Commands.ChatFunction;
import twitchbot.Modules.BotModule;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;

public class Clock extends BotModule {

    public Clock(TwitchBot bot) {
        super(bot);
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        cmds.put("!date", new ChatFunction(Permission.NORMAL, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                bot.botMessage("The current date is " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + ".");
            }

        });
        cmds.put("!time", new ChatFunction(Permission.NORMAL, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                bot.botMessage("The current time is " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " GMT.");
            }

        });
        return cmds;
    }

}
