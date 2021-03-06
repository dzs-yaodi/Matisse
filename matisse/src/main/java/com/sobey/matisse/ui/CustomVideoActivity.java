package com.sobey.matisse.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.sobey.matisse.R;
import com.sobey.matisse.internal.entity.Album;
import com.sobey.matisse.internal.entity.Item;
import com.sobey.matisse.internal.entity.SelectionSpec;
import com.sobey.matisse.internal.model.AlbumCollection;
import com.sobey.matisse.internal.model.AlbumMediaCollection;
import com.sobey.matisse.internal.model.SelectedItemCollection;
import com.sobey.matisse.internal.ui.adapter.CustomAlbumAdapter;
import com.sobey.matisse.internal.ui.widget.MediaGridInset;
import com.sobey.matisse.internal.utils.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomVideoActivity extends AppCompatActivity implements AlbumCollection.AlbumCallbacks, AlbumMediaCollection.AlbumMediaCallbacks {

    public static final String EXTRA_RESULT_VIDEO_URI = "extra_result_video_uri";
    private RecyclerView mRecyclerView;
    private TextView tvCancel;
    private TextView tvNext;
    private VideoView videoView;
    private SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();
    private ImageView ivCover;
    private CustomAlbumAdapter customAlbumAdapter;
    private List<Item> items = new ArrayList<>();
    //选中的视频
    private int selectIndex = 0;
    private ImageView ivPlay1;
    private ImageView ivPlay2;
    private SeekBar mSeekBar;
    private TextView tvDuration;
    private TextView tvProgress;
    //视频长度
    private int length = 0;
    //需要跳转的页面地址
    private String className;
    private Map<String,String> objectMap = new HashMap<>();
    private Handler handler = new Handler();
    private long firstZero;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int playTimes = videoView.getCurrentPosition() / 1000;
            if (playTimes == 0 && firstZero == 0){
                firstZero = System.currentTimeMillis();
            }

            if (length == 0){
                length = videoView.getDuration() / 1000;
                mSeekBar.setMax(length);
                mSeekBar.setProgress(playTimes);
                tvDuration.setText(DateUtils.formatElapsedTime(length));
            }else {
                String times = tvDuration.getText().toString().trim();
                String allTime = DateUtils.formatElapsedTime(length);
                if (!TextUtils.equals(times,allTime)){
                    tvDuration.setText(DateUtils.formatElapsedTime(length));
                }
            }
            if (playTimes == length){
                runOnUiThread(()-> {
                    tvProgress.setText(DateUtils.formatElapsedTime(length));
                    mSeekBar.setProgress(length);

                    ivPlay1.setImageResource(R.drawable.matisse_video_play_large);
                    ivPlay2.setImageResource(R.drawable.matisse_video_play_small);
                    ivPlay1.setVisibility(View.VISIBLE);
                    handler.removeCallbacks(runnable);
                });

            }else{
                runOnUiThread(()->{

                    if (playTimes == 0  && System.currentTimeMillis() - firstZero > 2000 && !videoView.isPlaying()){
                        tvProgress.setText(DateUtils.formatElapsedTime(length));
                        mSeekBar.setProgress(length);

                        ivPlay1.setImageResource(R.drawable.matisse_video_play_large);
                        ivPlay2.setImageResource(R.drawable.matisse_video_play_small);
                        ivPlay1.setVisibility(View.VISIBLE);
                        handler.removeCallbacks(runnable);
                    }else{
                        tvProgress.setText(DateUtils.formatElapsedTime(playTimes));
                        mSeekBar.setProgress(playTimes);
                    }
                });
            }
            if (playTimes == 0 && System.currentTimeMillis() - firstZero > 2000){
                handler.removeCallbacks(runnable);
            }else {
                handler.postDelayed(runnable, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.sobey_activity_custom_video);

        //设置沉浸式状态栏
        UIUtils.translucentStatusBar(this, true);
        //计算出状态栏高度，并设置view留出对应位置
        int height = (int) UIUtils.getStatusBarHeight(this);
        View bgView = findViewById(R.id.title_container);
        bgView.setPadding(0, height, 0, 0);
        bgView.setBackgroundColor(Color.BLACK);

        mRecyclerView = findViewById(R.id.recyclerView);
        tvCancel = findViewById(R.id.tv_cancle);
        tvNext = findViewById(R.id.tv_next);
        videoView = findViewById(R.id.videoView);
        ivCover = findViewById(R.id.image_cover);
        ivPlay1 = findViewById(R.id.image_play);
        ivPlay2 = findViewById(R.id.image_play1);
        mSeekBar = findViewById(R.id.seekbar);
        tvDuration = findViewById(R.id.duration);
        tvProgress = findViewById(R.id.tv_progress);

        mSelectedCollection.onCreate(savedInstanceState);
        initRecyView();

        mAlbumCollection.onCreate(this, this);
        mAlbumCollection.onRestoreInstanceState(savedInstanceState);
        mAlbumCollection.loadAlbums();

        mAlbumMediaCollection.onCreate(this, this);
    }

    private void initRecyView() {

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        int spacing = getResources().getDimensionPixelSize(R.dimen.media_custom_video_grid_spacing);
        mRecyclerView.addItemDecoration(new MediaGridInset(4, spacing, false));

        customAlbumAdapter = new CustomAlbumAdapter(this);
        mRecyclerView.setAdapter(customAlbumAdapter);

        if (getIntent().hasExtra("class_name")) {
            className = getIntent().getStringExtra("class_name");
        }

        if (getIntent().hasExtra("key_value")){
            objectMap = (Map<String, String>) getIntent().getSerializableExtra("key_value");
        }
        setListener();
    }

    private void setListener() {
        tvCancel.setOnClickListener(view -> finish());
        ivPlay1.setOnClickListener(v -> videoState());
        ivPlay2.setOnClickListener(v -> videoState());

        tvNext.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT_VIDEO_URI, UIUtils.getRealPathFromUri(items.get(selectIndex).uri, this));
            if (!TextUtils.isEmpty(className)) {
                try {
                    ComponentName comp = new ComponentName(this, className);
                    intent.setComponent(comp);
                    intent.setAction("android.intent.action.VIEW");
                    if (objectMap != null) {
                        for (Map.Entry<String, String> entry : objectMap.entrySet()) {
                            intent.putExtra(entry.getKey(), entry.getValue());
                        }
                    }
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                setResult(RESULT_OK, intent);
            }
            finish();
        });

        customAlbumAdapter.setOnSelectedListener(position -> {

            for (int i = 0; i < items.size(); i++) {
                if (i == position) {
                    items.get(i).isChecked = true;
                } else {
                    items.get(i).isChecked = false;
                }
            }
            selectIndex = position;
            customAlbumAdapter.notifyDataSetChanged();
            ivCover.setVisibility(View.GONE);

            //停止播放视频,并且释放
            videoView.stopPlayback();
            //在任何状态下释放媒体播放器
            videoView.suspend();
            handler.removeCallbacks(runnable);

            length = 0;
            setVideoDetails(selectIndex);
            videoState();

        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void videoState() {
        if (videoView.isPlaying()) {
            videoView.pause();

            ivPlay1.setImageResource(R.drawable.matisse_video_play_large);
            ivPlay2.setImageResource(R.drawable.matisse_video_play_small);
            ivPlay1.setVisibility(View.VISIBLE);
        } else {
            videoView.start();
            firstZero = 0;
            handler.postDelayed(runnable,500);
            ivPlay1.setVisibility(View.GONE);
            ivPlay2.setImageResource(R.drawable.matisse_video_stop_small);
            ivCover.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectedCollection.onSaveInstanceState(outState);
        mAlbumCollection.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        mAlbumCollection.onDestroy();
        mAlbumMediaCollection.onDestroy();

        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }

        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public void onAlbumLoad(Cursor cursor) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                cursor.moveToPosition(mAlbumCollection.getCurrentSelection());

                Album album = Album.valueOf(cursor);
                if (album.isAll() && SelectionSpec.getInstance().capture) {
                    album.addCaptureCount();
                }
                mAlbumMediaCollection.load(album, false);
            }
        });
    }

    @Override
    public void onAlbumReset() {
    }

    @Override
    public void onAlbumMediaLoad(Cursor cursor) {
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                Item item = Item.valueOf(cursor);
                if (items.size() == 0) {
                    item.isChecked = true;
                } else {
                    item.isChecked = false;
                }
                items.add(item);
            } while (cursor.moveToNext());

            if (items.size() > 0) {
                customAlbumAdapter.addList(items);

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                SelectionSpec.getInstance().imageEngine.loadThumbnail(this, screenWidth,
                        customAlbumAdapter.mPlaceholder, ivCover, items.get(0).getContentUri());

                setVideoDetails(0);
            }

        }
    }

    /**
     * 设置视频的uri,长度,时间等
     *
     * @param index
     */
    private void setVideoDetails(int index) {
        videoView.setVideoURI(items.get(index).getContentUri());
        length = (int) (items.get(index).duration / 1000);
        mSeekBar.setMax(length);
        mSeekBar.setProgress(0);
        tvDuration.setText(DateUtils.formatElapsedTime(items.get(index).duration / 1000));
    }

    @Override
    public void onAlbumMediaReset() {

    }
}
