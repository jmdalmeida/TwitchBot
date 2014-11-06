package twitchbot.Viewers;

public class Viewer {
    
    private final String username;
    private Permission permissionLevel;
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

    public void setPermissionLevel(Permission permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public long getTimeConnected() {
        return timeConnected;
    }

    @Override
    public String toString() {
        return username + " (" + permissionLevel.toString() + ")";
    }

}