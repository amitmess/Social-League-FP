package com.example.social_league_fp.ui.matches;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social_league_fp.R;
import com.example.social_league_fp.model.Match;
import com.example.social_league_fp.model.MatchStatus;

import java.util.ArrayList;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.VH> {

    public interface OnMatchClickListener {
        void onMatchClick(Match match);
    }

    private final ArrayList<Match> items = new ArrayList<>();
    private final OnMatchClickListener listener;

    public MatchesAdapter(OnMatchClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Match> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Match m = items.get(position);

        // Date/Time
        holder.tvDateTime.setText(m.getDateTime());

        // Location
        holder.tvLocation.setText(m.getLocation());

        // Attendance line (כרגע משתמשים ב-title עד שתוסיף confirmed/maybe למודל)
        // אם תרצה 1:1 ל-Lovable נוסיף ל-Match שדות confirmedCount/maybeCount ואז נחליף את השורה הזו.
        holder.tvAttendance.setText(m.getAttendanceText());

        // Status pill + Score box
        if (m.getStatus() == MatchStatus.PLAYED) {
            holder.tvPillStatus.setText("Finished");
            holder.tvPillStatus.setBackgroundResource(R.drawable.bg_pill_finished);
            holder.tvPillStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.textSecondary)
            );

            holder.scoreBox.setVisibility(View.VISIBLE);
            holder.tvScoreBig.setText(m.getScoreText()); // למשל "3 - 2"
        } else {
            holder.tvPillStatus.setText("Upcoming");
            holder.tvPillStatus.setBackgroundResource(R.drawable.bg_pill_upcoming);
            holder.tvPillStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary)
            );

            holder.scoreBox.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMatchClick(m);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDateTime, tvPillStatus, tvLocation, tvAttendance, tvScoreBig;
        LinearLayout scoreBox;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvPillStatus = itemView.findViewById(R.id.tvPillStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvAttendance = itemView.findViewById(R.id.tvAttendance);

            scoreBox = itemView.findViewById(R.id.scoreBox);
            tvScoreBig = itemView.findViewById(R.id.tvScoreBig);
        }
    }
}