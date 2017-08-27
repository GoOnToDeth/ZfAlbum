package com.wzf.album.dialog;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wzf.album.Constants;
import com.wzf.album.ImageLoader;
import com.wzf.album.R;
import com.wzf.album.utils.ScreenUtils;
import com.wzf.album.enitity.ImageEnitity;
import com.wzf.album.utils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 描    述：
 * 作    者：wzf
 * ================================================
 */

public class PreviewDialog extends Dialog implements View.OnClickListener {

    private Context mContext;

    private View rootView;
    private ViewPager mViewPager;
    private RelativeLayout layoutTitle;
    private RelativeLayout layoutBottom;
    private LinearLayout layoutToggle;
    private TextView tvTitle;
    private ImageView ivCheck;

    // 全部数据源
    private List<ImageEnitity> totalPhotoList = new ArrayList<>();
    // 当前选中的图片position
    private int currentIndex;
    // 最多能选择的照片数量
    private int maxCheckPhotoCount;
    // 还能选择的照片数量
    private int allowCheckPhotoCount;

    // 标题和底部动画时间
    private final long duration = 100;
    // 是否已经隐藏标题
    private boolean isShow = true;

    private BannerAdapter bannerAdapter;
    private OnAlbumPreviewListener albumPreviewListener;

    public PreviewDialog(Context context, List<ImageEnitity> photoList, int currentIndex) {
        super(context);
        this.mContext = context;
        this.totalPhotoList.clear();
        this.totalPhotoList.addAll(photoList);
        this.currentIndex = currentIndex;
        this.maxCheckPhotoCount = Constants.MAX_CHECK_COUNT;
        setDialogTheme();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = getContext();
        rootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_album_preview, null);
        mViewPager = (ViewPager) rootView.findViewById(R.id.vp_image);
        tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
        layoutToggle = (LinearLayout) rootView.findViewById(R.id.layout_preview_check);
        layoutBottom = (RelativeLayout) rootView.findViewById(R.id.layout_preview_bottom);
        layoutTitle = (RelativeLayout) rootView.findViewById(R.id.layout_title);
        ivCheck = (ImageView) rootView.findViewById(R.id.iv_toggle_check);
        rootView.findViewById(R.id.iv_back).setOnClickListener(this);
        rootView.findViewById(R.id.tv_complete).setOnClickListener(this);
        layoutToggle.setOnClickListener(this);
        setTitle();
        bindAdapter();
        mViewPager.setCurrentItem(currentIndex);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                setTitle();
                setCheckStatus(totalPhotoList.get(position).isSelected());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setContentView(rootView);
    }

    private void setTitle() {
        tvTitle.setText((currentIndex + 1) + "/" + totalPhotoList.size());
    }

    private void bindAdapter() {
        if (bannerAdapter == null) {
            bannerAdapter = new BannerAdapter(totalPhotoList);
            mViewPager.setAdapter(bannerAdapter);
        } else {
            bannerAdapter.notifyDataSetChanged();
        }
    }

    private void setDialogTheme() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// android:windowNoTitle
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));// android:windowBackground
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);// android:backgroundDimEnabled默认是true的
    }

    @Override
    public void onAttachedToWindow() {
        if (totalPhotoList.size() > currentIndex)
            setCheckStatus(totalPhotoList.get(currentIndex).isSelected());
        FrameLayout.LayoutParams paramsRootView = (FrameLayout.LayoutParams) rootView.getLayoutParams();
        paramsRootView.width = ScreenUtils.getScreenWidth(mContext);
        paramsRootView.height = ScreenUtils.getScreenHeight(mContext) - StatusBarUtils.getHeight(mContext);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.iv_back) {
            this.dismiss();
        } else if (i == R.id.tv_complete) {
            if (albumPreviewListener != null)
                albumPreviewListener.onCompleteForPreview();
        } else if (i == R.id.layout_preview_check) {
            ImageEnitity imageEnitity = totalPhotoList.get(mViewPager.getCurrentItem());
            if (!imageEnitity.isSelected() && allowCheckPhotoCount == 0) {
                Toast.makeText(mContext, "最多选择" + maxCheckPhotoCount + "张照片", Toast.LENGTH_SHORT).show();
                return;
            }
            imageEnitity.setSelected(!imageEnitity.isSelected());
            setCheckStatus(imageEnitity.isSelected());
            if (albumPreviewListener != null) {
                albumPreviewListener.toggleCheckForPreview(imageEnitity);
            }
        }
    }

    /**
     * 设置选中状态
     *
     * @param isCheck
     */
    private void setCheckStatus(boolean isCheck) {
        if (isCheck) {
            ivCheck.setImageResource(R.drawable.ic_photo_checked2);
        } else {
            ivCheck.setImageResource(R.drawable.ic_photo_uncheck2);
        }
    }

    private void toggleAnimator() {
        if (isShow) {
            ObjectAnimator
                    .ofFloat(layoutTitle, "translationY", 0, -layoutTitle.getMeasuredHeight())
                    .setDuration(duration)
                    .start();
            ObjectAnimator
                    .ofFloat(layoutBottom, "translationY", 0, layoutBottom.getMeasuredHeight())
                    .setDuration(duration)
                    .start();
        } else {
            ObjectAnimator
                    .ofFloat(layoutTitle, "translationY", -layoutTitle.getMeasuredHeight(), 0)
                    .setDuration(duration)
                    .start();
            ObjectAnimator
                    .ofFloat(layoutBottom, "translationY", layoutBottom.getMeasuredHeight(), 0)
                    .setDuration(duration)
                    .start();
        }
        isShow = !isShow;
    }

    public void setAllowCheckPhotoCount(int allowCheckPhotoCount) {
        this.allowCheckPhotoCount = allowCheckPhotoCount;
    }

    public void setPhotoDataSource(List<ImageEnitity> photoList) {
        this.totalPhotoList.clear();
        this.totalPhotoList.addAll(photoList);
        bindAdapter();
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
        if (mViewPager != null) {
            mViewPager.setCurrentItem(currentIndex);
            setTitle();
        }
    }

    class BannerAdapter extends PagerAdapter implements View.OnClickListener {

        private LayoutInflater inflater;

        public BannerAdapter(List<ImageEnitity> photoList) {
//            this.photoList = photoList;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return totalPhotoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.item_dialog_image, container, false);
            ImageView photoView = (ImageView) view.findViewById(R.id.image);
            photoView.setOnClickListener(this);
            ImageLoader.load(mContext, photoView, totalPhotoList.get(position).getPath());
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void onClick(View v) {
            toggleAnimator();
        }
    }

    public void setOnAlbumPreviewListener(OnAlbumPreviewListener albumPreviewListener) {
        this.albumPreviewListener = albumPreviewListener;
    }

    /**
     * 切换选中状态和完成按钮监听
     */
    public interface OnAlbumPreviewListener {
        void toggleCheckForPreview(ImageEnitity imageEnitity);

        void onCompleteForPreview();
    }
}
