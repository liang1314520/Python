package com.evilbeast.meizi.module.meizi;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.evilbeast.meizi.R;
import com.evilbeast.meizi.base.RxBaseFragment;
import com.evilbeast.meizi.utils.LogUtil;
import com.flyco.tablayout.SlidingTabLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

/**
 * Author: sumary
 */
public class MeiZiPageFragment extends RxBaseFragment {

    @BindView(R.id.sliding_tab)
    SlidingTabLayout mTabLayout;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    // titles and types
    private List<String> titles = Arrays.asList("最新","推荐", "热门", "清纯", "台湾", "日本", "性感");
    private List<String> types = Arrays.asList("","best", "hot", "mm", "taiwan", "japan", "xinggan");



    @Override
    public int getLayoutId() {
        return R.layout.meizi_view_fragment;
    }

    @Override
    public void initViews() {
        mViewPager.setAdapter(new MeiZiPageAdapter(getChildFragmentManager()));
        mViewPager.setOffscreenPageLimit(1);
        mTabLayout.setViewPager(mViewPager);
    }

    public static MeiZiPageFragment newInstance() {
        return new MeiZiPageFragment();
    }

    private class MeiZiPageAdapter extends FragmentStatePagerAdapter {
        private HashMap<Integer, MeiZiSimpleFragment> fragments = new HashMap<>();

        public MeiZiPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (fragments.containsKey(position)) {
                return fragments.get(position);
            }
            MeiZiSimpleFragment fragment = MeiZiSimpleFragment.newInstance(types.get(position));
            fragments.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
