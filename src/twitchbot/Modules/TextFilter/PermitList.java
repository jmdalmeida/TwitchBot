package twitchbot.Modules.TextFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import twitchbot.Config.Configuration;

/**
 * Viewers in the Permit List can post links, spam, etc...
 *
 * @author Joao
 */
public class PermitList {
    private static final String PERMITS_FILENAME = "permitlist";

    private List<String> permitList;

    public PermitList() {
        permitList = new ArrayList<>();
        try {
            loadPermits();
        } catch (FileNotFoundException ex) {
            System.err.println("Error loading permits: " + ex.getMessage());
        }
    }

    private void loadPermits() throws FileNotFoundException {
        URL url = getClass().getResource(PERMITS_FILENAME);
        try (Scanner scanner = new Scanner(new File(url.getPath()))) {
            while (scanner.hasNextLine()) {
                String s = scanner.nextLine();
                String[] ss = s.split("#");
                permitList.addAll(Arrays.asList(ss));
            }
        }
    }

    public void savePermits() throws FileNotFoundException, UnsupportedEncodingException {
        String newpermits = "";
        for (String s : permitList) {
            newpermits += s + "#";
        }
        newpermits = newpermits.substring(0, newpermits.length() - 1);
        URL url = getClass().getResource(PERMITS_FILENAME);
        try (PrintWriter writer = new PrintWriter(new File(url.getPath()));) {
            writer.write(newpermits);
        }
    }

    public void addPermit(String v) {
        if (!permitList.contains(v)) {
            permitList.add(v);
        }
    }

    public void removePermit(String v) {
        permitList.remove(v);
    }

    public boolean isPermited(String v) {
        return permitList.contains(v);
    }

    @Override
    public String toString() {
        String s = "";
        for (String p : permitList) {
            s += p + ", ";
        }
        return (s.length() > 2) ? s.substring(0, s.length() - 2) : "";
    }

}
