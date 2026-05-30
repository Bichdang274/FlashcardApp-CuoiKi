package com.example.flashcardapp.ui.game;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcardapp.R;
import com.example.flashcardapp.databinding.ItemMatchingTileBinding;
import com.example.flashcardapp.viewmodel.MatchingTile;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileAdapter extends RecyclerView.Adapter<TileAdapter.VH> {
    public interface OnTileClick { void onClick(String tileId); }

    private List<MatchingTile> tiles = Collections.emptyList();
    private Set<String> matched = new HashSet<>();
    private String selectedId = null;
    private List<String> wrongIds = Collections.emptyList();
    private final OnTileClick onClick;

    public TileAdapter(OnTileClick onClick) { this.onClick = onClick; }

    public void submitList(List<MatchingTile> newTiles) { tiles = newTiles; notifyDataSetChanged(); }
    public void updateMatched(Set<String> m) { matched = m; notifyDataSetChanged(); }
    public void updateSelected(String id) { selectedId = id; notifyDataSetChanged(); }
    public void updateWrong(List<String> ids) { wrongIds = ids; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemMatchingTileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(tiles.get(pos)); }

    @Override
    public int getItemCount() { return tiles.size(); }

    class VH extends RecyclerView.ViewHolder {
        private final ItemMatchingTileBinding b;
        VH(ItemMatchingTileBinding b) { super(b.getRoot()); this.b = b; }

        void bind(MatchingTile tile) {
            boolean isMatched = matched.contains(tile.cardId);
            boolean isSelected = tile.id.equals(selectedId);
            boolean isWrong = wrongIds.contains(tile.id);

            if ("english".equals(tile.type) && tile.emoji != null && !isMatched) {
                b.tvTileEmoji.setVisibility(View.VISIBLE);
                b.tvTileEmoji.setText(tile.emoji);
            } else {
                b.tvTileEmoji.setVisibility(View.GONE);
            }

            b.tvTileText.setText(tile.text);
            b.tvTileType.setText("english".equals(tile.type) ? "EN" : "VI");
            b.tvMatchedCheck.setVisibility(isMatched ? View.VISIBLE : View.GONE);
            b.tvTileText.setVisibility(isMatched ? View.GONE : View.VISIBLE);
            b.tvTileType.setVisibility(isMatched ? View.GONE : View.VISIBLE);
            if (isMatched) b.tvTileEmoji.setVisibility(View.GONE);

            if (isMatched) b.tileContainer.setBackgroundResource(R.drawable.bg_tile_matched);
            else if (isWrong) b.tileContainer.setBackgroundResource(R.drawable.bg_tile_wrong);
            else if (isSelected) b.tileContainer.setBackgroundResource(R.drawable.bg_tile_selected);
            else b.tileContainer.setBackgroundResource(R.drawable.bg_tile_default);

            b.tileContainer.setAlpha(isMatched ? 0.6f : 1f);

            b.getRoot().setOnClickListener(v -> { if (!isMatched) onClick.onClick(tile.id); });
        }
    }
}
