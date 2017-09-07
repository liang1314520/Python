package com.evilbeast.meizi.base;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;

/**
 * Author: sumary
 */
public abstract class RxBaseActivity extends RxAppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置布局内容
        setContentView(getLayoutId());

        //初始化黄油刀控件绑定框架
        ButterKnife.bind(this);

        // 初始化视图
        initViews(savedInstanceState);

        // 初始化工具栏
        initToolbar();

        Log.d("MainActivity", "initToolbar");

    }


    // 获取布局资源ID
    public abstract int getLayoutId();

    // 初始化视图
    public abstract void initViews(Bundle savedInstanceState);

    // 初始化工具栏
    public abstract void initToolbar();


}
