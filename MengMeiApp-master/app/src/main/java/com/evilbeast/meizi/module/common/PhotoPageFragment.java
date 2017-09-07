package com.evilbeast.meizi.module.common;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.evilbeast.meizi.R;
import com.evilbeast.meizi.base.RxBaseFragment;
import com.evilbeast.meizi.entity.photo.PhotoObject;
import com.evilbeast.meizi.network.Api.MeiZiApi;
import com.evilbeast.meizi.network.Api.ZxFuliApi;
import com.evilbeast.meizi.network.RetrofixHelper;
import com.evilbeast.meizi.rx.RxBus;
import com.evilbeast.meizi.utils.LogUtil;
import com.evilbeast.meizi.utils.MeiZiUtil;
import com.wang.avi.AVLoadingIndicatorView;


import java.io.IOException;

import butterknife.BindView;
import io.realm.Realm;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Author: sumary
 */
public class PhotoPageFragment extends RxBaseFragment implements RequestListener<String, GlideDrawable> {

    private static final String EXTRA_DATA_ID = "extra_data_id";

    private String image_url;
    private PhotoObject mData;
    private boolean isAccessNet = false;
    private Realm mRealm;

    private PhotoViewAttacher mPhotoViewAttacher;

    @BindView(R.id.image_view)
    ImageView mImageView;

    @BindView(R.id.error_msg)
    TextView mTextViewErrorMsg;

    @BindView(R.id.av_loading)
    AVLoadingIndicatorView mAvLoading;

    @Override
    public int getLayoutId() {
        return R.layout.meizi_detail_page_fragment;
    }

    @Override
    public void initViews() {
        mRealm = Realm.getDefaultInstance();
        int id = getArguments().getInt(EXTRA_DATA_ID);
        mData = mRealm.where(PhotoObject.class).equalTo("id", id).findFirst();


        if (mData != null && !mData.isEmpty())  {
            image_url = mData.getImageUrl();
        }

        if (image_url != null && !image_url.isEmpty()) {
            LogUtil.w("image："+ image_url);
            glideLoadImageUrl(image_url);
        } else {
            LogUtil.w("no image loaed");
            loadNetImage();
        }
    }


    @Override
    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
        // 显示错误信息
        if (isAccessNet) {
            showErrorMsg();
        } else {
            isAccessNet = true;
            loadNetImage();
        }
        return false;
    }
    public void showErrorMsg() {
        hideLoading();
        mTextViewErrorMsg.setVisibility(View.VISIBLE);
    }
    public void hideErrorMsg() { mTextViewErrorMsg.setVisibility(View.GONE);}
    public void showLoading() { mAvLoading.setVisibility(View.VISIBLE);}
    public void hideLoading() { mAvLoading.setVisibility(View.GONE);}

    private void glideLoadImageUrl(String url) {
       Glide.with(this).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .crossFade(0)
                .listener(this)
                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    private void saveUrlToCache(final String url) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mData.setImageUrl(url);
            }
        });
    }

    private void loadNetImage() {
        if (mData.getModule().equals("fuli")) {
            loadFuliNetImage();
        } else if (mData.getModule().equals("meizi")) {
            loadMeiziNetImage();
        } else {
            showErrorMsg();
        }
    }

    private void loadMeiziNetImage() {
        RetrofixHelper.createTextApi(MeiZiApi.class)
                .getGroupImageData(mData.getGroupId(), mData.getPosition())
                .compose(this.<ResponseBody>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        try {
                            String html = responseBody.string();
                            String url = MeiZiUtil.getInstance().parseMeiziImageUrl(html);
                            saveUrlToCache(url);
                            glideLoadImageUrl(url);

                        } catch (IOException e) {
                            showErrorMsg();
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showErrorMsg();
                    }
                });

    }
    private void loadFuliNetImage() {

        RetrofixHelper.createTextApi(ZxFuliApi.class)
                .getGroupImageData(mData.getGroupId(), mData.getPosition())
                .compose(this.<ResponseBody>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        try {
                            String html = responseBody.string();
                            String url =  MeiZiUtil.getInstance().parseFuliImageUrl(html);
                            saveUrlToCache(url);
                            glideLoadImageUrl(url);

                        } catch (IOException e) {
                            showErrorMsg();
                            e.printStackTrace();
                        }


                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showErrorMsg();
                    }
                });
    }
    @Override
    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

        // 设置Image
        mImageView.setImageDrawable(resource);
        mPhotoViewAttacher = new PhotoViewAttacher(mImageView);
        setPhotoViewAttacher();

        // 关闭错误提示

        hideErrorMsg();
        hideLoading();
        return false;
    }

    private void setPhotoViewAttacher() {
        mPhotoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float v, float v1) {
                RxBus.getInstance().post("toggleToolBar");
            }
        });

        mPhotoViewAttacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setMessage("是否保存到本地")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RxBus.getInstance().post(mData.getImageUrl());
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }



    public static PhotoPageFragment newInstance(int id) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_DATA_ID, id);
        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
