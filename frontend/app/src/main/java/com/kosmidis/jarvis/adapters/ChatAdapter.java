package com.kosmidis.jarvis.adapters;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.kosmidis.jarvis.R;
import com.kosmidis.jarvis.models.MessageModel;

import java.util.List;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<MessageModel> messages;
    private final Markwon markwon;

    public ChatAdapter(List<MessageModel> messages, Markwon markwon) {
        this.messages = messages;
        this.markwon = markwon;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        MessageModel model = messages.get(position);

        if (model.isTyping) {

            holder.messageText.setVisibility(View.GONE);

            holder.messageImagesScroll.setVisibility(View.GONE);
            holder.messageImagesContainer.removeAllViews();

            holder.messageFilesContainer.setVisibility(View.GONE);
            holder.messageFilesContainer.removeAllViews();

            holder.typingLayout.setVisibility(View.VISIBLE);
            holder.typingLayout.setBackgroundResource(R.drawable.bubble_ai);

            LinearLayout.LayoutParams typingParams =
                    (LinearLayout.LayoutParams) holder.typingLayout.getLayoutParams();

            typingParams.gravity = Gravity.START;
            typingParams.setMargins(0, 0, 64, 0);

            holder.typingLayout.setLayoutParams(typingParams);

            animateDot(holder.dot1, 0);
            animateDot(holder.dot2, 150);
            animateDot(holder.dot3, 300);

            return;
        }

        holder.messageText.setVisibility(View.VISIBLE);
        holder.typingLayout.setVisibility(View.GONE);

        holder.dot1.clearAnimation();
        holder.dot2.clearAnimation();
        holder.dot3.clearAnimation();

        holder.messageImagesContainer.removeAllViews();
        holder.messageFilesContainer.removeAllViews();

        if (model.fileNames != null && !model.fileNames.isEmpty()) {
            holder.messageFilesContainer.setVisibility(View.VISIBLE);

            for (String fileName : model.fileNames) {
                TextView fileChip = new TextView(holder.itemView.getContext());
                fileChip.setText("📄 " + fileName);
                fileChip.setTextColor(android.graphics.Color.WHITE);
                fileChip.setTextSize(14);
                fileChip.setPadding(
                        dpToPx(holder.itemView, 14),
                        dpToPx(holder.itemView, 10),
                        dpToPx(holder.itemView, 14),
                        dpToPx(holder.itemView, 10)
                );
                fileChip.setBackgroundResource(R.drawable.bg_file_chip);

                LinearLayout.LayoutParams fileParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                fileParams.setMargins(0, 0, 0, dpToPx(holder.itemView, 6));

                fileChip.setLayoutParams(fileParams);
                holder.messageFilesContainer.addView(fileChip);
            }
        } else {
            holder.messageFilesContainer.setVisibility(View.GONE);
        }

        if (model.imageUris != null && !model.imageUris.isEmpty()) {

            holder.messageImagesScroll.setVisibility(View.VISIBLE);

            for (Uri uri : model.imageUris) {

                ShapeableImageView imageView =
                        new ShapeableImageView(holder.itemView.getContext());

                LinearLayout.LayoutParams imageParams =
                        new LinearLayout.LayoutParams(
                                dpToPx(holder.itemView, 150),
                                dpToPx(holder.itemView, 150)
                        );

                imageParams.setMargins(
                        0,
                        0,
                        dpToPx(holder.itemView, 8),
                        0
                );

                imageView.setLayoutParams(imageParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageURI(uri);

                imageView.setShapeAppearanceModel(
                        imageView.getShapeAppearanceModel()
                                .toBuilder()
                                .setAllCornerSizes(dpToPx(holder.itemView, 18))
                                .build()
                );

                holder.messageImagesContainer.addView(imageView);
            }

        } else {

            holder.messageImagesScroll.setVisibility(View.GONE);
        }

        markwon.setMarkdown(holder.messageText, model.message);

        LinearLayout.LayoutParams textParams =
                (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();

        LinearLayout.LayoutParams scrollParams =
                (LinearLayout.LayoutParams) holder.messageImagesScroll.getLayoutParams();

        LinearLayout.LayoutParams fileContainerParams =
                (LinearLayout.LayoutParams) holder.messageFilesContainer.getLayoutParams();

        if (model.isUser) {

            textParams.gravity = Gravity.END;
            textParams.setMargins(64, 0, 0, 0);

            holder.messageText.setBackgroundResource(R.drawable.bubble_user);

            scrollParams.gravity = Gravity.END;
            scrollParams.setMargins(64, 0, 0, 12);

            fileContainerParams.gravity = Gravity.END;
            fileContainerParams.setMargins(64, 0, 0, 8);

        } else {

            textParams.gravity = Gravity.START;
            textParams.setMargins(0, 0, 64, 0);

            holder.messageText.setBackgroundResource(R.drawable.bubble_ai);

            scrollParams.gravity = Gravity.START;
            scrollParams.setMargins(0, 0, 64, 12);

            fileContainerParams.gravity = Gravity.START;
            fileContainerParams.setMargins(0, 0, 64, 8);
        }

        holder.messageText.setLayoutParams(textParams);
        holder.messageImagesScroll.setLayoutParams(scrollParams);
        holder.messageFilesContainer.setLayoutParams(fileContainerParams);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private int dpToPx(View view, int dp) {
        return Math.round(
                dp * view.getContext()
                        .getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void animateDot(View dot, long delay) {

        ObjectAnimator animator =
                ObjectAnimator.ofFloat(
                        dot,
                        "translationY",
                        0f,
                        -12f,
                        0f
                );

        animator.setDuration(600);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setStartDelay(delay);
        animator.start();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;

        HorizontalScrollView messageImagesScroll;

        LinearLayout messageImagesContainer;

        LinearLayout messageFilesContainer;

        LinearLayout typingLayout;

        View dot1, dot2, dot3;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.messageText);

            messageImagesScroll =
                    itemView.findViewById(R.id.messageImagesScroll);

            messageImagesContainer =
                    itemView.findViewById(R.id.messageImagesContainer);

            messageFilesContainer =
                    itemView.findViewById(R.id.messageFilesContainer);

            typingLayout =
                    itemView.findViewById(R.id.typingLayout);

            dot1 = itemView.findViewById(R.id.dot1);
            dot2 = itemView.findViewById(R.id.dot2);
            dot3 = itemView.findViewById(R.id.dot3);
        }
    }
}