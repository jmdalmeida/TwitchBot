package twitchbot.Commands;

import twitchbot.Viewers.Permission;

interface DoFunction {
    void function(String channel, String sender, String login, String hostname, String message);
}

public abstract class ChatFunction implements DoFunction {

    private final Permission permissionNeeded;
    private boolean enabled;

    public ChatFunction(Permission permissionNeeded, boolean enabled) {
        this.permissionNeeded = permissionNeeded;
        this.enabled = enabled;
    }

    public Permission getPermission() {
        return permissionNeeded;
    }

    public void setStatus(boolean enabled) {
        this.enabled = enabled;
    }

    public void doFunction(String channel, String sender, String login, String hostname, String message) {
        if (enabled) {
            function(channel, sender, login, hostname, message);
        }
    }

}
