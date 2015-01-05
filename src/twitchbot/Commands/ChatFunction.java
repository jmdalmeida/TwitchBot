package twitchbot.Commands;

import twitchbot.Viewers.Permission;

interface DoFunction {
    void doFunction(String channel, String sender, String login, String hostname, String message);
}

public abstract class ChatFunction implements DoFunction {
    private final Permission permissionNeeded;

    public ChatFunction(Permission permissionNeeded) {
        this.permissionNeeded = permissionNeeded;
    }

    public Permission getPermission() {
        return permissionNeeded;
    }
    
}
