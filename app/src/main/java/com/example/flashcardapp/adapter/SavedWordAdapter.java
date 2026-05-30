package com.example.flashcardapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcardapp.data.entity.SavedWord;
import com.example.flashcardapp.databinding.ItemSavedWordBinding;

public class SavedWordAdapter extends ListAdapter<SavedWord, SavedWordAdapter.VH> {
    public interface OnSpeak { void speak(String word); }
    public interface OnAddFolder { void add(SavedWord word); }
    public interface OnDelete { void delete(String id); }

    private final OnSpeak onSpeak;
    private final OnAddFolder onAddFolder;
    private final OnDelete onDelete;
    private String expandedId = null;

    public SavedWordAdapter(OnSpeak onSpeak, OnAddFolder onAddFolder, OnDelete onDelete) {
        super(DIFF);
        this.onSpeak = onSpeak;
        this.onAddFolder = onAddFolder;
        this.onDelete = onDelete;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSavedWordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(getItem(pos)); }

    class VH extends RecyclerView.ViewHolder {
        private final ItemSavedWordBinding b;
        VH(ItemSavedWordBinding b) { super(b.getRoot()); this.b = b; }

        void bind(SavedWord w) {
            b.tvWord.setText(w.word);
            b.tvPhonetic.setText(w.phonetic != null ? w.phonetic : "");
            b.tvPos.setText(w.partOfSpeech != null ? w.partOfSpeech : "");
            b.tvPos.setVisibility(w.partOfSpeech != null && !w.partOfSpeech.isEmpty() ? View.VISIBLE : View.GONE);

            boolean expanded = w.id.equals(expandedId);
            if (!expanded || w.definition == null || w.definition.isEmpty()) {
                b.tvDefinition.setVisibility(View.GONE);
                b.tvExample.setVisibility(View.GONE);
            } else {
                b.tvDefinition.setText(w.definition);
                b.tvDefinition.setVisibility(View.VISIBLE);
                if (w.example != null && !w.example.isEmpty()) {
                    b.tvExample.setText("\"" + w.example + "\"");
                    b.tvExample.setVisibility(View.VISIBLE);
                } else {
                    b.tvExample.setVisibility(View.GONE);
                }
            }

            b.getRoot().setOnClickListener(v -> {
                expandedId = expanded ? null : w.id;
                notifyDataSetChanged();
            });
            b.btnSpeak.setOnClickListener(v -> onSpeak.speak(w.word));
            b.btnAddFolder.setOnClickListener(v -> onAddFolder.add(w));
            b.btnDelete.setOnClickListener(v -> onDelete.delete(w.id));
        }
    }

    private static final DiffUtil.ItemCallback<SavedWord> DIFF = new DiffUtil.ItemCallback<SavedWord>() {
        @Override public boolean areItemsTheSame(@NonNull SavedWord a, @NonNull SavedWord b) { return a.id.equals(b.id); }
        @Override public boolean areContentsTheSame(@NonNull SavedWord a, @NonNull SavedWord b) { return a.id.equals(b.id) && a.word.equals(b.word); }
    };
}
