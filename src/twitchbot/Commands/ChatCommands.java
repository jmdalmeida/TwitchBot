package twitchbot.Commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import twitchbot.Modules.BotModule;
import twitchbot.Modules.PriorityLevel;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;
import twitchbot.Viewers.Viewer;
import twitchbot.Viewers.Viewers;

public class ChatCommands extends BotModule {

    public static String normalizeMessage(String msg) {
        return msg.trim();
    }

    public static String extractMessage(String[] exclude, String msg) {
        List<String> words = new ArrayList<>(Arrays.asList(msg.split(" ")));
        List<String> exc = new ArrayList<>(Arrays.asList(exclude));
        Iterator<String> i = words.iterator();
        while (i.hasNext()) {
            String s = i.next();
            if (exc.contains(s)) {
                i.remove();
                exc.remove(s);
            }
        }
        String m = "";
        m = words.stream().map((s) -> s + " ").reduce(m, String::concat);
        return m;
    }

    private static final String CUSTOM_FILENAME = "customcommands";

    private Map<String, ChatFunction> moduleCommands;
    private Map<String, ChatFunction> customCommands;

    public ChatCommands(TwitchBot bot) {
        super(bot, PriorityLevel.NORMAL);
    }

    public void setupCommands(Object[] modules) {
        moduleCommands = new HashMap<>();
        for (Object o : modules) {
            BotModule bm = (BotModule) o;
            moduleCommands.putAll(bm.getModuleCommands());
        }
        try {
            setupCustomCommands();
        } catch (FileNotFoundException ex) {
            System.err.println("Error setting up custom commands: " + ex.getMessage());
        }
    }

    private void setupCustomCommands() throws FileNotFoundException {
        customCommands = new HashMap<>();
        URL url = getClass().getResource(CUSTOM_FILENAME);
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
                    if (!moduleCommands.containsKey(c)) {
                        addCustomCommand(c, msg, enabled);
                    }
                });
            }
        }
    }

    private void addCustomCommand(String key, String msg, boolean enabled) {
        customCommands.putIfAbsent(key, new ChatFunction(Permission.NORMAL, enabled) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                bot.botMessage(msg);
            }
        });
    }

    private void removeCustomCommand(String key) {
        customCommands.remove(key);
    }

    private void setCustomCommandState(String key, boolean enabled) {
        ChatFunction cf = customCommands.get(key);
        if (cf != null) {
            cf.setStatus(enabled);
        }
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        String[] tokens = message.split(" ");
        if (tokens.length == 0) {
            return;
        }
        String cmd = tokens[0];
        if (moduleCommands.containsKey(cmd)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    ChatFunction func = moduleCommands.get(cmd);
                    if (func.getPermission() != Permission.NORMAL) {
                        Viewer u = ((Viewers) bot.getModuleManager().getModule("Viewers")).getViewer(sender);
                        if (u != null) {
                            if (func.getPermission().getValue() <= u.getPermissionLevel().getValue()) {
                                func.doFunction(channel, sender, login, hostname, message);
                            } else {
                                bot.botMessage("You don't have permission to run this command, " + sender + ".");
                            }
                        } else {
                            System.err.println("Error processing " + sender + "'s command (" + cmd + "): Viewer not listed.");
                        }
                    } else {
                        func.doFunction(channel, sender, login, hostname, message);
                    }
                }
            }).start();
        } else if (customCommands.containsKey(cmd) && tokens.length == 1) {
            customCommands.get(cmd).doFunction(channel, sender, login, hostname, message);
        }
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        cmds.put("!commands", new ChatFunction(Permission.NORMAL, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String cmd = moduleCommands.keySet().toString();
                bot.botMessage("Available commands are: " + cmd.substring(1, cmd.length() - 1) + ".");
            }

        });
        //!addcommand <command> <message>
        cmds.put("!addcommand", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String[] tokens = message.split(" ");
                if (tokens.length < 3) {
                    return;
                }
                String[] exc = {tokens[0], tokens[1]};
                String msg = extractMessage(exc, message);
                addCustomCommand(tokens[1], msg, true);
            }

        });
        //!removecommand <command>
        cmds.put("!removecommand", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String[] tokens = message.split(" ");
                if (tokens.length < 2) {
                    return;
                }
                removeCustomCommand(tokens[1]);
            }

        });
        return cmds;
    }

}
