package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.bean.News;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.ui.activity.TaskInfoActivity;
import com.jiubai.taskmoment.widget.RippleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息列表适配器
 */
public class NewsAdapter extends BaseAdapter {
    private Context context;
    private List<News> newsList;

    public NewsAdapter(Context context, List<News> newsList) {
        if (newsList != null) {
            this.newsList = newsList;
        } else {
            this.newsList = new ArrayList<>();
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_news, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ((RippleView) convertView.findViewById(R.id.rv_item_news))
                .setOnRippleCompleteListener(
                        rippleView -> {
                            Intent intent = new Intent(context, TaskInfoActivity.class);
                            intent.putExtra("task", newsList.get(position).getTask());

                            context.startActivity(intent);
                        }
                );

        final News news = newsList.get(position);
        final Task task = news.getTask();

        ImageLoader.getInstance().displayImage(
                Urls.MEDIA_CENTER_PORTRAIT + news.getSenderID() + ".jpg" + "?t=" +Config.TIME,
                holder.iv_portrait);

        holder.tv_sender.setText(news.getTitle());

        holder.tv_content.setText(news.getContent());

        holder.tv_time.setText(news.getTime());

        if (task.getPictures() != null && !task.getPictures().isEmpty()) {
            ImageLoader.getInstance().displayImage(task.getPictures().get(0), holder.iv_picture);
        } else {
            holder.iv_picture.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView iv_portrait;
        TextView tv_sender;
        TextView tv_content;
        TextView tv_time;
        ImageView iv_picture;

        public ViewHolder(View itemView) {
            iv_portrait = (ImageView) itemView.findViewById(R.id.iv_portrait);
            tv_sender = (TextView) itemView.findViewById(R.id.tv_sender);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            iv_picture = (ImageView) itemView.findViewById(R.id.iv_picture);
        }
    }
}