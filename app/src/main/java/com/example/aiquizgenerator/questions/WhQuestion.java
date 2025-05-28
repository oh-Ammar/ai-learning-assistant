package com.example.aiquizgenerator.questions;
import com.example.aiquizgenerator.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;
public class WhQuestion extends QuestionBank {

    public WhQuestion(String questionText, String modelAnswer) {
        super(questionText, modelAnswer);
    }

    @Override
    public String getQuestionType() {
        return "wh";
    }

    @Override
    public boolean isAnswerValid(String userAnswer) {
        // This method will only work with sync logic or callback wrapper.
        // For now, we use placeholder logic; actual validation will be async
        return false; // Placeholder
    }

    @Override
    public String formatForDisplay() {
        return "Q: " + questionText;
    }

    // Utility method to send answer to Flask and handle callback
    public void validateAnswer(String userAnswer, AnswerValidationCallback callback) {
        try {
            JSONArray userAnswers = new JSONArray();
            JSONArray correctAnswers = new JSONArray();
            userAnswers.put(userAnswer);
            correctAnswers.put(modelAnswer);

            JSONObject json = new JSONObject();
            json.put("user_answers", userAnswers);
            json.put("correct_answers", correctAnswers);

            ApiClient.post("/check_answers", json.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONArray results = new JSONArray(response);
                        JSONObject res = results.getJSONObject(0);
                        boolean isCorrect = res.getBoolean("is_correct");
                        callback.onResult(isCorrect, res.getDouble("similarity"));
                    } catch (Exception e) {
                        callback.onError("Parse error");
                    }
                }

                @Override
                public void onFailure(String error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public interface AnswerValidationCallback {
        void onResult(boolean isCorrect, double similarity);
        void onError(String errorMessage);
    }
}