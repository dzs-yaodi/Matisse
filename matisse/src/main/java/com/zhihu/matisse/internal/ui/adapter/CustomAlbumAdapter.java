package com.zhihu.matisse.internal.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhihu.matisse.R;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.listener.OnSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class CustomAlbumAdapter extends RecyclerView.Adapter<CustomAlbumAdapter.CustomAlbumViewHolder>{

    private List<Item> items = new ArrayList<>();
    private Context mContext;
    public Drawable mPlaceholder;
    private int mImageResize;
    private OnMediaSelectListener onSelectedListener;

    public void setOnSelectedListener(OnMediaSelectListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    public CustomAlbumAdapter(Context context) {
        this.mContext = context;

        TypedArray ta = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.item_placeholder});
        mPlaceholder = ta.getDrawable(0);
        ta.recycle();

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int availableWidth = screenWidth - context.getResources().getDimensionPixelSize(
                R.dimen.media_custom_video_grid_spacing) * (4 - 1);
        mImageResize = availableWidth / 4;
    }

    @NonNull
    @Override
    public CustomAlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomAlbumViewHolder(LayoutInflater.from(mContext).inflate(
                R.layout.item_custom_media_select,parent,false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAlbumViewHolder holder, int position) {

        Item item = items.get(position);
        if (item != null) {

            SelectionSpec.getInstance().imageEngine.loadThumbnail(mContext, mImageResize,
                    mPlaceholder, holder.media_thumbnail,item.getContentUri());

            holder.video_duration.setText(DateUtils.formatElapsedTime(item.duration / 1000));

            if (item.isChecked){
                holder.viewCover.setVisibility(View.VISIBLE);
            }else{
                holder.viewCover.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(view -> {
            onSelectedListener.onSelect(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addList(List<Item> itemList) {
        this.items.clear();
        this.items.addAll(itemList);
        notifyDataSetChanged();
    }

    class CustomAlbumViewHolder extends RecyclerView.ViewHolder{

        ImageView media_thumbnail;
        TextView video_duration;
        View viewCover;
        public CustomAlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            media_thumbnail = itemView.findViewById(R.id.media_thumbnail);
            video_duration = itemView.findViewById(R.id.video_duration);
            viewCover = itemView.findViewById(R.id.view_cover);
        }
    }

    public interface OnMediaSelectListener{
        void onSelect(int position);
    }
}
