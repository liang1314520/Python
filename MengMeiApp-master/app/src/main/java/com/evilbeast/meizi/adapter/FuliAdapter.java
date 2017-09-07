package com.evilbeast.meizi.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.evilbeast.meizi.R;
import com.evilbeast.meizi.entity.photo.PhotoGroupObject;
import com.evilbeast.meizi.widget.RatioImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sumary
 */
public class FuliAdapter extends AbstractAdapter {

    private List<PhotoGroupObject> mDataList = new ArrayList<>();
    /**
     * 传入view,处理View的滚动监听事件
     *
     * @param view
     */
    public FuliAdapter(RecyclerView view, List<PhotoGroupObject> data) {
        super(view);
        mDataList = data;
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new FuliViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fuli_page_item, parent, false));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (!mDataList.get(position).isValid()) {
            super.onBindViewHolder(holder, position);
            return;
        }
        final FuliViewHolder viewHolder = (FuliViewHolder) holder;

        viewHolder.itemTitle.setText(mDataList.get(position).getTitle());
        Glide.with(getContext())
                .load(mDataList.get(position).getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .into(viewHolder.itemImage)
                .getSize(new SizeReadyCallback() {
                    @Override
                    public void onSizeReady(int width, int height) {
                        if (!viewHolder.rootView.isShown()) {
                            viewHolder.rootView.setVisibility(View.VISIBLE);
                        }
                    }
                });

        super.onBindViewHolder(holder, position);
    }

    class FuliViewHolder extends ClickableViewHolder {

        public RatioImageView itemImage;
        public TextView itemTitle;
        public View rootView;

        public FuliViewHolder(View itemView) {
            super(itemView);
            itemImage = $(R.id.fuli_item_image);
            itemTitle = $(R.id.fuli_item_title);
            rootView = itemView;
            itemImage.setOriginalSize(164, 230);
        }
    }
}
