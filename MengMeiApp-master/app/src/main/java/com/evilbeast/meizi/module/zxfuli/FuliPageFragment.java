package com.evilbeast.meizi.module.zxfuli;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.evilbeast.meizi.R;
import com.evilbeast.meizi.adapter.AbstractAdapter;
import com.evilbeast.meizi.adapter.FuliAdapter;
import com.evilbeast.meizi.base.RxBaseFragment;
import com.evilbeast.meizi.entity.photo.PhotoGroupObject;
import com.evilbeast.meizi.entity.photo.PhotoObject;
import com.evilbeast.meizi.module.common.PhotoViewActivity;
import com.evilbeast.meizi.network.Api.ZxFuliApi;
import com.evilbeast.meizi.network.RetrofixHelper;
import com.evilbeast.meizi.utils.MeiZiUtil;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import io.realm.Realm;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Author: sumary
 */
public class FuliPageFragment extends RxBaseFragment {

    public static final String MODULE_NAME = "zxfuli";

    private List<PhotoGroupObject> mDataList;
    private Realm realm;

    private FuliAdapter mAdapter;
    private StaggeredGridLayoutManager mLayoutManager;

    @BindView(R.id.fuli_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.fuli_swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    private int page = 1;
    private int pageNum = 15;
    private int PRELOAD_NUM = 6;
    private boolean hasLoadMore = true;



    @Override
    public int getLayoutId() {
        return R.layout.fuli_page_fragment;
    }

    @Override
    public void initViews() {

        realm = Realm.getDefaultInstance();
        mDataList = realm.where(PhotoGroupObject.class).equalTo("module", MODULE_NAME).findAll();

        initRecyclerView();
        initSwipeRefresh();

        setItemClickListener();

    }

    private void initRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new FuliAdapter(mRecyclerView, mDataList);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(loadMoreListener());
    }

    private RecyclerView.OnScrollListener loadMoreListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean isBottom = mLayoutManager.findLastCompletelyVisibleItemPositions(new int[2])[1] > mAdapter.getItemCount() - PRELOAD_NUM;
                if (!mSwipeRefresh.isRefreshing() && isBottom && hasLoadMore) {
                    mSwipeRefresh.setRefreshing(true);
                    page++;
                    fetchData();
                }

            }
        };
    }

    private void initSwipeRefresh() {
        mSwipeRefresh.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefresh();
            }
        });

        if (mDataList.size() <= 0) {
            mSwipeRefresh.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefresh.setRefreshing(true);
                    pullRefresh();
                }
            }, 500);
        }
    }

    private void pullRefresh() {
        page = 1;
        hasLoadMore = true;
//        clearData();
        fetchData();
    }

    private void clearData() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(PhotoGroupObject.class).equalTo("module", MODULE_NAME).findAll().deleteAllFromRealm();
            }
        });
    }


    private void fetchData() {
        RetrofixHelper.createTextApi(ZxFuliApi.class)
                .getImageData(page)
                .compose(this.<ResponseBody>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        try {
                            String html = responseBody.string();
                            List<PhotoGroupObject> list = MeiZiUtil.getInstance().parseFuliItems(html, MODULE_NAME);

                            if (page == 1) {
                                clearData();
                            }
                            MeiZiUtil.getInstance().putFuliItemCache(list);
                            boolean isEnd = list.size() <= 0;
                            finishTask(isEnd);

                        } catch (IOException e) {
                            if ( mSwipeRefresh.isRefreshing()) {
                                mSwipeRefresh.setRefreshing(false);
                            }
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if ( mSwipeRefresh.isRefreshing()) {
                            mSwipeRefresh.setRefreshing(false);
                        }
                    }
                });
    }

    private void fetchGroupDataAndOpen(final int groupId) {
        long nums = realm.where(PhotoObject.class).equalTo("groupId", groupId).equalTo("module", MODULE_NAME).count();
        if (nums > 0) {
            Intent intent = PhotoViewActivity.newIntent(getActivity(), groupId, "", MODULE_NAME);
            startActivity(intent);
        } else {
            RetrofixHelper.createTextApi(ZxFuliApi.class)
                    .getGroupData(groupId)
                    .compose(this.<ResponseBody>bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<ResponseBody>() {
                        @Override
                        public void call(ResponseBody responseBody) {
                            try {
                                String html = responseBody.string();
                                List<PhotoObject> list = MeiZiUtil.getInstance().parseFuliGroupHtml(html, groupId, MODULE_NAME);
                                MeiZiUtil.getInstance().putGroupCache(list);
                                Intent intent = PhotoViewActivity.newIntent(getActivity(), groupId, "", MODULE_NAME);
                                startActivity(intent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
        }

    }

    private void finishTask(boolean isEnd) {
        if ( mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }

        if (isEnd) {
            hasLoadMore = false;

        } else {
            if (page > 1) {
                mAdapter.notifyItemRangeChanged((page-1)*pageNum-1, pageNum);
            } else {
                mAdapter.notifyDataSetChanged();
            }

           // setItemClickListener();

        }

    }

    private void setItemClickListener() {
        mAdapter.setOnItemClickListener(new AbstractAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, AbstractAdapter.ClickableViewHolder holder) {
                fetchGroupDataAndOpen(mDataList.get(position).getGroupId());
            }
        });
    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    public static FuliPageFragment newInstance() {
        return new FuliPageFragment();
    }
}
