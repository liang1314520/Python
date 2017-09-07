package com.evilbeast.meizi.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.evilbeast.meizi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: sumary
 */
public abstract class AbstractAdapter extends RecyclerView.Adapter<AbstractAdapter.ClickableViewHolder> {

    // 绑定context
    private Context mContext;

    // 绑定RecyclerView
    protected RecyclerView mRecyclerView;

    // 滚动事件回调列表
    protected List<RecyclerView.OnScrollListener> mListeners = new ArrayList<RecyclerView.OnScrollListener>();

    /**
     * 传入view,处理View的滚动监听事件
     * @param view
     */
    public AbstractAdapter(RecyclerView view) {
        mRecyclerView = view;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                for (RecyclerView.OnScrollListener listener: mListeners) {
                    listener.onScrollStateChanged(recyclerView, newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                for (RecyclerView.OnScrollListener listener: mListeners) { 
                    listener.onScrolled(recyclerView, dx, dy);
                }
            }
        });
    }

    /**
     * 添加滚动监听事件
     * @param listener
     */
    public void addOnScrollListener(RecyclerView.OnScrollListener listener) {
        mListeners.add(listener);
    }


    @Override
    public  void onBindViewHolder(final ClickableViewHolder holder, final int position) {
        holder.getParentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(position, holder);
                }
            }
        });

        holder.getParentView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemLongClickListener != null) {
                    return mItemLongClickListener.onItemLongClick(position, holder);
                } else {
                    return false;
                }
            }
        });

    }


    /** clicked listener **/
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    public interface OnItemClickListener  {
        public void onItemClick(int position, ClickableViewHolder holder);
    }

    public interface OnItemLongClickListener {
        public boolean onItemLongClick(int position, ClickableViewHolder holder);
    }

    public void setOnItemClickListener (OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setOnItemLongClickListener (OnItemLongClickListener listener ) {
        this.mItemLongClickListener = listener;
    }

    /** Context **/
    public void bindContext(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * ViewHolder add clicked
     */
    public class ClickableViewHolder extends RecyclerView.ViewHolder {
        private View parentView;
        public ClickableViewHolder(View itemView) {
            super(itemView);
            parentView = itemView;
        }

        public View getParentView() {
            return parentView;
        }

        // findViewById use $
        public <T extends View> T $(@IdRes int id) {
            return (T) parentView.findViewById(id);
        }
    }
}
