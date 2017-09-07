package com.evilbeast.meizi.module.activity;


import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.evilbeast.meizi.R;
import com.evilbeast.meizi.base.RxBaseActivity;
import com.evilbeast.meizi.module.meizi.MeiZiPageFragment;
import com.evilbeast.meizi.module.meizi.MeiZiSimpleFragment;
import com.evilbeast.meizi.module.zxfuli.FuliPageFragment;
import com.evilbeast.meizi.utils.SnackbarUtil;

import java.util.List;

import butterknife.BindView;

public class MainActivity extends RxBaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private long mUserExitTime;

    private int currentIndex;
    private Fragment[] fragmentList;

    // 绑定toolbar
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    // NavigateView
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;





    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        initFragments();
        initNavigationView();

    }

    private void initFragments() {
        MeiZiPageFragment meizi_fragment_1 = MeiZiPageFragment.newInstance();
        FuliPageFragment fuli_fragment_2 = FuliPageFragment.newInstance();
        fragmentList = new Fragment[] {
                meizi_fragment_1,
                fuli_fragment_2,
        };
        getSupportFragmentManager().beginTransaction().replace(R.id.content, meizi_fragment_1).commit();
        currentIndex = 0;
    }

    private void initNavigationView() {
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void initToolbar() {
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void changeIndex(int index, String title) {
        switchFragment(index);
        mToolbar.setTitle(title);
        mDrawerLayout.closeDrawers();
    }

    public void switchFragment(int index) {
        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
        trx.hide(fragmentList[currentIndex]);
        if (!fragmentList[index].isAdded()) {
            trx.add(R.id.content, fragmentList[index]);
        }
        trx.show(fragmentList[index]).commit();
        currentIndex = index;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitApp();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exitApp() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (System.currentTimeMillis() - mUserExitTime > 2000) {
            SnackbarUtil.showMessage(mDrawerLayout, getString(R.string.user_exit_app_tip));
            mUserExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.nav_home:
                changeIndex(0, getResources().getString(R.string.nav_home));
                return true;
            case R.id.nav_fuli:
                changeIndex(1, getResources().getString(R.string.nav_fuli));
                return true;
            default:
                break;
        }
        return true;
    }
}
