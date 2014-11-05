package twitchbot.Viewers;

public class Viewer {
    
    private final String username;
    private final Permission permissionLevel;
    private final long timeConnected;

    public Viewer(String username, Permission permissionLevel, long timeConnected) {
        this.username = username;
        this.permissionLevel = permissionLevel;
        this.timeConnected = timeConnected;
    }

    public String getUsername() {
        return username;
    }

    public Permission getPermissionLevel() {
        return permissionLevel;
    }

    public long getTimeConnected() {
        return timeConnected;
    }

}