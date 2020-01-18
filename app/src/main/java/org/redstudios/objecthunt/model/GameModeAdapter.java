package org.redstudios.objecthunt.model;

import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.redstudios.objecthunt.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GameModeAdapter extends RecyclerView.Adapter<GameModeAdapter.GameModeHolder> {

    private ArrayList<GameMode> gameModes;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Integer position);
    }

    @NonNull
    @Override
    public GameModeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_mode_item_list, parent, false);
        GameModeHolder holder = new GameModeHolder(view);
        return holder;
    }

    public GameModeAdapter(ArrayList<GameMode> gameModeArrayList, OnItemClickListener listener) {
        gameModes = gameModeArrayList;
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull GameModeHolder holder, int position) {
        GameMode currentGameMode = gameModes.get(position);
        holder.gameModeName.setText(currentGameMode.getGameModeName());
        holder.gameModeDescription.setText(currentGameMode.getDescription());
        holder.gameModeDescription.setLines(1 + currentGameMode.getDescription().length() / 50);
        holder.btn.setOnClickListener(new View.OnClickListener() {
            private Boolean isOpened = false;
            private int newHeight = -1;

            @Override
            public void onClick(View v) {
                RelativeLayout parent = (RelativeLayout) v.getParent().getParent();
                LinearLayout expandableView;
                expandableView = parent.findViewById(R.id.expandable);
                ValueAnimator slideAnimator;
                expandableView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                newHeight = expandableView.getMeasuredHeight();

                if (!isOpened) {
                    slideAnimator = ValueAnimator.ofInt(0, newHeight);
                    isOpened = true;
                } else {
                    slideAnimator = ValueAnimator.ofInt(newHeight, 0);
                    isOpened = false;
                }

                slideAnimator.setInterpolator(new DecelerateInterpolator());
                slideAnimator.setDuration(300);
                slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        expandableView.getLayoutParams().height = (int) animation.getAnimatedValue();
                        expandableView.requestLayout();
                    }
                });
                slideAnimator.start();
            }
        });
        holder.bind(position, listener);
        if (position % 2 == 1) {
            holder.itemView.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.colorPrimaryDarkSecondTone));
        }
    }

    @Override
    public int getItemCount() {
        return gameModes.size();
    }

    public static class GameModeHolder extends RecyclerView.ViewHolder {
        public TextView gameModeName;
        public TextView gameModeDescription;
        public ImageButton btn;

        public GameModeHolder(View itemView) {
            super(itemView);
            gameModeName = itemView.findViewById(R.id.list_item_game_mode_name);
            gameModeDescription = itemView.findViewById(R.id.list_item_game_mode_description);
            btn = itemView.findViewById(R.id.infoButton);
        }

        public void bind(final Integer position, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(position);
                }
            });
        }

    }

}

