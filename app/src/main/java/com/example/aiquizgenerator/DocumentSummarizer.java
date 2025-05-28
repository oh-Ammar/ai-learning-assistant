package com.example.aiquizgenerator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aiquizgenerator.helpers.OCRProcessor;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.IOException;
import java.io.InputStream;

public class DocumentSummarizer extends AppCompatActivity {

    private Button uploadDocument, uploadImage;
    private ProgressBar progressBar;
    private TextView progressText;
    private RelativeLayout rootLayout;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_FILE_REQUEST_CODE = 2;
    private OCRProcessor ocrProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);getSupportActionBar().hide();
        setContentView(R.layout.activity_document_summarizer);

        uploadDocument = findViewById(R.id.uploadDocument);
        uploadImage = findViewById(R.id.uploadImage);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        rootLayout = findViewById(R.id.rootLayout);

        ocrProcessor = new OCRProcessor();

        uploadDocument.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
            openDocumentPicker();
        });

        uploadImage.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
            openImagePicker();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                if (requestCode == PICK_FILE_REQUEST_CODE) {
                    handleFileUpload(fileUri);
                } else if (requestCode == PICK_IMAGE_REQUEST) {
                    handleImageUpload(fileUri);
                }
            }
        }
    }

    private void handleFileUpload(Uri fileUri) {
        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        new AsyncTask<Uri, Integer, String>() {
            @Override
            protected String doInBackground(Uri... uris) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uris[0]);
                    String mimeType = getContentResolver().getType(uris[0]);

                    if ("application/pdf".equals(mimeType)) {
                        PDDocument document = PDDocument.load(inputStream);
                        PDFTextStripper pdfStripper = new PDFTextStripper();
                        int totalPages = document.getNumberOfPages();
                        for (int i = 1; i <= totalPages; i++) {
                            Thread.sleep(50);
                            publishProgress((i * 100) / totalPages);
                        }
                        String extractedText = pdfStripper.getText(document);
                        document.close();
                        return extractedText;
                    } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType)) {
                        XWPFDocument document = new XWPFDocument(inputStream);
                        for (int i = 1; i <= 10; i++) {
                            Thread.sleep(100);
                            publishProgress(i * 10);
                        }
                        return new XWPFWordExtractor(document).getText();
                    } else {
                        return "Unsupported file type.";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Failed to process file.";
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                progressBar.setProgress(values[0]);
                progressText.setText(values[0] + "%");
            }

            @Override
            protected void onPostExecute(String result) {
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);

                if (result.equals("Unsupported file type.")) {
                    new AlertDialog.Builder(DocumentSummarizer.this)
                            .setTitle("Error")
                            .setMessage("The selected file type is not supported.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    TextSummarizer.summarizeTextAsync(result, summarizedText -> {
                        Intent intent = new Intent(DocumentSummarizer.this, OutputTextActivity.class);
                        intent.putExtra("outputText", summarizedText);
                        startActivity(intent);
                    });
                }
            }
        }.execute(fileUri);
    }

    private void handleImageUpload(Uri fileUri) {
        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        try {
            Bitmap selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
            new AsyncTask<Bitmap, Integer, String>() {
                @Override
                protected String doInBackground(Bitmap... bitmaps) {
                    for (int i = 1; i <= 100; i += 5) {
                        try {
                            Thread.sleep(50);
                            publishProgress(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return ocrProcessor.extractTextFromImage(bitmaps[0]);
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    progressBar.setProgress(values[0]);
                    progressText.setText(values[0] + "%");
                }

                @Override
                protected void onPostExecute(String result) {
                    progressBar.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                    TextSummarizer.summarizeTextAsync(result, summarizedText -> {
                        Intent intent = new Intent(DocumentSummarizer.this, OutputTextActivity.class);
                        intent.putExtra("outputText", summarizedText);
                        startActivity(intent);
                    });
                }
            }.execute(selectedImageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }
}