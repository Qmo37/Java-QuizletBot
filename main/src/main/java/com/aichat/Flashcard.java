package com.aichat;

public class Flashcard {

    private String question;
    private String answer;
    private boolean isShowingAnswer;

    public Flashcard(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.isShowingAnswer = false;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isShowingAnswer() {
        return isShowingAnswer;
    }

    public void toggleAnswer() {
        isShowingAnswer = !isShowingAnswer;
    }
}
