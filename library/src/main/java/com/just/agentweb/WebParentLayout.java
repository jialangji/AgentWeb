package com.just.agentweb;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by cenxiaozhong on 2017/12/8.
 */

public class WebParentLayout extends FrameLayout implements Provider<AgentWebUIController> ,WebViewClientCallbackManager.PageLifeCycleCallback{
    private AgentWebUIController mAgentWebUIController = null;
    private String TAG = this.getClass().getSimpleName();

    private WebView mWebView;
    private Set<String> mErrorUrls=new CopyOnWriteArraySet<>();
    public WebParentLayout(@NonNull Context context) {
        this(context, null);
    }

    public WebParentLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WebParentLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("WebParentLayout context must be activity or activity sub class .");
        }
    }

    void bindController(AgentWebUIController agentWebUIController) {
        LogUtils.i(TAG, "bindController:" + agentWebUIController);
        this.mAgentWebUIController = agentWebUIController;
        this.mAgentWebUIController.bindWebParent(this, (Activity) getContext());
    }

    public void showPageMainFrameError(@LayoutRes int layoutRes) {

        mErrorUrls.add(getWebView().getUrl());
        View container = this.findViewById(R.id.mainframe_error_container_id);
        if (container != null) {
            container.setVisibility(View.VISIBLE);
        } else {
            ViewStub mViewStub = (ViewStub) this.findViewById(R.id.mainframe_error_viewsub_id);
            final FrameLayout mFrameLayout = new FrameLayout(getContext());
            mFrameLayout.setId(R.id.mainframe_error_container_id);
            LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
            mLayoutInflater.inflate(layoutRes, mFrameLayout, true);
            final int index = this.indexOfChild(mViewStub);
            this.removeViewInLayout(mViewStub);
            final ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null) {
                this.addView(mFrameLayout, index, layoutParams);
            } else {
                this.addView(mFrameLayout, index);
            }
            mFrameLayout.setVisibility(View.VISIBLE);
            mFrameLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getWebView() != null) {
                        getWebView().reload();
                    }

                }
            });
        }
    }

    public void hidePageMainFrameError(){
        View mView=null;
        if((mView=this.findViewById(R.id.mainframe_error_container_id))!=null){
            mView.setVisibility(View.GONE);
        }
    }
    @Override
    public AgentWebUIController provide() {
        return this.mAgentWebUIController;
    }


    void bindWebView(WebView view) {
        if (this.mWebView == null) {
            this.mWebView = view;
        }
    }

    public WebView getWebView() {
        return this.mWebView;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        LogUtils.i(TAG,"webparentlayout onPageFinished:"+url);
        if(!mErrorUrls.contains(url)){
            hidePageMainFrameError();
            mErrorUrls.clear();
        }
    }
}
