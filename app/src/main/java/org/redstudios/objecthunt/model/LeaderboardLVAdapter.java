package org.redstudios.objecthunt.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Random;

import org.redstudios.objecthunt.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardLVAdapter extends ArrayAdapter<Pair<String, Integer>> {

    private class Item {
        private int color;
        private String name;
        private int pts;
        private int count;

        public Item(int color, String name, int pts, int count) {
            this.color = color;
            this.name = name;
            this.pts = pts;
            this.count = count;
        }
    }

    private Context context;
    private List<Item> userRank;
    private int layoutResID;

    public LeaderboardLVAdapter(Context context, int layoutResourceID, List<Pair<String, Integer>> userRank) {
        super(context, layoutResourceID, userRank);
        this.context = context;
        userRank = sortUserRank(userRank);
        this.userRank = transformIntoItems(userRank);
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

        Item item = userRank.get(position);

        holder.userColor.setBackgroundColor(item.color);
        holder.userName.setText(item.name);
        holder.points.setText(item.pts + "pts");
        holder.rank.setText(Integer.toString(item.count));

        return view;
    }

    private List<Pair<String, Integer>> sortUserRank(List<Pair<String, Integer>> userRank) {
        Collections.sort(userRank, (Pair<String, Integer> t0, Pair<String, Integer> t1) -> -Integer.compare(t0.second, t1.second));
        return userRank;
    }

    private int generateColor() {
        Random rand = new Random();
        return Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private List<Item> transformIntoItems(List<Pair<String, Integer>> userRank) {
        List<Item> items = new ArrayList<>();
        int i = 0;
        for (Pair<String, Integer> item : userRank) {
            items.add(new Item(generateColor(), item.first, item.second, ++i));
        }
        return items;
    }
}
