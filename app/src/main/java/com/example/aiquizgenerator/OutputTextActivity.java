package com.example.aiquizgenerator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class OutputTextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_output_text);

        // Get the output text from the intent
        String outputText = getIntent().getStringExtra("outputText");

        // Set the text in the TextView
        TextView outputTextView = findViewById(R.id.outputTextView);
        outputTextView.setText(outputText);

        // Download Button
        Button downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(v -> {
            if (outputText != null && !outputText.trim().isEmpty()) {
                saveTextAsFile(outputText, "ExtractedText.txt");
            } else {
                Toast.makeText(this, "No text to save!", Toast.LENGTH_SHORT).show();
            }
        });

        // Copy to Clipboard Button
        Button copyButton = findViewById(R.id.copyButton);
        copyButton.setOnClickListener(v -> {
            if (outputText != null && !outputText.trim().isEmpty()) {
                copyToClipboard(outputText);
            } else {
                Toast.makeText(this, "No text to copy!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTextAsFile(String text, String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(text.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "File saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Extracted Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}