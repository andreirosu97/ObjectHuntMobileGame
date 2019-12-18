package org.redstudios.objecthunt.model;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.redstudios.objecthunt.R;

import java.util.List;

public class ObjectsAdapter extends ArrayAdapter<Pair<String, String>> {

private Context context;
private List<Pair<String,String>> obj_count;
private int layoutResID;

public ObjectsAdapter(Context context, int layoutResourceID, List<Pair<String, String>> obj_count) {
        super(context, layoutResourceID, obj_count);
        this.context = context;
        this.obj_count = obj_count;
        this.layoutResID = layoutResourceID;
        }

    static class ObjViewHolder {
        public TextView objName;
        public TextView objCount;
    }
@Override
public View getView(int position, View convertView, ViewGroup parent) {
    ObjViewHolder holder;
    View view = convertView;

    if (view == null) {
    holder = new ObjViewHolder();
    LayoutInflater inflater = ((Activity) context).getLayoutInflater();

    view = inflater.inflate(layoutResID, parent, false);
    holder.objName = (TextView) view.findViewById(R.id.list_item_obj);
    holder.objCount = (TextView) view.findViewById(R.id.list_item_count);

    view.setTag(holder);
    } else {
        holder = (ObjViewHolder) view.getTag();
    }

    Pair<String,String> item = obj_count.get(position);

    holder.objName.setText(item.first);
    holder.objCount.setText(item.second);

    return view;
    }
}
