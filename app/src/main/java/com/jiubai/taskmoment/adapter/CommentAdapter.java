package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.common.ClickableText;
import com.jiubai.taskmoment.ui.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.ui.activity.TaskInfoActivity;
import com.jiubai.taskmoment.ui.fragment.TimelineFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论列表适配器
 */
public class CommentAdapter extends BaseAdapter {
    private Context context;
    public List<Comment> commentList;
    public String which;

    public CommentAdapter(Context context, List<Comment> commentList, String which) {
        if (commentList != null) {
            this.commentList = commentList;
        } else {
            this.commentList = new ArrayList<>();
        }
        this.context = context;
        this.which = which;
    }

    @Override
    public int getCount() {
        return commentList.size();
    }

    @Override
    public Object getItem(int position) {
        return commentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_comment, null);

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Comment comment = commentList.get(position);

        // 发送者可点击
        final SpannableString senderSpan = new SpannableString(comment.getSender());
        ClickableSpan senderClickableSpan = new ClickableText() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(context, PersonalTimelineActivity.class);
                intent.putExtra("mid", comment.getSenderId());
                context.startActivity(intent);
            }
        };
        senderSpan.setSpan(senderClickableSpan, 0, comment.getSender().length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        holder.tv_comment.setText(senderSpan);

        // 接收者可点击
        if (comment.getReceiver() != null && !comment.getReceiver().isEmpty()) {
            SpannableString receiverSpan = new SpannableString(comment.getReceiver());
            ClickableSpan receiverClickableSpan = new ClickableText() {
                @Override
                public void onClick(View widget) {
                    Intent intent = new Intent(context, PersonalTimelineActivity.class);
                    intent.putExtra("mid", comment.getReceiverId());
                    context.startActivity(intent);
                }
            };
            receiverSpan.setSpan(receiverClickableSpan, 0, comment.getReceiver().length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            holder.tv_comment.append("回复");
            holder.tv_comment.append(receiverSpan);
        }

        // 拼接
        holder.tv_comment.append("：");
        holder.tv_comment.append(comment.getContent());
        holder.tv_comment.setMovementMethod(LinkMovementMethod.getInstance());

        holder.tv_comment.setOnClickListener(v -> {

            if (!Config.MID.equals(comment.getSenderId())) {
                if ("timeline".equals(which)) {
                    TimelineFragment.showCommentWindow(context,
                            comment.getTaskId(), comment.getSender(), comment.getSenderId());
                } else if ("taskInfo".equals(which)) {
                    TaskInfoActivity.showCommentWindow(context,
                            comment.getTaskId(), comment.getSender(), comment.getSenderId());
                } else {
                    PersonalTimelineActivity.showCommentWindow(context,
                            comment.getTaskId(), comment.getSender(), comment.getSenderId());
                }
            } else {
                if ("timeline".equals(which)) {
                    TimelineFragment.showCommentWindow(context, comment.getTaskId(), "", "");
                } else if ("taskInfo".equals(which)) {
                    TaskInfoActivity.showCommentWindow(context, comment.getTaskId(), "", "");
                } else {
                    PersonalTimelineActivity.showCommentWindow(context,
                            comment.getTaskId(), "", "");
                }
            }

            holder.tv_comment.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.gray));

            new Handler().postDelayed(() -> holder.tv_comment.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.transparent)), 100);
        });

        // 将其宽度约束为ListView的宽度
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        holder.tv_comment.setWidth(UtilBox.dip2px(
                context, UtilBox.px2dip(context, metric.widthPixels) - 81));

        // 最后一项离底部为5dp
        if (position == getCount() - 1) {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) holder.tv_comment.getLayoutParams();
            params.setMargins(UtilBox.dip2px(context, 5), UtilBox.dip2px(context, 5),
                    UtilBox.dip2px(context, 5), UtilBox.dip2px(context, 5));
            holder.tv_comment.setLayoutParams(params);
        }

        return convertView;
    }

    private class ViewHolder {
        TextView tv_comment;

        public ViewHolder(View view) {
            tv_comment = (TextView) view.findViewById(R.id.tv_comment);
        }
    }
}