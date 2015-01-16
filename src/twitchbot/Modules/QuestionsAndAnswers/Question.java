package twitchbot.Modules.QuestionsAndAnswers;

public class Question {

    private final String question, viewer;

    public Question(String viewer, String question) {
        this.question = question;
        this.viewer = viewer;
    }

    public String getQuestion() {
        return question;
    }

    public String getViewer() {
        return viewer;
    }

}
