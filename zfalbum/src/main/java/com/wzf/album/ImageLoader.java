package com.wzf.album;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * ================================================
 * 描    述：
 * 作    者：wzf
 * 创建日期：2017/8/13
 * 版    本：1.0
 * 修订历史：
 * ================================================
 */

public class ImageLoader {

    private static RequestOptions myOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(false)  // 禁止内存缓存
            .centerCrop()
            .error(R.drawable.ic_img_error)
            .placeholder(R.drawable.ic_photo_loading);

    public static void load(Context context, ImageView imageView, String imagePath) {
        Glide.with(context)
                .load(imagePath)
                .apply(myOptions)
                .into(imageView);
    }


}
