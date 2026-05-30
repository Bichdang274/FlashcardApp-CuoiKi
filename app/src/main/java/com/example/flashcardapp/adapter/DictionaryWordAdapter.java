package com.example.flashcardapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcardapp.data.model.FlashcardDto;
import com.example.flashcardapp.databinding.ItemSearchResultBinding;
import java.util.HashSet;
import java.util.Set;

public class DictionaryWordAdapter extends ListAdapter<FlashcardDto, DictionaryWordAdapter.VH> {
    public interface OnToggle { void onToggle(String id); }
    public interface OnSpeak { void onSpeak(String word); }
    public interface OnAddToFolder { void onAdd(FlashcardDto card); }

    private final OnToggle onToggle;
    private final OnSpeak onSpeak;
    private final OnAddToFolder onAddToFolder;
    private Set<String> selectedIds = new HashSet<>();
    private String expandedId = null;

    public DictionaryWordAdapter(OnToggle onToggle, OnSpeak onSpeak, OnAddToFolder onAddToFolder) {
        super(DIFF);
        this.onToggle = onToggle; this.onSpeak = onSpeak; this.onAddToFolder = onAddToFolder;
    }

    public void updateSelected(Set<String> ids) { selectedIds = ids != null ? ids : new HashSet<>(); notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSearchResultBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(getItem(pos)); }

    class VH extends RecyclerView.ViewHolder {
        private final ItemSearchResultBinding b;
        VH(ItemSearchResultBinding b) { super(b.getRoot()); this.b = b; }

        void bind(FlashcardDto card) {
            boolean isSelected = selectedIds.contains(card.id);
            boolean isExpanded = card.id.equals(expandedId);

            b.tvEmoji.setText(card.emoji);
            b.tvEnglish.setText(card.english);
            b.tvPhonetic.setText(card.phonetic);
            b.tvVietnamese.setText(card.vietnamese);
            b.tvExample.setText(card.example);
            b.tvExample.setVisibility(isExpanded && !card.example.isEmpty() ? View.VISIBLE : View.GONE);

            b.getRoot().setBackgroundColor(isSelected ? Color.parseColor("#261454") : Color.TRANSPARENT);

            b.getRoot().setOnClickListener(v -> {
                expandedId = isExpanded ? null : card.id;
                notifyDataSetChanged();
            });
            b.getRoot().setOnLongClickListener(v -> { onToggle.onToggle(card.id); return true; });
            b.tvEmoji.setOnClickListener(v -> onToggle.onToggle(card.id));
            b.btnSpeak.setOnClickListener(v -> onSpeak.onSpeak(card.english));
            b.btnAddToFolder.setOnClickListener(v -> onAddToFolder.onAdd(card));
        }
    }

    private static final DiffUtil.ItemCallback<FlashcardDto> DIFF = new DiffUtil.ItemCallback<FlashcardDto>() {
        @Override public boolean areItemsTheSame(@NonNull FlashcardDto a, @NonNull FlashcardDto b) { return a.id.equals(b.id); }
        @Override public boolean areContentsTheSame(@NonNull FlashcardDto a, @NonNull FlashcardDto b) { return a.id.equals(b.id); }
    };
}
