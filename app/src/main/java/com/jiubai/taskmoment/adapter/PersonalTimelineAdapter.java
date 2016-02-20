package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.ui.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.ui.activity.TaskInfoActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 个人时间线适配器
 */
@SuppressLint("InflateParams")
public class PersonalTimelineAdapter extends BaseAdapter {
    public static ArrayList<Task> taskList;
    private Context mContext;

    public PersonalTimelineAdapter(Context context, boolean isRefresh, String response) {
        this.mContext = context;

        if (isRefresh) {
            taskList = new ArrayList<>();
        }

        try {
            JSONObject taskJson = new JSONObject(response);

            if (!"null".equals(taskJson.getString("info"))) {
                JSONArray taskArray = taskJson.getJSONArray("info");

                for (int i = 0; i < taskArray.length(); i++) {
                    JSONObject obj = new JSONObject(taskArray.getString(i));

                    String id = obj.getString("id");

                    String mid = obj.getString("mid");
                    String portraitUrl = Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg";

                    String nickname = obj.getString("show_name");

                    char p1 = obj.getString("p1").charAt(0);
                    String grade = (p1 - 48) == 1 ? "S" : String.valueOf((char) (p1 + 15));

                    String desc = obj.getString("comments");
                    String executor = obj.getString("ext1");
                    String supervisor = obj.getString("ext2");
                    String auditor = obj.getString("ext3");

                    ArrayList<String> pictures = decodePictureList(obj.getString("works"));
                    ArrayList<Comment> comments
                            = decodeCommentList(id, obj.getString("member_comment"));

                    long deadline = Long.valueOf(obj.getString("time1")) * 1000;
                    long publish_time = Long.valueOf(obj.getString("time2")) * 1000;
                    long create_time = Long.valueOf(obj.getString("create_time")) * 1000;

                    String audit_result = obj.getString("p2");

                    Task task = new Task(id, portraitUrl, nickname, mid, grade, desc,
                            executor, supervisor, auditor,
                            pictures, comments, deadline, publish_time, create_time,
                            audit_result, Task.SUCCESS);
                    taskList.add(task);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static int getTaskPositionWithID(String taskID) {
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            if (task.getId().equals(taskID)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 设置任务等级的颜色
     *
     * @param tv_grade 需要设置的TextView
     * @param grade    级别
     */
    private void setGradeColor(TextView tv_grade, String grade) {
        switch (grade) {
            case "S":
                tv_grade.setTextColor(ContextCompat.getColor(mContext, R.color.S));
                break;

            case "A":
                tv_grade.setTextColor(ContextCompat.getColor(mContext, R.color.A));
                break;

            case "B":
                tv_grade.setTextColor(ContextCompat.getColor(mContext, R.color.B));
                break;

            case "C":
                tv_grade.setTextColor(ContextCompat.getColor(mContext, R.color.C));
                break;

            case "D":
                tv_grade.setTextColor(ContextCompat.getColor(mContext, R.color.D));
                break;
        }
    }

    /**
     * 将json解码成list
     *
     * @param pictures 图片Json
     * @return 图片list
     */
    private ArrayList<String> decodePictureList(String pictures) {
        ArrayList<String> pictureList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(pictures);

            for (int i = 0; i < jsonArray.length(); i++) {
                pictureList.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pictureList;
    }

    /**
     * 将json解码成list
     *
     * @param comments 评论json
     * @return 图片List
     */
    private ArrayList<Comment> decodeCommentList(String taskID, String comments) {
        ArrayList<Comment> commentList = new ArrayList<>();

        if (!"".equals(comments) && !"null".equals(comments)) {
            try {

                JSONArray jsonArray = new JSONArray(comments);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = new JSONObject(jsonArray.getString(i));

                    String sender = "null".equals(object.getString("send_real_name")) ?
                            object.getString("send_mobile") : object.getString("send_real_name");

                    String receiver = "null".equals(object.getString("receiver_real_name")) ?
                            object.getString("receiver_mobile") : object.getString("receiver_real_name");

                    if ("null".equals(receiver)) {
                        Comment comment = new Comment(taskID,
                                sender, object.getString("send_id"),
                                object.getString("content"),
                                Long.valueOf(object.getString("create_time")) * 1000);

                        commentList.add(comment);
                    } else {
                        Comment comment = new Comment(taskID,
                                sender, object.getString("send_id"),
                                receiver, object.getString("receiver_id"),
                                object.getString("content"),
                                Long.valueOf(object.getString("create_time")) * 1000);

                        commentList.add(comment);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return commentList;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_timeline, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Task task = taskList.get(position);
        holder.nicknameTextView.setText(task.getNickname());
        holder.gradeTextView.setText(task.getGrade());
        setGradeColor(holder.gradeTextView, task.getGrade());
        holder.descTextView.setText(task.getDesc());

        ImageLoader.getInstance().displayImage(
                UtilBox.getThumbnailImageName(task.getPortraitUrl(),
                        UtilBox.dip2px(mContext, 45),
                        UtilBox.dip2px(mContext, 45))
                        + "?t=" + Config.TIME, holder.portraitImageView);

        holder.portraitImageView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, PersonalTimelineActivity.class);
            intent.putExtra("mid", task.getMid());
            mContext.startActivity(intent);
        });

        holder.nicknameTextView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, PersonalTimelineActivity.class);
            intent.putExtra("mid", task.getMid());
            mContext.startActivity(intent);
        });

        holder.descTextView.setOnClickListener(v -> {
            holder.descTextView.setBackgroundColor(
                    ContextCompat.getColor(mContext, R.color.gray));

            new Handler().postDelayed(() -> holder.descTextView.setBackgroundColor(
                    ContextCompat.getColor(mContext, R.color.transparent)), 100);

            Intent intent = new Intent(mContext, TaskInfoActivity.class);
            intent.putExtra("task", task);

            mContext.startActivity(intent);
        });

        if(task.getPictures() == null || task.getPictures().isEmpty()){
            holder.pictureGridView.setVisibility(View.GONE);
        } else {
            holder.pictureGridView.setAdapter(new TimelinePictureAdapter(mContext, task.getPictures()));
            UtilBox.setGridViewHeightBasedOnChildren(holder.pictureGridView, true);
        }

        if (task.getComments() == null || task.getComments().isEmpty()) {
            holder.commentListView.setVisibility(View.GONE);
        } else {
            holder.commentListView.setVisibility(View.VISIBLE);
            holder.commentListView.setAdapter(new CommentAdapter(mContext, task.getComments(), "personal"));
            UtilBox.setListViewHeightBasedOnChildren(holder.commentListView);
        }

        holder.commentButton.setOnClickListener(v ->
                PersonalTimelineActivity.showCommentWindow(mContext, task.getId(), "", ""));

        if (Config.MID.equals(task.getAuditor()) && "1".equals(task.getAuditResult())) {
            holder.auditButton.setOnClickListener(v ->
                    PersonalTimelineActivity.showAuditWindow(mContext, task.getId()));
        } else {
            holder.auditButton.setVisibility(View.GONE);
        }

        holder.sendStateTextView.setTextColor(Color.parseColor("#767676"));
        holder.sendStateTextView.setText(UtilBox.getDateToString(task.getCreateTime(), UtilBox.DATE_TIME));

        return convertView;
    }

    public static class ViewHolder {

        @Bind(R.id.iv_item_portrait) ImageView portraitImageView;
        @Bind(R.id.tv_item_nickname) TextView nicknameTextView;
        @Bind(R.id.tv_item_grade) TextView gradeTextView;
        @Bind(R.id.tv_item_desc) TextView descTextView;
        @Bind(R.id.gv_item_picture) GridView pictureGridView;
        @Bind(R.id.lv_item_comment) ListView commentListView;
        @Bind(R.id.btn_item_comment) Button commentButton;
        @Bind(R.id.btn_item_audit) Button auditButton;
        @Bind(R.id.tv_sendState) TextView sendStateTextView;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}