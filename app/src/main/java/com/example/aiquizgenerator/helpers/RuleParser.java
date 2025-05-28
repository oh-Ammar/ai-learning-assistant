package com.example.aiquizgenerator.helpers;

import android.util.Log;
import com.example.aiquizgenerator.questions.WhQuestion;
import com.example.aiquizgenerator.questions.QuestionBank;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class RuleParser {

    public static List<QuestionBank> generateQuestionsFromTokens(JSONArray tokens) {
        List<QuestionBank> questions = new ArrayList<>();

        try {
            for (int i = 0; i < tokens.length() - 2; i++) {
                JSONObject t1 = tokens.getJSONObject(i);
                JSONObject t2 = tokens.getJSONObject(i + 1);
                JSONObject t3 = tokens.getJSONObject(i + 2);

                String word1 = t1.getString("text");
                String pos1 = t1.getString("pos");

                String word2 = t2.getString("text");
                String pos2 = t2.getString("pos");

                String word3 = t3.getString("text");
                String pos3 = t3.getString("pos");

                // Skip if any token looks like junk/code/url
                if (isNoise(word1) || isNoise(word2) || isNoise(word3)) continue;

                // Rule 1: Proper Noun + Verb + Noun  → Who [verb] [noun]?
                if (pos1.equals("PROPN") && pos2.equals("VERB") && pos3.equals("NOUN")) {
                    String q = "Who " + word2 + " " + word3 + "?";
                    questions.add(new WhQuestion(q, word1));
                    continue;
                }

                // Rule 2: Noun + Verb → What does the [noun] [verb]?
                if (pos1.equals("NOUN") && pos2.equals("VERB")) {
                    boolean isPlural = word1.toLowerCase().endsWith("s") && !word1.toLowerCase().endsWith("ss");
                    String helper = isPlural ? "do" : "does";
                    String q = "What " + helper + " the " + word1 + " " + word2 + "?";
                    questions.add(new WhQuestion(q, word1));
                    continue;
                }

                // Rule 3: Verb + Noun → What does it [verb]?
                if (pos1.equals("VERB") && pos2.equals("NOUN")) {
                    String q = "What does it " + word1 + "?";
                    questions.add(new WhQuestion(q, word2));
                    continue;
                }
            }
        } catch (Exception e) {
            Log.e("RuleParser", "Error parsing tokens", e);
        }

        return questions;
    }

    private static boolean isNoise(String word) {
        return word.contains("http")
                || word.matches(".*[<>:;/\\\\|].*")   // escape backslashes
                || word.matches(".*\\d.*")           // escape \d properly
                || word.length() <= 2
                || word.toLowerCase().equals("dim")
                || word.startsWith("\"")             // fix quote
                || word.startsWith("'")
                || word.contains("=")
                || word.contains("@");
    }

}
