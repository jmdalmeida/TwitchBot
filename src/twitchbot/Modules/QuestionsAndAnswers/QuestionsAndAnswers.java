package twitchbot.Modules.QuestionsAndAnswers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import twitchbot.Commands.ChatCommands;
import twitchbot.Commands.ChatFunction;
import twitchbot.Modules.BotModule;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;

public class QuestionsAndAnswers extends BotModule {

    private static final int QUESTION_LIMIT = 1000;
    private static final String MESSAGE_PREFIX = "Q&A > ";

    private final LinkedList<Question> questions;
    private Question currentQuestion;
    private boolean isOngoing;

    public QuestionsAndAnswers(TwitchBot bot) {
        super(bot);
        questions = new LinkedList<>();
        isOngoing = false;
    }

    public void startQA() {
        if (!isOngoing) {
            isOngoing = true;
            questions.clear();
            bot.botMessage(MESSAGE_PREFIX + "A new Q&A has started. Type !question \"question here\" to submit your question to the broadcaster.");
        }
    }

    public void endQA() {
        if (isOngoing) {
            isOngoing = false;
            questions.clear();
            bot.botMessage(MESSAGE_PREFIX + "The Q&A has ended.");
        }
    }

    private void addQuestion(String v, String q) {
        if (isOngoing) {
            //Check if there's already a question by this viewer
            Question[] array = questions.toArray(new Question[questions.size()]);
            for (Question qe : array) {
                if (qe.getViewer().equals(v)) {
                    return;
                }
            }
            if (questions.size() < QUESTION_LIMIT) {
                questions.add(new Question(v, q));
            }
        }
    }

    private void nextQuestion() {
        if (isOngoing) {
            currentQuestion = questions.pollFirst();
            if (currentQuestion != null) {
                bot.botMessage(MESSAGE_PREFIX + "Question: \"" + currentQuestion.getQuestion() + "\" by "
                        + currentQuestion.getViewer() + ". " + questions.size() + " questions left. (@" + bot.getChannel() + ")");
            } else {
                bot.botMessage(MESSAGE_PREFIX + "There's no questions in queue. (@" + bot.getChannel() + ")");
            }
        }
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        cmds.put("!startqa", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                startQA();
            }
        });
        cmds.put("!endqa", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                endQA();
            }
        });
        cmds.put("!nextquestion", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                nextQuestion();
            }
        });
        cmds.put("!question", new ChatFunction(Permission.NORMAL, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String[] exc = {"!question"};
                String question = ChatCommands.extractMessage(exc, message);
                if (question.length() == 0) {
                    return;
                }
                addQuestion(sender, question);
            }
        });
        return cmds;
    }

}
