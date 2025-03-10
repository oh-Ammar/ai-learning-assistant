package com.example.aiquizgenerator;

import android.content.Context;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextSummarizer {

    private static final Logger logger = Logger.getLogger(TextSummarizer.class.getName());
    private static SentenceDetectorME sentenceDetector;
    private static TokenizerME tokenizer;
    static final double summaryPercentage = 0.4;

    // Initialize models
    public static void initializeModels(Context context) {
        try {
            logger.info("Attempting to load sentence model from assets...");

            // Load sentence detection model from assets
            InputStream sentenceModelIn = context.getAssets().open("opennlp-en-ud-ewt-sentence-1.2-2.5.0.bin");
            SentenceModel sentenceModel = new SentenceModel(sentenceModelIn);
            sentenceDetector = new SentenceDetectorME(sentenceModel);

            // Load tokenization model from assets
            InputStream tokenModelIn = context.getAssets().open("opennlp-en-ud-ewt-tokens-1.2-2.5.0.bin");
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            tokenizer = new TokenizerME(tokenModel);

            logger.info("Models successfully loaded.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing models.", e);
        }
    }

    // Tokenize sentences and words for sentence similarity computation
    private static String[] tokenizeText(String text) {
        return tokenizer.tokenize(text);
    }

    // Convert a sentence into a word frequency map
    private static Map<String, Integer> getWordFrequencyMap(String sentence) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        String[] words = tokenizeText(sentence);

        for (String word : words) {
            wordFrequency.put(word.toLowerCase(), wordFrequency.getOrDefault(word.toLowerCase(), 0) + 1);
        }
        return wordFrequency;
    }

    // Summarize text using a word frequency-based approach (extractive summarization)
    public static String summarizeText(String text) {
        // Check if models are initialized
        if (sentenceDetector == null || tokenizer == null) {
            logger.severe("Models are not initialized. Returning early.");
            return "Models are not initialized.";
        }

        if (text == null || text.trim().isEmpty()) {
            logger.warning("Input text is empty or null.");
            return "Input text is empty or null.";
        }

        // Step 1: Split the text into sentences
        String[] sentences = sentenceDetector.sentDetect(text);

        // Step 2: Calculate word frequencies in each sentence
        Map<String, Integer> globalWordFrequency = new HashMap<>();
        List<Map<String, Integer>> sentenceWordFrequencies = new ArrayList<>();
        for (String sentence : sentences) {
            Map<String, Integer> wordFrequency = getWordFrequencyMap(sentence);
            sentenceWordFrequencies.add(wordFrequency);

            // Update global word frequencies
            for (String word : wordFrequency.keySet()) {
                globalWordFrequency.put(word, globalWordFrequency.getOrDefault(word, 0) + wordFrequency.get(word));
            }
        }

        // Step 3: Rank sentences based on the sum of frequencies of words in the sentence
        List<SentenceScore> sentenceScores = new ArrayList<>();
        for (int index = 0; index < sentences.length; index++) {
            final int i = index;  // Make index effectively final for lambda use
            double score = sentenceWordFrequencies.get(i).keySet().stream()
                    .mapToDouble(word -> globalWordFrequency.getOrDefault(word, 0) * sentenceWordFrequencies.get(i).get(word))
                    .sum();
            sentenceScores.add(new SentenceScore(sentences[i], score));
        }

        // Step 4: Sort sentences by their scores
        sentenceScores.sort((a, b) -> Double.compare(b.score, a.score));

        // Step 5: Select the top N sentences for the summary
        StringBuilder summary = new StringBuilder();
        int topSentencesCount = (int) Math.ceil(sentences.length * summaryPercentage);
        for (int i = 0; i < Math.min(topSentencesCount, sentenceScores.size()); i++) {
            summary.append(sentenceScores.get(i).sentence).append("\n");
        }

        return summary.toString().trim();
    }

    // Helper class to store sentences with their calculated scores
    private static class SentenceScore {
        String sentence;
        double score;

        SentenceScore(String sentence, double score) {
            this.sentence = sentence;
            this.score = score;
        }
    }
}
