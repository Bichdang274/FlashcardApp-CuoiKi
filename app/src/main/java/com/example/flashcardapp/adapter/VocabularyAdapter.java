package com.example.flashcardapp.adapter;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcardapp.R;
import com.example.flashcardapp.data.model.FlashcardDto;
import java.util.ArrayList;
import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VH> {
    private List<FlashcardDto> items = new ArrayList<>();
    private TextToSpeech tts;

    public VocabularyAdapter(TextToSpeech tts) { this.tts = tts; }

    public void setItems(List<FlashcardDto> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_vocabulary_word, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        FlashcardDto word = items.get(pos);
        h.tvEmoji.setText(word.emoji != null && !word.emoji.isEmpty() ? word.emoji : "📝");
        h.tvEnglish.setText(word.english);
        h.tvVietnamese.setText(word.vietnamese);
        h.btnSound.setOnClickListener(v -> {
            if (tts != null) tts.speak(word.english, TextToSpeech.QUEUE_FLUSH, null, null);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvEnglish, tvVietnamese;
        LinearLayout btnSound;
        VH(View v) {
            super(v);
            tvEmoji = v.findViewById(R.id.tvEmoji);
            tvEnglish = v.findViewById(R.id.tvEnglish);
            tvVietnamese = v.findViewById(R.id.tvVietnamese);
            btnSound = v.findViewById(R.id.btnSound);
        }
    }
}
