package com.alameen.wael.hp.market;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

class SecRecyclerAdapter extends RecyclerView.Adapter<SecRecyclerAdapter.ViewHolder> {

    private List<Sections> sectionsList;
    private static int selectedPos = 0;
    private AdapterView.OnItemClickListener itemClickListener;

    SecRecyclerAdapter(Context context, List<Sections> sectionsList, AdapterView.OnItemClickListener click) {
        this.sectionsList = sectionsList;
        LayoutInflater.from(context);
        itemClickListener = click;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sec_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Sections sections = sectionsList.get(position);
        holder.sectionTitle.setText(sections.getSecTitle());
        holder.sectionImage.setImageResource(sections.getSecImage());

        if(selectedPos == position) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            //holder.itemView.setSelected(true);

        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            //holder.itemView.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return sectionsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView sectionTitle;
        ImageView sectionImage;

        ViewHolder(View itemView) {
            super(itemView);
            sectionTitle = (TextView)itemView.findViewById(R.id.section_title);
            sectionImage = (ImageView)itemView.findViewById(R.id.section_image);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(null, view, getLayoutPosition(), getItemId());
            notifyItemChanged(selectedPos);
            selectedPos = getLayoutPosition();
            //itemView.setBackgroundColor(Color.rgb(242, 242, 242));
            notifyItemChanged(selectedPos);
        }
    }
}
