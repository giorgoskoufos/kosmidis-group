package com.kosmidis.jarvis.managers;

import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.kosmidis.jarvis.R;

import java.util.ArrayList;
import java.util.List;

public class ImageManager {

    private final List<Uri> selectedImageUris = new ArrayList<>();

    private final LinearLayout imageContainer;
    private final LinearLayout previewListLayout;
    private final ImageButton clearImageButton;

    private final ActivityResultLauncher<String> imagePickerLauncher;

    private static final int MAX_IMAGES = 5;

    public ImageManager(
            AppCompatActivity activity,
            LinearLayout imageContainer,
            LinearLayout previewListLayout,
            ImageButton clearImageButton
    ) {
        this.imageContainer = imageContainer;
        this.previewListLayout = previewListLayout;
        this.clearImageButton = clearImageButton;

        imagePickerLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        int remainingSlots = MAX_IMAGES - selectedImageUris.size();

                        if (remainingSlots <= 0) {
                            return;
                        }

                        int count = Math.min(uris.size(), remainingSlots);

                        for (int i = 0; i < count; i++) {
                            Uri uri = uris.get(i);

                            if (!selectedImageUris.contains(uri)) {
                                selectedImageUris.add(uri);
                                addPreviewImage(activity, uri);
                            }
                        }

                        imageContainer.setVisibility(View.VISIBLE);
                    }
                }
        );

        clearImageButton.setOnClickListener(v -> clearSelectedImages());
    }

    private void addPreviewImage(AppCompatActivity activity, Uri uri) {
        ImageView imageView = new ImageView(activity);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(activity, 88),
                dpToPx(activity, 88)
        );

        params.setMargins(0, 0, dpToPx(activity, 8), 0);

        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(uri);
        imageView.setBackgroundResource(R.drawable.bg_image_preview);

        previewListLayout.addView(imageView);
    }

    private int dpToPx(AppCompatActivity activity, int dp) {
        return Math.round(dp * activity.getResources().getDisplayMetrics().density);
    }

    public void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    public List<Uri> getSelectedImageUris() {
        return new ArrayList<>(selectedImageUris);
    }

    public Uri getSelectedImageUri() {
        if (selectedImageUris.isEmpty()) return null;
        return selectedImageUris.get(0);
    }

    public boolean hasSelectedImage() {
        return !selectedImageUris.isEmpty();
    }

    public void clearSelectedImages() {
        selectedImageUris.clear();
        previewListLayout.removeAllViews();
        imageContainer.setVisibility(View.GONE);
    }

    public void clearSelectedImage() {
        clearSelectedImages();
    }
}