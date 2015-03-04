package twitchbot.Modules.TextFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitchbot.Commands.ChatFunction;
import twitchbot.Config.Configuration;
import twitchbot.Modules.BotModule;
import twitchbot.Modules.PriorityLevel;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;

public class TextFilter extends BotModule {

    private static final String FILENAME = "textlist";

    private PermitList permitList;
    private List<String> textList;
    private boolean running;
    private int maxLen;

    public TextFilter(TwitchBot bot) {
        super(bot, PriorityLevel.MAJOR);
        setup();
    }

    private void setup() {
        textList = new IgnoreCaseArrayList();
        permitList = new PermitList();
        try {
            setupTextList();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TextFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        maxLen = Integer.parseInt(Configuration.getInstance().getProperty("maxlen"));
    }

    public boolean isRunning() {
        return running;
    }

    public boolean validateText(String sender, String text) {
        if (permitList.isPermited(sender)) {
            return true;
        }
        if (maxLen > 0 && text.length() > maxLen) {
            return false;
        }
        for (String s : textList) {
            if (text.contains(s)) {
                return false;
            }
        }
        return true;
    }

    private void setupTextList() throws FileNotFoundException {
        URL url = getClass().getResource(FILENAME);
        try (Scanner scanner = new Scanner(new File(url.getPath()))) {
            while (scanner.hasNextLine()) {
                textList.add(scanner.nextLine());
            }
        }
    }

    public void setViewerPermit(String v, boolean permit) {
        if (permit) {
            permitList.addPermit(v);
        } else {
            permitList.removePermit(v);
        }
    }

    @Override
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        if (bot.isMe(sourceNick)) {
            try {
                permitList.savePermits();
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                Logger.getLogger(TextFilter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        cmds.put("+permit", new ChatFunction(Permission.MODERATOR, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String[] ss = message.trim().split(" ");
                if (ss.length != 2) {
                    return;
                }
                setViewerPermit(ss[1], true);
            }
        });
        cmds.put("-permit", new ChatFunction(Permission.MODERATOR, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String[] ss = message.trim().split(" ");
                if (ss.length != 2) {
                    return;
                }
                setViewerPermit(ss[1], false);
            }
        });
        cmds.put("!permitlist", new ChatFunction(Permission.MODERATOR, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String s = permitList.toString();
                if (!s.isEmpty()) {
                    bot.botMessage("Permit List: " + permitList.toString());
                }
            }
        });
        return cmds;
    }

    private class IgnoreCaseArrayList extends ArrayList<String> {

        @Override
        public boolean contains(Object o) {
            String paramStr = (String) o;
            for (String s : this) {
                if (paramStr.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            return false;
        }

    }

}
