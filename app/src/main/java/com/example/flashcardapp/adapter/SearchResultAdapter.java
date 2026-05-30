package com.example.flashcardapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcardapp.data.model.FlashcardDto;
import com.example.flashcardapp.databinding.ItemSearchResultBinding;

public class SearchResultAdapter extends ListAdapter<FlashcardDto, SearchResultAdapter.VH> {

    public SearchResultAdapter() { super(DIFF); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSearchResultBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(getItem(pos)); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemSearchResultBinding b;
        VH(ItemSearchResultBinding b) { super(b.getRoot()); this.b = b; }

        void bind(FlashcardDto c) {
            b.tvEmoji.setText(c.emoji);
            b.tvEnglish.setText(c.english);
            b.tvPhonetic.setText(c.phonetic);
            b.tvVietnamese.setText(c.vietnamese);
            b.tvExample.setText(c.example);
        }
    }

    private static final DiffUtil.ItemCallback<FlashcardDto> DIFF = new DiffUtil.ItemCallback<FlashcardDto>() {
        @Override public boolean areItemsTheSame(@NonNull FlashcardDto a, @NonNull FlashcardDto b) { return a.id.equals(b.id); }
        @Override public boolean areContentsTheSame(@NonNull FlashcardDto a, @NonNull FlashcardDto b) { return a.id.equals(b.id); }
    };
}
