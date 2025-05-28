package com.example.aiquizgenerator;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.*;

public class TextSummarizer {

    private static final String TAG = "TextSummarizer";
    private static final String HF_API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";
    private static final String HF_API_KEY = "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";

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public interface SummarizationCallback {
        void onSummarizationComplete(String result);
    }

    public static void summarizeTextAsync(String text, SummarizationCallback callback) {
        new ChunkedSummarizationTask(callback).execute(text);
    }

    private static class ChunkedSummarizationTask extends AsyncTask<String, Void, String> {
        private static final int CHUNK_SIZE = 1000;
        private SummarizationCallback callback;

        ChunkedSummarizationTask(SummarizationCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... texts) {
            String fullText = texts[0];
            if (fullText == null || fullText.trim().isEmpty()) {
                return "Input text is empty or null.";
            }

            List<String> chunks = new ArrayList<>();
            for (int i = 0; i < fullText.length(); i += CHUNK_SIZE) {
                int end = Math.min(i + CHUNK_SIZE, fullText.length());
                chunks.add(fullText.substring(i, end));
            }

            StringBuilder combinedSummary = new StringBuilder();

            for (String chunk : chunks) {
                String chunkSummary = summarizeChunk(chunk);
                combinedSummary.append(cleanChunk(chunkSummary)).append("\n\n");
            }

            return applyPostProcessing(combinedSummary.toString().trim());
        }

        private String summarizeChunk(String chunk) {
            try {
                String json = "{" +
                        "\"inputs\":\"" + escapeJsonString(chunk) + "\"," +
                        "\"parameters\": {\"min_length\": 50}" +
                        "}";

                Request request = new Request.Builder()
                        .url(HF_API_URL)
                        .addHeader("Authorization", "Bearer " + HF_API_KEY)
                        .post(RequestBody.create(json, MediaType.parse("application/json")))
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().string();
                    return parseBartSummary(result);
                } else {
                    Log.e(TAG, "Chunk summarization failed: " + response.code());
                    return "[Error summarizing chunk]";
                }
            } catch (IOException e) {
                Log.e(TAG, "Error summarizing chunk.", e);
                return "[Exception occurred]";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (callback != null) {
                callback.onSummarizationComplete(result);
            }
        }

        private String cleanChunk(String chunk) {
            return chunk.replaceAll("(?i)for confidential support call.*", "")
                    .replaceAll("(?i)visit a local Samaritans.*", "")
                    .replaceAll("http[s]?://\\S+", "[link]")
                    .replaceAll("\\n{2,}", "\n");
        }


        private String applyPostProcessing(String text) {
            String[] lines = text.split("\\n");
            StringBuilder result = new StringBuilder();

            String lastHeading = "";
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                // Automatically detect likely headings based on formatting (e.g., capitalized short lines)
                if (trimmed.length() < 60 && trimmed.equals(trimmed.toUpperCase()) && !trimmed.equals(lastHeading)) {
                    result.append("\n\n== ").append(trimmed).append(" ==\n");
                    lastHeading = trimmed;
                }

                result.append(trimmed).append("\n");
            }
            return result.toString().trim();
        }
    }

        private static String parseBartSummary(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            if (jsonArray.length() > 0) {
                JSONObject firstItem = jsonArray.getJSONObject(0);
                if (firstItem.has("summary_text")) {
                    return firstItem.getString("summary_text");
                }
            }
            return "[No summary generated]";
        } catch (Exception e) {
            Log.e(TAG, "Error parsing BART summary response.", e);
            return "[Error parsing summary]";
        }
    }

    private static String escapeJsonString(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
