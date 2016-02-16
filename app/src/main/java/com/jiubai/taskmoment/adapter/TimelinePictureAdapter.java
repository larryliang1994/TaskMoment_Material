package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.ui.activity.CheckPictureActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.util.ArrayList;

/**
 * Timeline中的Picture的适配器
 */
public class TimelinePictureAdapter extends BaseAdapter {
    public ArrayList<String> pictureList;
    private Context context;

    public TimelinePictureAdapter(Context context, ArrayList<String> pictureList) {
        this.context = context;
        this.pictureList = pictureList;
    }

    @Override
    public int getCount() {
        return pictureList.size();
    }

    @Override
    public Object getItem(int position) {
        return pictureList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_picture, null);

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int pictureSize = ((UtilBox.getWidthPixels(context)) - UtilBox.dip2px(context, 93)) / 3;

        // 适配图片大小
        ViewGroup.LayoutParams params = holder.iv_picture.getLayoutParams();
        params.width = pictureSize;
        params.height = pictureSize;
        holder.iv_picture.setLayoutParams(params);

        holder.iv_picture.setOnClickListener(v -> {
            Intent intent = new Intent(context, CheckPictureActivity.class);

            intent.putStringArrayListExtra("pictureList", pictureList);
            intent.putExtra("index", position);
            intent.putExtra("fromWhere", "net");

            context.startActivity(intent);

            ((Activity) context).overridePendingTransition(
                    R.anim.zoom_in_quick, R.anim.scale_stay);
        });

        if (!pictureList.get(position).contains("http")) {
            String imgUrl = ImageDownloader.Scheme.FILE.wrap(pictureList.get(position));
            ImageLoader.getInstance().displayImage(imgUrl, holder.iv_picture);
        } else {
            ImageLoader.getInstance().displayImage(
                    UtilBox.getThumbnailImageName(pictureList.get(position),
                    pictureSize, pictureSize), holder.iv_picture);
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView iv_picture;

        public ViewHolder(View view) {
            iv_picture = (ImageView) view.findViewById(R.id.iv_picture);
        }
    }
}