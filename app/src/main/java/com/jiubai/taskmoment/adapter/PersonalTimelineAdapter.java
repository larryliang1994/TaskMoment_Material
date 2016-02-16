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
    public CommentAdapter commentAdapter;
    private Context context;

    public PersonalTimelineAdapter(Context context, boolean isRefresh, String response) {
        this.context = context;

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

    /**
     * 设置任务等级的颜色
     *
     * @param tv_grade 需要设置的TextView
     * @param grade    级别
     */
    private void setGradeColor(TextView tv_grade, String grade) {
        switch (grade) {
            case "S":
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.S));
                break;

            case "A":
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.A));
                break;

            case "B":
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.B));
                break;

            case "C":
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.C));
                break;

            case "D":
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.D));
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_timeline, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Task task = taskList.get(position);
        holder.tv_nickname.setText(task.getNickname());
        holder.tv_grade.setText(task.getGrade());
        setGradeColor(holder.tv_grade, task.getGrade());
        holder.tv_desc.setText(task.getDesc());

        ImageLoader.getInstance().displayImage(
                UtilBox.getThumbnailImageName(task.getPortraitUrl(),
                        UtilBox.dip2px(context, 45),
                        UtilBox.dip2px(context, 45))
                        + "?t=" + Config.TIME, holder.iv_portrait);

        holder.iv_portrait.setOnClickListener(v -> {
            Intent intent = new Intent(context, PersonalTimelineActivity.class);
            intent.putExtra("mid", task.getMid());
            context.startActivity(intent);
        });

        holder.tv_nickname.setOnClickListener(v -> {
            Intent intent = new Intent(context, PersonalTimelineActivity.class);
            intent.putExtra("mid", task.getMid());
            context.startActivity(intent);
        });

        holder.tv_desc.setOnClickListener(v -> {
            holder.tv_desc.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.gray));

            new Handler().postDelayed(() -> holder.tv_desc.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.transparent)), 100);

            Intent intent = new Intent(context, TaskInfoActivity.class);
            intent.putExtra("task", task);

            context.startActivity(intent);
        });

        if(task.getPictures() == null || task.getPictures().isEmpty()){
            holder.gv_picture.setVisibility(View.GONE);
        } else {
            holder.gv_picture.setAdapter(new TimelinePictureAdapter(context, task.getPictures()));
            UtilBox.setGridViewHeightBasedOnChildren(holder.gv_picture, true);
        }

        if (task.getComments() == null || task.getComments().isEmpty()) {
            holder.lv_comment.setVisibility(View.GONE);
        } else {
            holder.lv_comment.setVisibility(View.VISIBLE);
            commentAdapter = new CommentAdapter(context, task.getComments(), "personal");
            holder.lv_comment.setAdapter(commentAdapter);
            UtilBox.setListViewHeightBasedOnChildren(holder.lv_comment);
        }

        holder.btn_comment.setOnClickListener(v ->
                PersonalTimelineActivity.showCommentWindow(context, task.getId(), "", ""));

        if (Config.MID.equals(task.getAuditor()) && "1".equals(task.getAuditResult())) {
            holder.btn_audit.setOnClickListener(v ->
                    PersonalTimelineActivity.showAuditWindow(context, task.getId()));
        } else {
            holder.btn_audit.setVisibility(View.GONE);
        }

        holder.tv_sendState.setTextColor(Color.parseColor("#767676"));
        holder.tv_sendState.setText(UtilBox.getDateToString(task.getCreateTime(), UtilBox.DATE_TIME));

        return convertView;
    }

    public static class ViewHolder {

        @Bind(R.id.iv_item_portrait) ImageView iv_portrait;
        @Bind(R.id.tv_item_nickname) TextView tv_nickname;
        @Bind(R.id.tv_item_grade) TextView tv_grade;
        @Bind(R.id.tv_item_desc) TextView tv_desc;
        @Bind(R.id.gv_item_picture) GridView gv_picture;
        @Bind(R.id.lv_item_comment) ListView lv_comment;
        @Bind(R.id.btn_item_comment) Button btn_comment;
        @Bind(R.id.btn_item_audit) Button btn_audit;
        @Bind(R.id.tv_sendState) TextView tv_sendState;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}