/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twitchbot.WordFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitchbot.Config.Configuration;

public class WordFilter {
    private List<String> wordList;
    private boolean running;

    public WordFilter() {
        running = Configuration.getInstance().getValue("WORDFILTER_status").equalsIgnoreCase("on");
        if (running) {
            setup();
        }
    }

    private void setup() {
        wordList = new IgnoreCaseArrayList();
        try {
            setupWords();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WordFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean validateWords(String[] words) {
        for (String s : words) {
            if (wordList.contains(s.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private void setupWords() throws FileNotFoundException {
        URL url = getClass().getResource(Configuration.getInstance().getValue("WORDFILTER_filename"));
        try (Scanner scanner = new Scanner(new File(url.getPath()))) {
            while (scanner.hasNextLine()) {
                wordList.add(scanner.next());
            }
        };
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
