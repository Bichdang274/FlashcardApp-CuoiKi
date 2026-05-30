package com.example.flashcardapp.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcardapp.data.model.FolderDto;
import com.example.flashcardapp.databinding.ItemFolderBinding;

public class FolderAdapter extends ListAdapter<FolderDto, FolderAdapter.VH> {
    public interface OnFolderAction { void onAction(FolderDto folder); }

    private final OnFolderAction onStudy, onQuiz, onGame;
    private final OnFolderAction onDelete, onLongClick;

    public FolderAdapter(OnFolderAction onStudy, OnFolderAction onQuiz, OnFolderAction onGame,
                         OnFolderAction onDelete, OnFolderAction onLongClick) {
        super(DIFF);
        this.onStudy = onStudy; this.onQuiz = onQuiz; this.onGame = onGame;
        this.onDelete = onDelete; this.onLongClick = onLongClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemFolderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(getItem(pos)); }

    class VH extends RecyclerView.ViewHolder {
        private final ItemFolderBinding b;
        VH(ItemFolderBinding b) { super(b.getRoot()); this.b = b; }

        void bind(FolderDto f) {
            b.tvEmoji.setText(f.emoji);
            b.tvName.setText(f.name);
            b.tvWordCount.setText(f.wordIds.size() + " từ");
            try {
                String[] colors = f.color.split(",");
                GradientDrawable grad = new GradientDrawable(GradientDrawable.Orientation.BR_TL,
                    new int[]{Color.parseColor(colors[0]),
                              Color.parseColor(colors.length > 1 ? colors[1] : colors[0])});
                grad.setCornerRadius(20f);
                b.getRoot().setBackground(grad);
            } catch (Exception ignored) {}
            b.btnStudy.setOnClickListener(v -> onStudy.onAction(f));
            b.btnQuiz.setOnClickListener(v -> onQuiz.onAction(f));
            b.btnGame.setOnClickListener(v -> onGame.onAction(f));
            b.btnDelete.setOnClickListener(v -> { if (onDelete != null) onDelete.onAction(f); });
            b.getRoot().setOnLongClickListener(v -> { if (onLongClick != null) onLongClick.onAction(f); return onLongClick != null; });
        }
    }

    private static final DiffUtil.ItemCallback<FolderDto> DIFF = new DiffUtil.ItemCallback<FolderDto>() {
        @Override public boolean areItemsTheSame(@NonNull FolderDto a, @NonNull FolderDto b) {
            return a.id != null && a.id.equals(b.id);
        }
        @Override public boolean areContentsTheSame(@NonNull FolderDto a, @NonNull FolderDto b) {
            return a.id != null && a.id.equals(b.id) && a.name.equals(b.name);
        }
    };
}
