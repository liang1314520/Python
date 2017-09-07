package com.evilbeast.meizi.adapter;

import android.support.v4.view.ViewCompat;
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
public class MeiZiAdapter extends AbstractAdapter {

    private List<PhotoGroupObject> mDataList = new ArrayList<PhotoGroupObject>();

    public MeiZiAdapter(RecyclerView view, List<PhotoGroupObject> dataList) {
        super(view);
        mDataList = dataList;
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new MeiZiItemHolder(LayoutInflater.from(getContext()).inflate(R.layout.meizi_category_item, parent, false));
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (holder instanceof MeiZiItemHolder) {
            if (mDataList.get(position).isValid()) {
                final MeiZiItemHolder itemHolder = (MeiZiItemHolder) holder;
                itemHolder.imageTitle.setText(mDataList.get(position).getTitle());
                Glide.with(getContext())
                        .load(mDataList.get(position).getImageUrl())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.placeholder_image)
                        .into(itemHolder.imageView)
                        .getSize(new SizeReadyCallback() {
                            @Override
                            public void onSizeReady(int width, int height) {
                                if (!itemHolder.item.isShown()) {
                                    itemHolder.item.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                // 共享元素切换效果的实现
               // itemHolder.imageView.setTag(R.string.app_name, mDataList.get(position).getImageUrl());
               // ViewCompat.setTransitionName(itemHolder.imageView, mDataList.get(position).getImageUrl());
            }


        }
        super.onBindViewHolder(holder, position);
    }

    public class MeiZiItemHolder extends ClickableViewHolder {
        public RatioImageView imageView;
        public TextView imageTitle;
        public View item;

        public MeiZiItemHolder(View itemView) {
            super(itemView);
            imageView = $(R.id.item_img);
            imageTitle = $(R.id.item_title);
            item = itemView;
            imageView.setOriginalSize(236, 354);
        }
    }
}
