package com.alameen.wael.hp.market;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


class NavRecyclerAdapter extends RecyclerView.Adapter<NavRecyclerAdapter.ViewHolder> {

    private List<String> titlesList;
    private List<Integer> iconsList;
    private AdapterView.OnItemClickListener itemClickListener;
    private static int selectedPos = 0;

    NavRecyclerAdapter(Context context, List<String> titlesList, List<Integer> iconsList, AdapterView.OnItemClickListener itemClickListener) {
        LayoutInflater.from(context);
        this.titlesList = titlesList;
        this.iconsList = iconsList;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_list_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(titlesList.get(position));
        holder.icon.setImageResource(iconsList.get(position));

        if(selectedPos == position) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.itemView.setSelected(true);

        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.itemView.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return titlesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
            icon = (ImageView)itemView.findViewById(R.id.icon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(null, view, getLayoutPosition(), getItemId());
            notifyItemChanged(selectedPos);
            selectedPos = getLayoutPosition();
            itemView.setBackgroundColor(Color.rgb(242, 242, 242));
            notifyItemChanged(selectedPos);
        }
    }
}
