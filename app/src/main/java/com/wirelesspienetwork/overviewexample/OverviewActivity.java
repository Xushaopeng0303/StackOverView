package com.wirelesspienetwork.overviewexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wirelesspienetwork.overview.model.OverviewAdapter;
import com.wirelesspienetwork.overview.model.ViewHolder;
import com.wirelesspienetwork.overview.views.Overview;

import java.util.ArrayList;
import java.util.Random;

/**
 * The main Recent activity that is started from AlternateRecentComponent.
 */
public class OverviewActivity extends Activity implements View.OnClickListener {
    Overview mRecentView;
    ArrayList<WindowModel> mWindowDataList;
    OverviewAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBottomBar();
        initData();
        initOverView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_over_view_back:
                finish();
                break;
            case R.id.id_over_view_add:
                WindowModel model = new WindowModel();
                Random random = new Random();
                random.setSeed(mWindowDataList.size());
                int color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
                model.thumbnailIconUrl = "";
                model.colorId = color;
                model.title = "（new） 窗口";
                model.windowDrawable = null;
                mWindowDataList.add(model);
                mAdapter.notifyDataSetChanged(mWindowDataList);
                break;
            case R.id.id_over_view_clear_all:
                mRecentView.clearDisappearingChildren();
                break;
        }
    }

    private void initData() {
        mWindowDataList = new ArrayList<>();
        WindowModel model;
        for (int i = 0; i < 5; i++) {
            model = new WindowModel();
            Random random = new Random();
            random.setSeed(i);
            int color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
            model.thumbnailIconUrl = "";
            model.colorId = color;
            model.title = "这是第 " + i + " 个窗口";
            model.windowDrawable = null;
            mWindowDataList.add(model);
        }
    }

    private void initBottomBar() {
        findViewById(R.id.id_over_view_back).setOnClickListener(this);
        findViewById(R.id.id_over_view_add).setOnClickListener(this);
        findViewById(R.id.id_over_view_clear_all).setOnClickListener(this);
    }

    private void initOverView() {
        mAdapter = new OverviewAdapter<ViewHolder<View, WindowModel>, WindowModel>(mWindowDataList) {
            @Override
            public ViewHolder<View, WindowModel> onCreateViewHolder(Context context, ViewGroup parent) {
                View v = View.inflate(context, R.layout.recents_dummy, null);
                return new ViewHolder<>(v);
            }

            @Override
            public void onBindViewHolder(ViewHolder<View, WindowModel> viewHolder) {
                FrameLayout imageView = viewHolder.itemView.findViewById(R.id.id_over_view_image_view);
                imageView.setBackgroundColor(viewHolder.model.colorId);
                ImageView closeView = viewHolder.itemView.findViewById(R.id.id_over_view_close);
                closeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("xsp", "onClick: 点击关闭按钮");
                    }
                });

                TextView titleText = viewHolder.itemView.findViewById(R.id.id_over_view_title_text);
                titleText.setText(viewHolder.model.title);
            }
        };
        mRecentView = findViewById(R.id.recent_view);
        mRecentView.setTaskStack(mAdapter);
        mRecentView.setCallbacks(new Overview.RecentViewsCallbacks() {
            @Override
            public void onCardDismissed(int position) {
                Log.d("xsp", "第几个Card消失：" + position);
            }

            @Override
            public void onAllCardsDismissed() {
                finish();
            }
        });
    }
}