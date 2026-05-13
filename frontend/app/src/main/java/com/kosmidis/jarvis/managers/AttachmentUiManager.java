package com.kosmidis.jarvis.managers;

import android.app.Dialog;
import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kosmidis.jarvis.R;

import java.util.ArrayList;
import java.util.List;

public class AttachmentUiManager {

    private final Activity activity;
    private final ImageManager imageManager;
    private final FileManager fileManager;
    private final LinearLayout selectedFilesContainer;

    public AttachmentUiManager(
            Activity activity,
            ImageManager imageManager,
            FileManager fileManager,
            LinearLayout selectedFilesContainer
    ) {
        this.activity = activity;
        this.imageManager = imageManager;
        this.fileManager = fileManager;
        this.selectedFilesContainer = selectedFilesContainer;
    }

    public void showAttachmentDialog() {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_attachment_options);

        LinearLayout optionImages = dialog.findViewById(R.id.optionImages);
        LinearLayout optionDocuments = dialog.findViewById(R.id.optionDocuments);

        optionImages.setOnClickListener(v -> {
            dialog.dismiss();
            imageManager.openImagePicker();
        });

        optionDocuments.setOnClickListener(v -> {
            dialog.dismiss();
            fileManager.openDocumentPicker();
        });

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
            params.x = 55;
            params.y = 290;

            window.setAttributes(params);
        }

        dialog.show();
    }

    public void updateSelectedFilesPreview() {
        selectedFilesContainer.removeAllViews();

        if (fileManager == null || !fileManager.hasSelectedFiles()) {
            View scrollParent = (View) selectedFilesContainer.getParent();
            scrollParent.setVisibility(View.GONE);
            return;
        }

        View scrollParent = (View) selectedFilesContainer.getParent();
        scrollParent.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(activity);

        for (Uri uri : fileManager.getSelectedFileUris()) {
            View chipView = inflater.inflate(
                    R.layout.item_attachment_chip,
                    selectedFilesContainer,
                    false
            );

            ImageView chipIcon = chipView.findViewById(R.id.chipIcon);
            TextView chipName = chipView.findViewById(R.id.chipFileName);
            ImageView btnRemove = chipView.findViewById(R.id.btnRemoveChip);

            chipName.setText(getFileName(uri));
            chipIcon.setImageResource(R.drawable.article);

            btnRemove.setOnClickListener(v -> {
                fileManager.getSelectedFileUris().remove(uri);
                updateSelectedFilesPreview();
            });

            selectedFilesContainer.addView(chipView);
        }
    }

    public List<String> getSelectedFileNames() {
        List<String> names = new ArrayList<>();

        if (fileManager == null || !fileManager.hasSelectedFiles()) {
            return names;
        }

        for (Uri uri : fileManager.getSelectedFileUris()) {
            names.add(getFileName(uri));
        }

        return names;
    }

    private String getFileName(Uri uri) {
        String result = "document";

        android.database.Cursor cursor =
                activity.getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            try {
                int nameIndex = cursor.getColumnIndex(
                        android.provider.OpenableColumns.DISPLAY_NAME
                );

                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    result = cursor.getString(nameIndex);
                }
            } finally {
                cursor.close();
            }
        }

        return result;
    }
}