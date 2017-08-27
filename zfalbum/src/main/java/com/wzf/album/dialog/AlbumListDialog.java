package com.wzf.album.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wzf.album.ImageLoader;
import com.wzf.album.R;
import com.wzf.album.utils.ScreenUtils;
import com.wzf.album.enitity.AlbumEnitity;

import java.util.List;

/**
 * ===============================
 * 描    述：
 * 作    者：wzf
 * 创建日期：2017/8/11 10:43
 * ===============================
 */
public class AlbumListDialog extends Dialog implements AdapterView.OnItemClickListener {

    private final long duration = 300;

    private Context mContext;

    private View rootView;
    protected Animation mInnerShowAnim;
    protected Animation mInnerDismissAnim;

    private int offsetX = 0;
    private int offsetY = 0;

    private ListView lvAlbum;

    private OnChooseAlbumListener listener;
    private List<AlbumEnitity> albumEnitities;
    private AlbumAdapter albumAdapter;
    private int selectedIndex;

    public AlbumListDialog(@NonNull Context context, int offsetY, List<AlbumEnitity> albumEnitities) {
        super(context);
        initDialogTheme();
        this.offsetY = offsetY;
        this.albumEnitities = albumEnitities;
        selectedIndex = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = getContext();
        rootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_album, null);
        lvAlbum = (ListView) rootView.findViewById(R.id.lv_dialog_album);
        lvAlbum.setOnItemClickListener(this);
        bindAdapter();
        setContentView(rootView);
    }

    private void bindAdapter() {
        if (albumEnitities == null) return;
        if (albumAdapter == null) {
            albumAdapter = new AlbumAdapter();
            lvAlbum.setAdapter(albumAdapter);
        } else {
            albumAdapter.notifyDataSetChanged();
        }
    }

    private void initDialogTheme() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// android:windowNoTitle
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));// android:windowBackground
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);// android:backgroundDimEnabled默认是true的
    }

    @Override
    public void onAttachedToWindow() {
        FrameLayout.LayoutParams paramsRootView = (FrameLayout.LayoutParams) rootView.getLayoutParams();
        paramsRootView.width = ScreenUtils.getScreenWidth(mContext);
        paramsRootView.height = FrameLayout.LayoutParams.WRAP_CONTENT;

        Window window = getWindow();
        WindowManager.LayoutParams paramsWindow = window.getAttributes();
        window.setGravity(Gravity.BOTTOM);
        paramsWindow.x = offsetX;
        paramsWindow.y = offsetY;
        paramsWindow.dimAmount = 0f;
        window.setAttributes(paramsWindow);

        // 开始入场动画
        Animation animation = onAttachedAnimation();
        rootView.startAnimation(animation);
    }

    @Override
    public void dismiss() {
        Animation animation = onDetachedAnimation();
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                AlbumListDialog.super.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rootView.startAnimation(animation);
    }

    private Animation onDetachedAnimation() {
        if (mInnerDismissAnim == null) {
            mInnerDismissAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
            mInnerDismissAnim.setDuration(duration);
        }
        return mInnerDismissAnim;
    }

    private Animation onAttachedAnimation() {
        if (mInnerShowAnim == null) {
            mInnerShowAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
            mInnerShowAnim.setDuration(duration);
        }
        return mInnerShowAnim;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (listener == null) return;
        selectedIndex = position;
        bindAdapter();
        listener.onAlbumChoosed(albumEnitities.get(position));
    }

    public void setOnChooseAlbumListener(OnChooseAlbumListener listener) {
        this.listener = listener;
    }

    /**
     * 选中某一个相册的回调
     */
    public interface OnChooseAlbumListener {
        void onAlbumChoosed(AlbumEnitity albumEnitity);
    }

    class AlbumAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public AlbumAdapter() {
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return albumEnitities.size();
        }

        @Override
        public Object getItem(int i) {
            return albumEnitities.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = inflater.inflate(R.layout.item_album_dialog, null);
                viewHolder.ivTop = (ImageView) view.findViewById(R.id.iv_img);
                viewHolder.ivDian = (ImageView) view.findViewById(R.id.iv_right);
                viewHolder.tvAlbumName = (TextView) view.findViewById(R.id.tv_album_name);
                viewHolder.tvCount = (TextView) view.findViewById(R.id.tv_count);
                view.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) view.getTag();
            AlbumEnitity item = albumEnitities.get(position);
            ImageLoader.load(mContext, viewHolder.ivTop, item.getTopImageEnitity().getPath());
            viewHolder.tvAlbumName.setText(item.getAlbumName());
            viewHolder.tvCount.setText(item.getImageCounts() + "张");
            if (selectedIndex == position)
                viewHolder.ivDian.setVisibility(View.VISIBLE);
            else
                viewHolder.ivDian.setVisibility(View.GONE);
            return view;
        }

        class ViewHolder {

            private ImageView ivTop;
            private ImageView ivDian;
            private TextView tvAlbumName;
            private TextView tvCount;

            public ViewHolder() {
            }
        }
    }
}
