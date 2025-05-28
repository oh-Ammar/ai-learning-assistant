package com.example.aiquizgenerator;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GenerateQuiz extends AppCompatActivity {

    private TextView inputText;
    private Button generateQuizButton, submitAnswersButton;
    private TextView quizOutput;
    private ProgressBar progressBar;

    private JSONArray generatedQuestions;
    private ArrayList<String> correctAnswers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_quiz);


        inputText = findViewById(R.id.inputText);
        generateQuizButton = findViewById(R.id.generateQuizButton);
        submitAnswersButton = findViewById(R.id.submitAnswersButton);
        quizOutput = findViewById(R.id.quizOutput);
        progressBar = findViewById(R.id.progressBar);

        generateQuizButton.setOnClickListener(v -> generateQuiz());
        submitAnswersButton.setOnClickListener(v -> submitAnswers());
    }

    private void generateQuiz() {
        String summarizedText = inputText.getText().toString().trim();
        Log.d("QUIZ_GENERATOR", "Raw input: " + summarizedText);  // 🔍 Log 1

        if (summarizedText.isEmpty()) {
            Toast.makeText(this, "Please enter some text.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        quizOutput.setVisibility(View.GONE);
        submitAnswersButton.setVisibility(View.GONE);

        try {
            JSONObject json = new JSONObject();
            json.put("text", summarizedText);

            ApiClient.post("/generate_quiz", json.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        try {
                            Log.d("QUIZ_GENERATOR", "Raw response: " + response);  // 🔍 Log 2

                            generatedQuestions = new JSONArray(response);
                            correctAnswers.clear();
                            for (int i = 0; i < generatedQuestions.length(); i++) {
                                correctAnswers.add(generatedQuestions.getJSONObject(i).getString("answer"));
                            }
                            Log.d("QUIZ_GENERATOR", "Generated questions: " + generatedQuestions.length());  // 🔍 Log 3

                            showQuiz();
                            submitAnswersButton.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            quizOutput.setText("Failed to parse quiz data.");
                            quizOutput.setVisibility(View.VISIBLE);
                            Log.e("QUIZ_GENERATOR", "Parse exception: ", e);  // 🔍 Log error
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GenerateQuiz.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        Log.e("QUIZ_GENERATOR", "API Failure: " + error);  // 🔍 Log failure
                    });
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error preparing request.", Toast.LENGTH_SHORT).show();
            Log.e("QUIZ_GENERATOR", "Request prep error: ", e);  // 🔍 Log exception
        }
    }


    private void showQuiz() {
        if (generatedQuestions == null) return;

        StringBuilder quizText = new StringBuilder();

        for (int i = 0; i < generatedQuestions.length(); i++) {
            try {
                JSONObject q = generatedQuestions.getJSONObject(i);
                quizText.append(i + 1).append(". ").append(q.getString("question")).append("\n\n");
            } catch (Exception e) {
                quizText.append("Error loading question.\n");
            }
        }

        quizOutput.setText(quizText.toString());
        quizOutput.setVisibility(View.VISIBLE);
    }

    private void submitAnswers() {
        if (generatedQuestions == null || correctAnswers.isEmpty()) {
            Toast.makeText(this, "Generate a quiz first.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        try {
            JSONArray userAnswers = new JSONArray();
            for (int i = 0; i < correctAnswers.size(); i++) {
                userAnswers.put("A"); // TODO: Replace with real input when UI ready
            }

            JSONObject json = new JSONObject();
            json.put("user_answers", userAnswers);
            json.put("correct_answers", new JSONArray(correctAnswers));

            ApiClient.post("/check_answers", json.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONArray results = new JSONArray(response);
                            int correctCount = 0;
                            StringBuilder resultText = new StringBuilder();

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject res = results.getJSONObject(i);
                                boolean isCorrect = res.getBoolean("is_correct");
                                if (isCorrect) correctCount++;

                                resultText.append(i + 1).append(". ")
                                        .append("Your answer: ").append(res.getString("user_answer"))
                                        .append("\nCorrect answer: ").append(res.getString("correct_answer"))
                                        .append("\nSimilarity: ").append(String.format("%.2f", res.getDouble("similarity")))
                                        .append("\nResult: ").append(isCorrect ? "✅ Correct" : "❌ Incorrect")
                                        .append("\n\n");
                            }

                            int total = results.length();
                            int percent = (int) ((correctCount / (float) total) * 100);
                            resultText.insert(0, "Score: " + percent + "%\n\n");

                            quizOutput.setText(resultText.toString());
                            quizOutput.setVisibility(View.VISIBLE);

                        } catch (Exception e) {
                            quizOutput.setText("Error parsing result.");
                            quizOutput.setVisibility(View.VISIBLE);
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(GenerateQuiz.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error submitting answers.", Toast.LENGTH_SHORT).show();
        }
    }
}
