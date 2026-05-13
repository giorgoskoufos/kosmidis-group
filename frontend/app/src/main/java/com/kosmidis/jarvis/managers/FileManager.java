package com.kosmidis.jarvis.managers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static final int PICK_DOCUMENT_REQUEST = 2001;

    private final Activity activity;

    private final List<Uri> selectedFileUris = new ArrayList<>();

    public FileManager(Activity activity) {
        this.activity = activity;
    }

    public void openDocumentPicker() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("*/*");

        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        String[] mimeTypes = {
                "application/pdf",
                "text/plain",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };

        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        activity.startActivityForResult(
                Intent.createChooser(intent, "Select Documents"),
                PICK_DOCUMENT_REQUEST
        );
    }

    public void handlePickerResult(Intent data) {

        if (data == null) return;

        if (data.getClipData() != null) {

            int count = data.getClipData().getItemCount();

            for (int i = 0; i < count; i++) {

                if (selectedFileUris.size() >= 2) break;

                Uri uri = data.getClipData().getItemAt(i).getUri();

                if (!selectedFileUris.contains(uri)) {
                    selectedFileUris.add(uri);
                }
            }

        } else if (data.getData() != null) {

            if (selectedFileUris.size() < 2) {

                Uri uri = data.getData();

                if (!selectedFileUris.contains(uri)) {
                    selectedFileUris.add(uri);
                }
            }
        }
    }

    public List<Uri> getSelectedFileUris() {
        return selectedFileUris;
    }

    public boolean hasSelectedFiles() {
        return !selectedFileUris.isEmpty();
    }

    public void clearFiles() {
        selectedFileUris.clear();
    }
}