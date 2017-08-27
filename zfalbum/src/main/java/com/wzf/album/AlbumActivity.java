package com.wzf.album;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wzf.album.dialog.AlbumListDialog;
import com.wzf.album.dialog.PreviewDialog;
import com.wzf.album.enitity.AlbumEnitity;
import com.wzf.album.enitity.ImageEnitity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlbumActivity extends AppCompatActivity implements
        View.OnClickListener, AlbumDataSource.OnAlbumBuildFinished,
        AlbumListDialog.OnChooseAlbumListener, ImageGridAdapter.OnCheckListener,
        AdapterView.OnItemClickListener, PreviewDialog.OnAlbumPreviewListener {

    // 能选中照片的最大数
    private int maxPhotoCount = Constants.MAX_CHECK_COUNT;

    // 是否是单选 true为单选，false为多选
    private boolean SINGLE_CHECK;

    TextView tvComplete;
    GridView gvImages;
    RelativeLayout layoutBottom;
    View vDialogBg;
    TextView tvPreview;

    // 所有相册列表
    private List<AlbumEnitity> albumList;
    // 当前相册的所有照片列表
    private List<ImageEnitity> photoList;
    // 多选，所有相册已选中的照片列表
    private List<ImageEnitity> photoCheckedList = new ArrayList<>();
    // 多选，Intent传递的数据键值对
    private Map<String, ImageEnitity> mIntentMap = new HashMap<>();

    private ImageGridAdapter mImageGridAdapter;
    private AlbumListDialog mAlbumListDialog;
    private PreviewDialog mPreviewDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initIntents();
        initViews();
        gvImages.setOnItemClickListener(this);
        AlbumDataSource mAlbumDataSource = new AlbumDataSource(this, mIntentMap);
        mAlbumDataSource.execute();
        mAlbumDataSource.setOnAlbumBuildFinishedListener(this);
    }

    private void initIntents() {
        if (getIntent() == null) return;
        // 判断单多选
        SINGLE_CHECK = getIntent().getBooleanExtra(Constants.KEY_IS_SINGLE_MODE, true);
        if (SINGLE_CHECK) return;
        List<ImageEnitity> hasCheckedImages
                = (List<ImageEnitity>) getIntent().getSerializableExtra(Constants.KEY_OLD_IMAGE_LIST);
        if (hasCheckedImages != null) {
            for (int i = 0; i < hasCheckedImages.size(); i++) {
                if (i >= Constants.MAX_CHECK_COUNT) return;
                ImageEnitity enitity = hasCheckedImages.get(i);
                mIntentMap.put(enitity.getPath(), enitity);
            }
        }
    }

    private void initViews() {
        tvComplete = (TextView) findViewById(R.id.tv_complete);
        tvPreview = (TextView) findViewById(R.id.tv_preview);
        gvImages = (GridView) findViewById(R.id.gv_album);
        layoutBottom = (RelativeLayout) findViewById(R.id.layout_album_bottom);
        vDialogBg = findViewById(R.id.v_bg);

        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.layout_album_choose).setOnClickListener(this);
        tvComplete.setOnClickListener(this);
        tvPreview.setOnClickListener(this);

        tvComplete.setVisibility(SINGLE_CHECK ? View.GONE : View.VISIBLE);
        tvPreview.setVisibility(SINGLE_CHECK ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_back) {
            this.finish();
        } else if (i == R.id.layout_album_choose) {
            if (albumList == null) return;
            showAlbumChooseDialog();
        } else if (i == R.id.tv_complete) {
            completeChecked();
        } else if (i == R.id.tv_preview) {
            if (!SINGLE_CHECK)
                showPreviewDialog(photoCheckedList, 0);
        }
    }

    @Override
    public void onDataSourceBuildFinish(List<AlbumEnitity> albumList, List<ImageEnitity> checkedImageList) {
        this.albumList = albumList;
        this.photoList = albumList.get(0).getImageEnitities();
        this.photoCheckedList = checkedImageList;
        refuseAdapter();
        setCheckText();
        setAllowCheckImageCount();
    }

    @Override
    public void onAlbumChoosed(AlbumEnitity albumEnitity) {
        if (albumEnitity == null || mAlbumListDialog == null) return;
        mAlbumListDialog.dismiss();
        this.photoList = albumEnitity.getImageEnitities();
        refuseAdapter();
    }

    /**
     * 适配器切换照片选择回调
     *
     * @param imageListChecked
     */
    @Override
    public void checkPhotoStatus(List<ImageEnitity> imageListChecked) {
        this.photoCheckedList.clear();
        this.photoCheckedList.addAll(imageListChecked);
        setCheckText();
        setAllowCheckImageCount();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (SINGLE_CHECK) {
            Intent intent = new Intent();
            intent.putExtra(Constants.RESULT_SINGLE_ENITITY, photoList.get(position));
            setResult(RESULT_OK, intent);
            this.finish();
        } else {
            showPreviewDialog(photoList, position);
        }
    }

    /**
     * PreviewDialog切换选择事件回调
     *
     * @param imageEnitity
     */
    @Override
    public void toggleCheckForPreview(ImageEnitity imageEnitity) {
        refuseAdapter();
        if (imageEnitity.isSelected()) {
            photoCheckedList.add(imageEnitity);
        } else {
            photoCheckedList.remove(imageEnitity);
        }
        // 更新adapter选中数据源
        if (mImageGridAdapter != null)
            mImageGridAdapter.updateImageListChecked(imageEnitity);
        setCheckText();
        setAllowCheckImageCount();
    }

    /**
     * PreviewDialog完成事件回调
     */
    @Override
    public void onCompleteForPreview() {
        completeChecked();
    }

    private void showAlbumChooseDialog() {
        if (mAlbumListDialog == null) {
            mAlbumListDialog = new AlbumListDialog(this, layoutBottom.getMeasuredHeight(), albumList);
            mAlbumListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    vDialogBg.setVisibility(View.GONE);
                }
            });
            mAlbumListDialog.setOnChooseAlbumListener(this);
        }
        vDialogBg.setVisibility(View.VISIBLE);
        mAlbumListDialog.show();
    }

    private void showPreviewDialog(List<ImageEnitity> previewImgList, int position) {
        if (previewImgList == null || previewImgList.size() == 0) return;
        if (mPreviewDialog == null) {
            mPreviewDialog = new PreviewDialog(this, previewImgList, position);
            mPreviewDialog.setOnAlbumPreviewListener(this);
        } else {
            mPreviewDialog.setPhotoDataSource(previewImgList);
            mPreviewDialog.setCurrentIndex(position);
        }
        setAllowCheckImageCount();
        mPreviewDialog.show();
    }

    private void setCheckText() {
        String previewTxt, completeTxt;
        if (photoCheckedList.size() == 0) {
            previewTxt = "预览";
            completeTxt = "完成";
        } else {
            previewTxt = "预览(" + photoCheckedList.size() + ")";
            completeTxt = "完成(" + photoCheckedList.size() + "/" + maxPhotoCount + ")";
        }
        tvPreview.setText(previewTxt);
        tvComplete.setText(completeTxt);
    }

    private void setAllowCheckImageCount() {
        int allowCheckCount = maxPhotoCount - photoCheckedList.size();
        if (mImageGridAdapter != null)
            mImageGridAdapter.setAllowCheckPhotoCount(allowCheckCount);
        if (mPreviewDialog != null)
            mPreviewDialog.setAllowCheckPhotoCount(allowCheckCount);
    }

    private void completeChecked() {
        if (photoCheckedList.size() == 0) {
            Toast.makeText(this, "请选择照片", Toast.LENGTH_SHORT).show();
            return;
        }
        List<ImageEnitity> mDiffImageList = new ArrayList<>();
        if (mIntentMap.size() == 0) {
            mDiffImageList.addAll(photoCheckedList);
        } else {
            for (ImageEnitity enitity : photoCheckedList) {
                if (mIntentMap.get(enitity.getPath()) == null) {
                    mDiffImageList.add(enitity);
                }
            }
        }
        Intent intent = new Intent();
        intent.putExtra(Constants.RESULT_ENITITY_LIST_ALL, (Serializable) photoCheckedList);
        intent.putExtra(Constants.RESULT_ENITITY_LIST_DIFF, (Serializable) mDiffImageList);
        setResult(RESULT_OK, intent);
        this.finish();
    }

    private void refuseAdapter() {
        if (mImageGridAdapter == null) {
            mImageGridAdapter = new ImageGridAdapter(this, photoList, photoCheckedList, SINGLE_CHECK);
            mImageGridAdapter.setOnCheckListener(this);
            gvImages.setAdapter(mImageGridAdapter);
        } else {
            mImageGridAdapter.changeDataSet(photoList);
        }
    }

    public static void startActivityForResultSingle(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, AlbumActivity.class);
        intent.putExtra(Constants.KEY_IS_SINGLE_MODE, true);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startActivityForResultMultiple(Activity activity, int requestCode) {
        startActivityForResultMultiple(activity, requestCode, new ArrayList<ImageEnitity>());
    }

    public static void startActivityForResultMultiple(Activity activity, int requestCode, ArrayList<ImageEnitity> hasCheckedImages) {
        Intent intent = new Intent(activity, AlbumActivity.class);
        intent.putExtra(Constants.KEY_OLD_IMAGE_LIST, hasCheckedImages);
        intent.putExtra(Constants.KEY_IS_SINGLE_MODE, false);
        activity.startActivityForResult(intent, requestCode);
    }
}
