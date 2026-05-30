package com.example.flashcardapp.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcardapp.data.model.CardSetDto;
import com.example.flashcardapp.databinding.ItemCardSetBinding;
import java.util.HashMap;
import java.util.Map;

public class CardSetAdapter extends ListAdapter<CardSetDto, CardSetAdapter.VH> {
    public interface OnSetClick { void onClick(CardSetDto set); }

    private final OnSetClick onSetClick, onStudyClick, onQuizClick, onGameClick;
    private final Map<String, Integer> progressMap = new HashMap<>();
    private final Map<String, Integer> totalMap = new HashMap<>();

    public CardSetAdapter(OnSetClick onSetClick, OnSetClick onStudyClick,
                          OnSetClick onQuizClick, OnSetClick onGameClick) {
        super(DIFF);
        this.onSetClick = onSetClick;
        this.onStudyClick = onStudyClick;
        this.onQuizClick = onQuizClick;
        this.onGameClick = onGameClick;
    }

    public void updateProgress(Map<String, Integer> map) {
        progressMap.clear(); progressMap.putAll(map); notifyDataSetChanged();
    }

    public void updateTotals(Map<String, Integer> map) {
        totalMap.clear(); totalMap.putAll(map); notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemCardSetBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(getItem(pos)); }

    class VH extends RecyclerView.ViewHolder {
        private final ItemCardSetBinding b;
        VH(ItemCardSetBinding b) { super(b.getRoot()); this.b = b; }

        void bind(CardSetDto set) {
            b.tvEmoji.setText(set.emoji);
            b.tvTitle.setText(set.title);
            b.tvTitleVi.setText(set.titleVi);
            int known = progressMap.containsKey(set.id) ? progressMap.get(set.id) : 0;
            int total = totalMap.containsKey(set.id) ? totalMap.get(set.id) : 0;
            b.tvCardCount.setText(total > 0 ? total + " thẻ" : "-- thẻ");
            b.tvProgressText.setText("Đã thuộc: " + known + (total > 0 ? "/" + total : ""));
            int pct = total > 0 ? (known * 100 / total) : 0;
            b.tvPct.setText(pct + "%");
            b.progressBar.setProgress(pct);

            String[] colors = set.gradient.split(",");
            try {
                GradientDrawable grad = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{Color.parseColor(colors[0]),
                              Color.parseColor(colors.length > 1 ? colors[1] : colors[0])});
                grad.setCornerRadius(24f);
                b.headerLayout.setBackground(grad);
            } catch (Exception ignored) {}

            b.getRoot().setOnClickListener(v -> onSetClick.onClick(set));
            b.btnStudy.setOnClickListener(v -> onStudyClick.onClick(set));
            b.btnQuiz.setOnClickListener(v -> onQuizClick.onClick(set));
            b.btnGame.setOnClickListener(v -> onGameClick.onClick(set));
        }
    }

    private static final DiffUtil.ItemCallback<CardSetDto> DIFF = new DiffUtil.ItemCallback<CardSetDto>() {
        @Override public boolean areItemsTheSame(@NonNull CardSetDto a, @NonNull CardSetDto b) { return a.id.equals(b.id); }
        @Override public boolean areContentsTheSame(@NonNull CardSetDto a, @NonNull CardSetDto b) {
            return a.id.equals(b.id) && a.title.equals(b.title);
        }
    };
}
