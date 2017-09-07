package com.evilbeast.meizi.module.common;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.evilbeast.meizi.R;
import com.evilbeast.meizi.base.RxBaseActivity;
import com.evilbeast.meizi.entity.photo.PhotoObject;
import com.evilbeast.meizi.network.Api.ZxFuliApi;
import com.evilbeast.meizi.network.RetrofixHelper;
import com.evilbeast.meizi.rx.RxBus;
import com.evilbeast.meizi.utils.ConstantUtil;
import com.evilbeast.meizi.utils.GlideUtil;
import com.evilbeast.meizi.utils.ImmersiveUtil;
import com.evilbeast.meizi.utils.MeiZiUtil;
import com.evilbeast.meizi.utils.StatusBarCompat;
import com.evilbeast.meizi.widget.DepthTransFormes;
import com.jakewharton.rxbinding.view.RxMenuItem;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import io.realm.Realm;
import io.realm.Sort;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PhotoViewActivity extends RxBaseActivity {

    private static final String EXTRA_GROUP_ID = "extra_group_id";
    private static final String EXTRA_GROUP_TITLE = "extra_group_title";
    private static final String EXTRA_MODULE_NAME = "extra_module_name";

    private int groupId;
    private String groupTitle;
    private String currentImageUrl;
    private String moduleName;

    // 数据
    private Realm realm;
    private List<PhotoObject> mDataList;

    private PhotoPagerAdapter mPagerAdapter;

    private boolean toolbarIsHidden;

    @BindView(R.id.appbar_layout)
    AppBarLayout mAppbarLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView mTitlebar;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    @Override
    public int getLayoutId() {
        return R.layout.meizi_detail_activity;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        groupId = intent.getIntExtra(EXTRA_GROUP_ID, -1);
        groupTitle = intent.getStringExtra(EXTRA_GROUP_TITLE);
        moduleName = intent.getStringExtra(EXTRA_MODULE_NAME);

        // 获取数据
        realm = Realm.getDefaultInstance();
        mDataList = realm.where(PhotoObject.class)
                .equalTo("groupId", groupId)
                .equalTo("module", moduleName)
                .findAllSorted("position", Sort.ASCENDING);

        if (mDataList.size() > 0) {
            currentImageUrl = mDataList.get(0).getImageUrl();
        }

        mPagerAdapter = new PhotoPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        //  页面翻页过渡效果
        mViewPager.setPageTransformer(true, new DepthTransFormes());

        mPagerAdapter.notifyDataSetChanged();


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentImageUrl = mDataList.get(position).getImageUrl();
                mTitlebar.setText(position+1+"/"+mDataList.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        RxBus.getInstance().toObserverable(String.class)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (s.equals("toggleToolBar")) {
                            toggleToolbar();
                        } else if (s.startsWith("http")) {
                            saveImage(s);
                        }
                    }
                });
    }

    public static Intent newIntent(Activity activity, int groupId, String groupTitle, String moduleName) {
        Intent intent = new Intent(activity, PhotoViewActivity.class);
     //   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_GROUP_TITLE, groupTitle);
        intent.putExtra(EXTRA_MODULE_NAME, moduleName);
        return intent;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }



    @Override
    public void initToolbar() {
        mToolbar.setTitle("");
        mTitlebar.setText("1/"+mDataList.size());
        mToolbar.setBackgroundResource(R.color.black_90);
        mAppbarLayout.setBackgroundResource(R.color.black_90);
        mAppbarLayout.setAlpha(0.5f);

        StatusBarCompat.setStatusBarColor(this, ContextCompat.getColor(this,R.color.black_90),50);

        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });

        mToolbar.inflateMenu(R.menu.menu_meizi);

        // 保存图片
        connectSaveImageEvent();


    }

    private void saveImage(final String url) {
        Observable.just(url)
                .compose(this.<String>bindToLifecycle())
                .compose(RxPermissions.getInstance(PhotoViewActivity.this).ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .observeOn(Schedulers.io())
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean;
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Uri>>() {
                    @Override
                    public Observable<Uri> call(Boolean aBoolean) {
                        return GlideUtil.saveImageToLocal(PhotoViewActivity.this, url);
                    }
                })
                .map(new Func1<Uri, String>() {
                    @Override
                    public String call(Uri uri) {
                        return String.format("图片已保存至 %s 文件夹",
                                new File(Environment.getExternalStorageDirectory(), ConstantUtil.FILE_DIR)
                                        .getAbsolutePath());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(PhotoViewActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(PhotoViewActivity.this, "保存失败,请重试", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void connectSaveImageEvent() {
        mToolbar.getMenu().findItem(R.id.action_save_image).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                saveImage(currentImageUrl);
                return  true;
            }
        });

        //RxMenuItem.clicks().

    }

    protected void toggleToolbar() {
        if (toolbarIsHidden) {
            ImmersiveUtil.exit(this);
            mAppbarLayout.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(2))
                    .start();
            toolbarIsHidden = false;
        } else {
            ImmersiveUtil.enter(this);
            mAppbarLayout.animate()
                    .translationY(-mAppbarLayout.getHeight())
                    .setInterpolator(new DecelerateInterpolator(2))
                    .start();
            toolbarIsHidden = true;
        }
    }

    private class PhotoPagerAdapter extends FragmentStatePagerAdapter {

        public PhotoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            PhotoObject data = mDataList.get(position);
            return PhotoPageFragment.newInstance(data.getId());

        }

        @Override
        public int getCount() {
            return mDataList.size();
        }
    }
}
