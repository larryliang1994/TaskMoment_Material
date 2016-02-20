package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.ui.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.ui.activity.TaskInfoActivity;
import com.jiubai.taskmoment.ui.fragment.TimelineFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class TimelineListAdapter extends RecyclerView.Adapter {
    public static ArrayList<Task> taskList = new ArrayList<>();
    private Context mContext;
    private IUploadImagePresenter uploadImagePresenter;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private boolean loading;
    public OnLoadMoreListener onLoadMoreListener;

    public TimelineListAdapter(Context context, boolean isRefresh, String response,
                               IUploadImagePresenter uploadImagePresenter, RecyclerView recyclerView) {
        this.mContext = context;

        if (isRefresh) {
            taskList = new ArrayList<>();
            taskList.clear();
            loading = false;
        }

        if (uploadImagePresenter != null) {
            this.uploadImagePresenter = uploadImagePresenter;
        }

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.clearOnScrollListeners();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView,
                                   int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!loading && totalItemCount <= lastVisibleItem + 1) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    loading = true;
                }
            }
        });

        decodeTaskList(response);
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_timeline, parent, false);

            return new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.load_more_timeline, parent, false);

            return new FooterViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return taskList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (taskList == null || taskList.isEmpty() || position >= taskList.size()) {
            return;
        }

        if (!(viewHolder instanceof ViewHolder)) {
            return;
        }

        ViewHolder holder = (ViewHolder) viewHolder;

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
            intent.putExtra("taskPosition", position);

            mContext.startActivity(intent);
        });

        if (task.getPictures() != null && !task.getPictures().isEmpty()) {
            holder.pictureGridView.setAdapter(new TimelinePictureAdapter(mContext, task.getPictures()));
            UtilBox.setGridViewHeightBasedOnChildren(holder.pictureGridView, true);
        } else {
            holder.pictureGridView.setVisibility(View.GONE);
        }

        if (task.getComments() == null || task.getComments().isEmpty()) {
            holder.commentListView.setVisibility(View.GONE);
        } else {
            holder.commentListView.setVisibility(View.VISIBLE);
            holder.commentListView.setAdapter(new CommentAdapter(mContext, task.getComments(), "timeline"));
            UtilBox.setListViewHeightBasedOnChildren(holder.commentListView);
        }

        holder.commentButton.setOnClickListener(v ->
                TimelineFragment.showCommentWindow(mContext, task.getId(), "", ""));

        if (Config.MID.equals(task.getAuditor()) && "1".equals(task.getAuditResult())) {
            holder.auditButton.setOnClickListener(v -> TimelineFragment.showAuditWindow(mContext, task.getId()));
        } else {
            holder.auditButton.setVisibility(View.GONE);
        }

        switch (task.getSendState()) {
            case Task.SUCCESS:
                holder.sendStateTextView.setTextColor(Color.parseColor("#767676"));
                holder.sendStateTextView.setText(UtilBox.getDateToString(task.getCreateTime(), UtilBox.DATE_TIME));
                break;

            case Task.SENDING:
                holder.sendStateTextView.setTextColor(ContextCompat.getColor(mContext, R.color.clickableText));
                holder.sendStateTextView.setText("发送中...");
                break;

            case Task.FAILED:
                holder.sendStateTextView.setTextColor(ContextCompat.getColor(mContext, R.color.clickableText));
                holder.sendStateTextView.setText("重新发送");
                holder.sendStateTextView.setOnClickListener(v -> {
                    taskList.get(position).setSendState(Task.SENDING);
                    holder.sendStateTextView.setText("发送中...");

                    if (uploadImagePresenter != null) {
                        uploadImagePresenter.doUploadImages(TimelineFragment.pictureList, Constants.DIR_TASK);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
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

    private void decodeTaskList(String response) {
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

                    int taskState;

                    ArrayList<String> pictures;

                    // 如果有任务附图上传中，先显示本地图片
                    if (id.equals(TimelineFragment.taskID) && TimelineFragment.pictureList != null
                            && !TimelineFragment.pictureList.isEmpty()) {
                        pictures = TimelineFragment.pictureList;
                        taskState = Task.SENDING;
                    } else {
                        pictures = decodePictureList(obj.getString("works"));
                        taskState = Task.SUCCESS;
                    }

                    ArrayList<Comment> comments
                            = decodeCommentList(id, obj.getString("member_comment"));

                    long deadline = Long.valueOf(obj.getString("time1")) * 1000;
                    long publish_time = Long.valueOf(obj.getString("time2")) * 1000;
                    long create_time = Long.valueOf(obj.getString("create_time")) * 1000;

                    String audit_result = obj.getString("p2");

                    taskList.add(new Task(id, portraitUrl, nickname, mid, grade, desc,
                            executor, supervisor, auditor,
                            pictures, comments, deadline, publish_time, create_time,
                            audit_result, taskState));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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

        if (pictures != null && !"null".equals(pictures)) {
            try {
                JSONArray jsonArray = new JSONArray(pictures);

                for (int i = 0; i < jsonArray.length(); i++) {
                    pictureList.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.iv_item_portrait) CircleImageView portraitImageView;
        @Bind(R.id.tv_item_nickname) TextView nicknameTextView;
        @Bind(R.id.tv_item_grade) TextView gradeTextView;
        @Bind(R.id.tv_item_desc) TextView descTextView;
        @Bind(R.id.gv_item_picture) GridView pictureGridView;
        @Bind(R.id.lv_item_comment) ListView commentListView;
        @Bind(R.id.btn_item_comment) Button commentButton;
        @Bind(R.id.btn_item_audit) Button auditButton;
        @Bind(R.id.tv_sendState) TextView sendStateTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
