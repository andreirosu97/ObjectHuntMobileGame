package org.redstudios.objecthunt.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.redstudios.objecthunt.R;

import java.util.List;
import java.util.Random;

public class LeaderboardLVAdapter extends ArrayAdapter<LeaderboardItem> {

    private Context context;
    private List<LeaderboardItem> userRank;
    private int layoutResID;

    public LeaderboardLVAdapter(Context context, int layoutResourceID, List<LeaderboardItem> userRank) {
        super(context, layoutResourceID, userRank);
        this.context = context;
        this.userRank = userRank;
        assignColors();
        this.layoutResID = layoutResourceID;
    }

    static class RankViewHolder {
        public ImageView userColor;
        public TextView userName;
        public TextView points;
        public TextView rank;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LeaderboardLVAdapter.RankViewHolder holder;
        View view = convertView;

        if (view == null) {
            holder = new LeaderboardLVAdapter.RankViewHolder();
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            view = inflater.inflate(layoutResID, parent, false);
            holder.userColor = view.findViewById(R.id.list_item_color);
            holder.userName = view.findViewById(R.id.list_item_name);
            holder.points = view.findViewById(R.id.list_item_point);
            holder.rank = view.findViewById(R.id.list_item_rank);
            view.setTag(holder);
        } else {
            holder = (LeaderboardLVAdapter.RankViewHolder) view.getTag();
        }

        LeaderboardItem item = userRank.get(position);

        holder.userColor.setBackgroundColor(item.getColor());
        holder.userName.setText(item.getDisplayName());
        String displayingScore = item.getDisplayScore() + "pts";
        holder.points.setText(displayingScore);
        holder.rank.setText(item.getDisplayRank());

        return view;
    }

    private int generateColor() {
        Random rand = new Random();
        return Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private void assignColors() {
        for (LeaderboardItem item : userRank) {
            item.setColor(generateColor());
        }
    }
}
