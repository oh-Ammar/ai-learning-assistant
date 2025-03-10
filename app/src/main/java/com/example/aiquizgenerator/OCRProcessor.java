package com.example.aiquizgenerator;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

public class OCRProcessor {

    private TessBaseAPI tessBaseAPI;

    // Constructor to initialize Tesseract API
    public OCRProcessor() {
        tessBaseAPI = new TessBaseAPI();
    }

    public String extractTextFromImage(Bitmap bitmap) {
        String extractedText = "";

        try {
            // Path to tessdata directory
            String dataPath = "/data/data/com.example.aiquizgenerator/files/tesseract/";

            // Initialize Tesseract with English language
            tessBaseAPI.init(dataPath, "eng");

            // Set the image for OCR processing
            tessBaseAPI.setImage(bitmap);

            // Extract text
            extractedText = tessBaseAPI.getUTF8Text();

            // End the OCR process
            tessBaseAPI.end();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return extractedText.trim();
    }
}
