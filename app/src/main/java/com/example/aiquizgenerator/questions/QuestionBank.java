package com.example.aiquizgenerator.questions;

public abstract class QuestionBank {

    protected String questionText;
    protected String modelAnswer;

    public QuestionBank(String questionText, String modelAnswer) {
        this.questionText = questionText;
        this.modelAnswer = modelAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getModelAnswer() {
        return modelAnswer;
    }

    public abstract String getQuestionType();

    public abstract boolean isAnswerValid(String userAnswer);

    public abstract String formatForDisplay();
}
