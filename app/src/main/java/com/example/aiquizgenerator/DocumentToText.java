package com.example.aiquizgenerator;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;

import com.googlecode.tesseract.android.TessBaseAPI;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import android.widget.ProgressBar;

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
import androidx.appcompat.app.AppCompatActivity;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.InputStream;

public class DocumentToText extends AppCompatActivity {
    private Button uploadDocument, uploadImage;
    private ProgressBar progressBar;
    private TextView progressText;
    private RelativeLayout rootLayout;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_FILE_REQUEST_CODE = 2;

    private OCRProcessor ocrProcessor; // Utilize the provided OCRProcessor class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PDFBoxResourceLoader.init(getApplicationContext());
        getSupportActionBar().hide();
        setContentView(R.layout.activity_document_to_text);

        // Initialize UI components
        uploadDocument = findViewById(R.id.uploadDocument);
        uploadImage = findViewById(R.id.uploadImage);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText); // Add progress text
        rootLayout = findViewById(R.id.rootLayout);

        // Initialize the OCRProcessor
        ocrProcessor = new OCRProcessor();

        // Button click listeners
        uploadDocument.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
            openDocumentPicker();
        });

        uploadImage.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
            openImagePicker();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
                            Thread.sleep(50); // Simulate processing time
                            int progress = (i * 100) / totalPages;
                            publishProgress(progress);
                        }

                        String extractedText = pdfStripper.getText(document);
                        document.close();
                        return extractedText;
                    } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType)) {
                        XWPFDocument document = new XWPFDocument(inputStream);

                        for (int i = 1; i <= 10; i++) {
                            Thread.sleep(100); // Simulate processing
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

                Intent intent = new Intent(DocumentToText.this, OutputTextActivity.class);
                intent.putExtra("outputText", result);
                startActivity(intent);
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
                            Thread.sleep(50); // Simulate processing time
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

                    Intent intent = new Intent(DocumentToText.this, OutputTextActivity.class);
                    intent.putExtra("outputText", result);
                    startActivity(intent);
                }
            }.execute(selectedImageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }
}
