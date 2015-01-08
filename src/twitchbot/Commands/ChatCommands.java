package twitchbot.Commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import twitchbot.Config.Configuration;
import twitchbot.Modules.BotModule;
import twitchbot.Modules.PriorityLevel;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;
import twitchbot.Viewers.Viewer;
import twitchbot.Viewers.Viewers;

public class ChatCommands extends BotModule {

    private final TwitchBot bot;
    private Map<String, ChatFunction> commands;

    public ChatCommands(TwitchBot bot) {
        super(bot, PriorityLevel.NORMAL);
        this.bot = bot;
    }

    public void setupCommands(Object[] modules) {
        commands = new HashMap<>();
        for (Object o : modules) {
            BotModule bm = (BotModule) o;
            commands.putAll(bm.getModuleCommands());
        }
        try {
            setupCustomCommands();
        } catch (FileNotFoundException ex) {
            System.err.println("Error setting up custom commands: " + ex.getMessage());
        }
    }

    private void setupCustomCommands() throws FileNotFoundException {
        URL url = getClass().getResource(Configuration.getInstance().getValue("CHATCOMMANDS_filename"));
        try (Scanner scanner = new Scanner(new File(url.getPath()))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] ss = line.split("#");
                List<String> cmds = new ArrayList<>();
                cmds.addAll(Arrays.asList(ss[0].split("&")));
                boolean enabled = "true".equalsIgnoreCase(ss[1]);
                String msg = ss[2];
                cmds.stream().forEach((c) -> {
                    commands.putIfAbsent(c, new ChatFunction(Permission.NORMAL, enabled) {

                        @Override
                        public void function(String channel, String sender, String login, String hostname, String message) {
                            bot.sendMessage(channel, msg);
                        }
                    });
                });
            }
        };
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        String[] tokens = message.split(" ");
        if (tokens.length == 0) {
            return;
        }
        String cmd = tokens[0];
        if (commands.containsKey(cmd)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    ChatFunction func = commands.get(cmd);
                    if (func.getPermission() != Permission.NORMAL) {
                        Viewer u = ((Viewers) bot.getModule("Viewers")).getViewer(sender);
                        if (u != null) {
                            if (func.getPermission().getValue() <= u.getPermissionLevel().getValue()) {
                                func.doFunction(channel, sender, login, hostname, message);
                            } else {
                                bot.sendMessage(channel, "You don't have permission to run this command, " + sender + ".");
                            }
                        } else {
                            System.err.println("Error processing " + sender + "'s command (" + cmd + "): Viewer not listed.");
                        }
                    } else {
                        func.doFunction(channel, sender, login, hostname, message);
                    }
                }
            }).start();
        }
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        cmds.put("!commands", new ChatFunction(Permission.NORMAL, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String cmd = commands.keySet().toString();
                bot.sendMessage(channel, "Available commands are: " + cmd.substring(1, cmd.length() - 1) + ".");
            }

        });
        return cmds;
    }

}
