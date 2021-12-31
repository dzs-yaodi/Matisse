package com.sobey.matisse.ui;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
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
    private TextView tvNext;
    private SelectedItemCollection mSelectedCollection = new SelectedItemCollection(this);
    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();
    private CustomAlbumAdapter customAlbumAdapter;
    private List<Item> items = new ArrayList<>();
    //选中的视频
    private int selectIndex = 0;
    //视频长度
    private int length = 0;
    //需要跳转的页面地址
    private String className;
    private Map<String,String> objectMap = new HashMap<>();

    private StandardGSYVideoPlayer player;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.sobey_activity_custom_video);
        //EXOPlayer内核
        PlayerFactory.setPlayManager(SystemPlayerManager.class);
        //设置沉浸式状态栏
        UIUtils.translucentStatusBar(this, true);
        //计算出状态栏高度，并设置view留出对应位置
        int height = (int) UIUtils.getStatusBarHeight(this);
        View bgView = findViewById(R.id.title_container);
        bgView.setPadding(0, height, 0, 0);
        bgView.setBackgroundColor(Color.BLACK);

        mRecyclerView = findViewById(R.id.recyclerView);
        tvNext = findViewById(R.id.tv_next);

        player = findViewById(R.id.player);
        imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        player.getFullscreenButton().setVisibility(View.GONE);
        player.getBackButton().setVisibility(View.VISIBLE);
        player.getBackButton().setOnClickListener(v -> {
            onBackPressed();
        });

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

            Uri uri = items.get(selectIndex).getContentUri();
            player.setUp(getRealFilePath(this,uri),false,"");
            player.startPlayLogic();

        });
    }

    public String getRealFilePath(final Context context, final Uri uri ) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    private void videoState() {

//        if (videoView.isPlaying()) {
//            videoView.pause();
//
//            ivPlay1.setImageResource(R.drawable.matisse_video_play_large);
//            ivPlay2.setImageResource(R.drawable.matisse_video_play_small);
//            ivPlay1.setVisibility(View.VISIBLE);
//        } else {
//            videoView.start();
//            firstZero = 0;
//            handler.postDelayed(runnable,500);
//            ivPlay1.setVisibility(View.GONE);
//            ivPlay2.setImageResource(R.drawable.matisse_video_stop_small);
//            ivCover.setVisibility(View.GONE);
//        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSelectedCollection.onSaveInstanceState(outState);
        mAlbumCollection.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        player.onVideoPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        player.setVideoAllCallBack(null);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mAlbumCollection.onDestroy();
        mAlbumMediaCollection.onDestroy();
        GSYVideoManager.releaseAllVideos();

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
                        customAlbumAdapter.mPlaceholder, imageView, items.get(0).getContentUri());

                Uri uri = items.get(0).getContentUri();
                player.setUp(getRealFilePath(this,uri),false,"");
//                setVideoDetails(0);
            }

        }
    }

    @Override
    public void onAlbumMediaReset() {

    }

}
