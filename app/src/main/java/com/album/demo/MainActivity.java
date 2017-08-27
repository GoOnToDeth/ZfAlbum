package com.album.demo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wzf.album.AlbumActivity;
import com.wzf.album.Constants;
import com.wzf.album.ImageLoader;
import com.wzf.album.enitity.ImageEnitity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mScrollView;
    private ImageView mImageView;
    ArrayList<ImageEnitity> imageList = new ArrayList<>();

    int size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScrollView = (LinearLayout) findViewById(R.id.layout_scroll);
        mImageView = (ImageView) findViewById(R.id.iv_img);
        size = dip2px(this, 100);
    }

    public void openAlbumForMulite(View v) {
        AlbumActivity.startActivityForResultMultiple(this, 1);
        // AlbumActivity.startActivityForResultMultiple(this, 1,imageList);
    }

    public void openAlbumForSingle(View v) {
        AlbumActivity.startActivityForResultSingle(this, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            imageList = (ArrayList<ImageEnitity>) data.getSerializableExtra(Constants.RESULT_ENITITY_LIST_ALL);
            // Constants.RESULT_ENITITY_LIST_DIFF获取差异数据列表，(如：原数据ABCD，现数据ACDFG，差异数据则是FG)
            // List<ImageEnitity> diffImageList = (List<ImageEnitity>) data.getSerializableExtra(Constants.RESULT_ENITITY_LIST_DIFF);
            addViews();
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            ImageEnitity imageEnitity = (ImageEnitity) data.getSerializableExtra(Constants.RESULT_SINGLE_ENITITY);
            ImageLoader.load(this, mImageView, imageEnitity.getPath());
        }
    }

    private void addViews() {
        if (imageList == null) return;
        mScrollView.removeAllViews();
        for (ImageEnitity mImageEnitity : imageList) {
            ImageView imageView = getImageView();
            ImageLoader.load(this, imageView, mImageEnitity.getPath());
            mScrollView.addView(imageView);
        }
    }

    private ImageView getImageView() {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(10, 0, 10, 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(params);
        return imageView;
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
