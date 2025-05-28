package com.example.aiquizgenerator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aiquizgenerator.helpers.OCRProcessor;
import com.example.aiquizgenerator.questions.QuestionBank;
import com.example.aiquizgenerator.questions.QuestionFactory;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.example.aiquizgenerator.questions.WhQuestion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class QuizGeneratorActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_FILE_REQUEST_CODE = 2;

    private Button pasteTextButton, uploadDocButton, uploadImageButton, generateQuizButton, summarizeButton, submitAnswersButton;
    private EditText inputText;
    private ProgressBar progressBar;
    private LinearLayout questionContainer;

    private OCRProcessor ocrProcessor;
    private List<EditText> answerInputs = new ArrayList<>();
    private List<QuestionBank> generatedQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        PDFBoxResourceLoader.init(getApplicationContext());
        setContentView(R.layout.activity_quiz_generator);

        pasteTextButton = findViewById(R.id.pasteTextButton);
        uploadDocButton = findViewById(R.id.uploadDocButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        generateQuizButton = findViewById(R.id.generateQuizButton);
        summarizeButton = findViewById(R.id.summarizeButton);
        inputText = findViewById(R.id.inputText);
        progressBar = findViewById(R.id.progressBar);
        questionContainer = findViewById(R.id.questionContainer);
        submitAnswersButton = findViewById(R.id.submitAnswersButton);

        ocrProcessor = new OCRProcessor();

        pasteTextButton.setOnClickListener(v -> pasteText());
        uploadDocButton.setOnClickListener(v -> openDocumentPicker());
        uploadImageButton.setOnClickListener(v -> openImagePicker());
        generateQuizButton.setOnClickListener(v -> generateQuizFromText());
        summarizeButton.setOnClickListener(v -> summarizeText());
        submitAnswersButton.setOnClickListener(v -> submitAnswers());
    }

    private void pasteText() {
        inputText.setVisibility(View.VISIBLE);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                if (requestCode == PICK_FILE_REQUEST_CODE) {
                    extractTextFromDocument(fileUri);
                } else if (requestCode == PICK_IMAGE_REQUEST) {
                    extractTextFromImage(fileUri);
                }
            }
        }
    }

    private void extractTextFromDocument(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            String extractedText = new DocumentToText().extractTextFromPDF(inputStream);
            inputText.setText(extractedText);
            inputText.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to process document.", Toast.LENGTH_SHORT).show();
        }
    }

    private void extractTextFromImage(Uri fileUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
            String extractedText = ocrProcessor.extractTextFromImage(bitmap);
            inputText.setText(extractedText);
            inputText.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to process image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void summarizeText() {
        String text = inputText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter or upload some text first.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        TextSummarizer.summarizeTextAsync(text, new TextSummarizer.SummarizationCallback() {
            @Override
            public void onSummarizationComplete(String result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    inputText.setText(result);
                    Toast.makeText(QuizGeneratorActivity.this, "Text summarized successfully.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void generateQuizFromText() {
        String text = inputText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter or upload some text first.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        try {
            JSONObject json = new JSONObject();
            json.put("text", text);

            ApiClient.post("/generate_quiz", json.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONArray questionsJson = new JSONArray(response);
                            generatedQuestions.clear();
                            answerInputs.clear();

                            for (int i = 0; i < questionsJson.length(); i++) {
                                JSONObject item = questionsJson.getJSONObject(i);
                                String q = item.getString("question");
                                String a = item.getString("answer");
                                generatedQuestions.add(new WhQuestion(q, a));
                            }

                            displayQuestions();
                        } catch (Exception e) {
                            Log.e("QUIZ_GENERATOR", "Parsing error", e);
                            Toast.makeText(QuizGeneratorActivity.this, "Invalid quiz format.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(QuizGeneratorActivity.this, "Server error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Unexpected error occurred.", Toast.LENGTH_SHORT).show();
            Log.e("QUIZ_GENERATOR", "Request error", e);
        }
    }


    private void displayQuestions() {
        questionContainer.removeAllViews();
        answerInputs.clear();

        if (generatedQuestions == null || generatedQuestions.isEmpty()) {
            TextView noQuestions = new TextView(this);
            noQuestions.setText("⚠️ No questions generated.");
            noQuestions.setTextSize(18);
            questionContainer.addView(noQuestions);
            return;
        }

        for (QuestionBank q : generatedQuestions) {
            String questionText = q.formatForDisplay();  // Use your existing method
            if (questionText == null || questionText.trim().isEmpty()) {
                questionText = "Q: [Missing question text]";
            }

            // Sanitize formatting just in case
            questionText = questionText.trim();
            if (!questionText.endsWith("?")) questionText += "?";
            questionText = questionText.substring(0, 1).toUpperCase() + questionText.substring(1);

            TextView questionView = new TextView(this);
            questionView.setText(questionText);
            questionView.setTextSize(18);
            questionView.setPadding(16, 24, 16, 8);

            EditText answerInput = new EditText(this);
            answerInput.setHint("Your answer here");
            answerInputs.add(answerInput);

            questionContainer.addView(questionView);
            questionContainer.addView(answerInput);
        }


        questionContainer.setVisibility(View.VISIBLE);
        submitAnswersButton.setVisibility(View.VISIBLE);
    }



    private void submitAnswers() {
        progressBar.setVisibility(View.VISIBLE);

        JSONArray userAnswers = new JSONArray();
        JSONArray correctAnswers = new JSONArray();

        try {
            for (int i = 0; i < generatedQuestions.size(); i++) {
                String userAnswer = answerInputs.get(i).getText().toString().trim();
                String correctAnswer = generatedQuestions.get(i).getModelAnswer();
                userAnswers.put(userAnswer);
                correctAnswers.put(correctAnswer);
            }

            JSONObject json = new JSONObject();
            json.put("user_answers", userAnswers);
            json.put("correct_answers", correctAnswers);

            ApiClient.post("/check_answers", json.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        try {
                            JSONArray results = new JSONArray(response);
                            int correctCount = 0;
                            int total = results.length();

                            StringBuilder resultSummary = new StringBuilder();

                            for (int i = 0; i < total; i++) {
                                JSONObject res = results.getJSONObject(i);
                                boolean isCorrect = res.getBoolean("is_correct");
                                double similarity = res.getDouble("similarity");

                                resultSummary.append(i + 1).append(". ")
                                        .append(isCorrect ? "✅ Correct" : "❌ Incorrect")
                                        .append(" (Similarity: ").append(String.format("%.2f", similarity)).append(")\n\n");

                                if (isCorrect) correctCount++;
                            }

                            int percentage = (int) (((double) correctCount / total) * 100);
                            resultSummary.insert(0, "Score: " + percentage + "%\n\n");

                            showResultDialog(resultSummary.toString());

                        } catch (Exception e) {
                            Toast.makeText(QuizGeneratorActivity.this, "Error parsing results.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(QuizGeneratorActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error preparing answers.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResultDialog(String resultSummary) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Quiz Results")
                .setMessage(resultSummary)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
