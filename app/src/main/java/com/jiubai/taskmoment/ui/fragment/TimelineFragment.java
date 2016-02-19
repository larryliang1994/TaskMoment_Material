package com.jiubai.taskmoment.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.TimelineListAdapter;
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.News;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.AuditPresenterImpl;
import com.jiubai.taskmoment.presenter.CommentPresenterImpl;
import com.jiubai.taskmoment.presenter.IAuditPresenter;
import com.jiubai.taskmoment.presenter.ICommentPresenter;
import com.jiubai.taskmoment.presenter.ITaskPresenter;
import com.jiubai.taskmoment.presenter.ITimelinePresenter;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.presenter.TaskPresenterImpl;
import com.jiubai.taskmoment.presenter.TimelinePresenterImpl;
import com.jiubai.taskmoment.presenter.UploadImagePresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.ui.iview.IAuditView;
import com.jiubai.taskmoment.ui.iview.ICommentView;
import com.jiubai.taskmoment.ui.iview.ITaskView;
import com.jiubai.taskmoment.ui.iview.ITimelineView;
import com.jiubai.taskmoment.ui.iview.IUploadImageView;
import com.jiubai.taskmoment.widget.DividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 时间线（任务圈）
 */
public class TimelineFragment extends Fragment implements ITimelineView, IUploadImageView,
        ICommentView, ITaskView, IAuditView {

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private static AppBarLayout appBarLayout;
    private static TimelineListAdapter adapter;
    private static ICommentPresenter commentPresenter;
    private static IAuditPresenter auditPresenter;

    public static LinearLayout ll_comment;
    public static LinearLayout ll_audit;
    public static ArrayList<String> pictureList; // 供任务附图上传中时使用
    public static String taskID;
    public static boolean commentWindowIsShow = false, auditWindowIsShow = false;

    private ITaskPresenter taskPresenter;
    private ITimelinePresenter timelinePresenter;
    private IUploadImagePresenter uploadImagePresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_timeline, container, false);

        ButterKnife.bind(this, view);

        initView(view);

        return view;
    }

    /**
     * 初始化界面
     */
    @SuppressLint("InflateParams")
    private void initView(View view) {

        swipeRefreshLayout.setOnRefreshListener(() -> refreshTimeline("refresh",
                Calendar.getInstance(Locale.CHINA).getTimeInMillis() + ""));
        swipeRefreshLayout.setEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            if (verticalOffset == 0) {
                swipeRefreshLayout.setEnabled(true);
            } else {
                swipeRefreshLayout.setEnabled(false);
            }
        });

        TextView tv_space_comment = (TextView) view.findViewById(R.id.tv_space_comment);
        tv_space_comment.setOnTouchListener((v, event) -> {
            if (commentWindowIsShow) {
                ll_comment.setVisibility(View.GONE);
                commentWindowIsShow = false;

                // 关闭键盘
                UtilBox.toggleSoftInput(ll_comment, false);
            }
            return false;
        });

        TextView tv_space_audit = (TextView) view.findViewById(R.id.tv_space_audit);
        tv_space_audit.setOnTouchListener((v, event) -> {
            if (auditWindowIsShow) {
                ll_audit.setVisibility(View.GONE);

                auditWindowIsShow = false;
            }
            return false;
        });

        ll_comment = (LinearLayout) view.findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) view.findViewById(R.id.ll_audit);

        timelinePresenter = new TimelinePresenterImpl(this);
        commentPresenter = new CommentPresenterImpl(getActivity(), this);
        uploadImagePresenter = new UploadImagePresenterImpl(getActivity(), this);
        auditPresenter = new AuditPresenterImpl(getActivity(), this);
        taskPresenter = new TaskPresenterImpl(this);

        // 延迟执行才能使旋转进度条显示出来
        new Handler().postDelayed(() -> {
            timelinePresenter.onSetSwipeRefreshVisibility(Constants.VISIBLE);
            refreshTimeline("refresh",
                    Calendar.getInstance(Locale.CHINA).getTimeInMillis() / 1000 + "");
        }, 200);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(UpdateViewEvent event) {
        int position;
        switch (event.getAction()) {
            case Constants.ACTION_DELETE_TASK:
                position = TimelineListAdapter.getTaskPositionWithID(event.getStringExtra());

                if (position != -1) {
                    TimelineListAdapter.taskList.remove(position);
                    adapter.notifyDataSetChanged();
                }

                break;

            case Constants.ACTION_SEND_COMMENT:
                position = TimelineListAdapter.getTaskPositionWithID(event.getStringExtra());

                if (position != -1) {
                    Task task = TimelineListAdapter.taskList.get(position);
                    Comment comment = (Comment) event.getSerializableExtra();
                    // 防止多次添加
                    if (task.getComments().isEmpty() ||
                            task.getComments().get(task.getComments().size() - 1).getTime()
                                    != comment.getTime()) {

                        TimelineListAdapter.taskList.get(position).getComments().add(comment);

                        adapter.notifyDataSetChanged();

                        break;
                    }
                }

                break;

            case Constants.ACTION_AUDIT:
                position = TimelineListAdapter.getTaskPositionWithID(event.getStringExtra());

                if (position != -1) {
                    TimelineListAdapter.taskList.get(position).setAuditResult(
                            (String) event.getSerializableExtra());

                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * 刷新时间线
     *
     * @param type         refresh或loadMore
     * @param request_time 需要获取哪个时间之后的数据
     */
    private void refreshTimeline(final String type, final String request_time) {
        if (!Config.IS_CONNECTED) {
            UtilBox.showSnackbar(getActivity(), R.string.cant_access_network);

            timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
            return;
        }

        timelinePresenter.doPullTimeline(request_time, type);
    }

    /**
     * 弹出评论窗口
     *
     * @param context    上下文
     * @param taskID     任务ID
     * @param receiver   接收者
     * @param receiverID 接收者ID
     */
    public static void showCommentWindow(final Context context, final String taskID,
                                         final String receiver, final String receiverID) {
        commentWindowIsShow = true;

        if (auditWindowIsShow) {
            ll_audit.setVisibility(View.GONE);

            auditWindowIsShow = false;
        }

        ll_comment.setVisibility(View.VISIBLE);
        final EditText edt_content = (EditText) ll_comment.findViewById(R.id.edt_comment_content);
        if (!"".equals(receiver)) {
            edt_content.setHint("回复" + receiver + ":");
        } else {
            edt_content.setHint("评论");
        }
        edt_content.setText(null);
        edt_content.requestFocus();

        // 弹出键盘
        UtilBox.toggleSoftInput(ll_comment, true);

        appBarLayout.setExpanded(false, false);

        Button btn_send = (Button) ll_comment.findViewById(R.id.btn_comment_send);
        btn_send.setOnClickListener(v -> {
            if (edt_content.getText().toString().isEmpty()) {
                Toast.makeText(context,
                        "请填入评论内容",
                        Toast.LENGTH_SHORT).show();
                return;
            } else if (!Config.IS_CONNECTED) {
                Toast.makeText(context,
                        R.string.cant_access_network,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            ll_comment.setVisibility(View.GONE);

            UtilBox.toggleSoftInput(ll_comment, false);

            commentPresenter.doSendComment(taskID, receiver, receiverID, edt_content.getText().toString());
        });
    }

    /**
     * 弹出审核窗口
     *
     * @param context 上下文
     * @param taskID  任务ID
     */
    public static void showAuditWindow(final Context context, final String taskID) {
        auditWindowIsShow = true;

        if (commentWindowIsShow) {
            ll_comment.setVisibility(View.GONE);
            commentWindowIsShow = false;
        }

        ll_audit.setVisibility(View.VISIBLE);

        appBarLayout.setExpanded(false, true);

        final int[] audit_result = {3};
        RadioGroup radioGroup = (RadioGroup) ll_audit.findViewById(R.id.rg_audit);
        radioGroup.check(R.id.rb_complete);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_failed:
                    audit_result[0] = 4;
                    break;

                case R.id.rb_complete:
                    audit_result[0] = 3;
                    break;

                case R.id.rb_solved:
                    audit_result[0] = 2;
                    break;
            }
        });

        Button btn_send = (Button) ll_audit.findViewById(R.id.btn_audit_send);
        btn_send.setOnClickListener(v -> {
            if (!Config.IS_CONNECTED) {
                UtilBox.showSnackbar(context, R.string.cant_access_network);
                return;
            }

            auditPresenter.doAudit(taskID, audit_result[0] + "");
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case Constants.CODE_PUBLISH_TASK:
                if (resultCode == Activity.RESULT_OK) {
                    String grade = data.getStringExtra("grade");
                    String content = data.getStringExtra("content");
                    String executor = data.getStringExtra("executor");
                    String supervisor = data.getStringExtra("supervisor");
                    String auditor = data.getStringExtra("auditor");

                    taskID = data.getStringExtra("taskID");
                    pictureList = data.getStringArrayListExtra("pictureList");

                    long deadline = data.getLongExtra("deadline", 0);
                    long publish_time = data.getLongExtra("publish_time", 0);
                    long create_time = data.getLongExtra("create_time", 0);

                    int sendState = Task.SENDING;
                    if (pictureList != null && !pictureList.isEmpty()) {
                        // 开始上传图片
                        uploadImagePresenter.doUploadImages(pictureList, Constants.DIR_TASK);
                    } else {
                        sendState = Task.SUCCESS;
                    }

                    TimelineListAdapter.taskList.add(0, new Task(taskID,
                            Urls.MEDIA_CENTER_PORTRAIT + Config.MID + ".jpg",
                            Config.NICKNAME, Config.MID, grade, content,
                            executor, supervisor, auditor,
                            pictureList, null, deadline, publish_time, create_time,
                            "1", sendState));

                    adapter.notifyItemInserted(0);
                    recyclerView.smoothScrollToPosition(0);
                }
                break;
        }
    }

    @Override
    public void onPullTimelineResult(String result, final String type, String info) {
        switch (result) {
            case Constants.SUCCESS:
                if ("refresh".equals(type)) {
                    adapter = new TimelineListAdapter(getActivity(), true, info, uploadImagePresenter, recyclerView);
                    recyclerView.setAdapter(adapter);
                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);

                    if (adapter.onLoadMoreListener == null) {
                        adapter.setOnLoadMoreListener(() -> {
                            TimelineListAdapter.taskList.add(null);
                            adapter.notifyItemInserted(TimelineListAdapter.taskList.size() - 1);

                            refreshTimeline("loadMore",
                                    (TimelineListAdapter.taskList
                                            .get(TimelineListAdapter.taskList.size() - 2)
                                            .getCreateTime() / 1000 - 1) + "");
                        });
                    }
                } else {
                    TimelineListAdapter.taskList.remove(TimelineListAdapter.taskList.size() - 1);
                    adapter.notifyItemRemoved(TimelineListAdapter.taskList.size());

                    adapter = new TimelineListAdapter(getActivity(), false, info, uploadImagePresenter, recyclerView);
                    adapter.notifyDataSetChanged();
                    adapter.setLoaded();
                }

                break;

            case Constants.NOMORE:
            case Constants.FAILED:
            default:
                if ("refresh".equals(type)) {
                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    TimelineListAdapter.taskList.remove(TimelineListAdapter.taskList.size() - 1);
                    adapter.notifyItemRemoved(TimelineListAdapter.taskList.size());
                }

                UtilBox.showSnackbar(getActivity(), info);
                break;
        }
    }

    @Override
    public void onGetNewsResult(int result, News news) {
    }

    @Override
    public void onSetSwipeRefreshVisibility(int visibility) {
        if (Constants.VISIBLE == visibility) {
            swipeRefreshLayout.setRefreshing(true);
        } else if (Constants.INVISIBLE == visibility) {
            new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        }
    }

    @Override
    public void onSendCommentResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            commentWindowIsShow = false;
        } else if (Constants.FAILED.equals(result)) {
            UtilBox.showSnackbar(getActivity(), info);
        }
    }

    @Override
    public void onUploadImageResult(String result, String info) {
    }

    @Override
    public void onUploadImagesResult(String result, String info, List<String> pictureList) {
        switch (result) {
            case Constants.SUCCESS:
                taskPresenter.doUpdateTask(taskID, pictureList);
                break;

            case Constants.FAILED:
                int position = TimelineListAdapter.getTaskPositionWithID(taskID);

                if (position != -1) {
                    TimelineListAdapter.taskList.get(position).setSendState(Task.FAILED);
                    adapter.notifyDataSetChanged();
                }

                UtilBox.showSnackbar(getActivity(), info);
                break;
        }
    }

    @Override
    public void onAuditResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            ll_audit.setVisibility(View.GONE);

            auditWindowIsShow = false;
        }

        Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPublishTaskResult(String result, String info) {
    }

    @Override
    public void onDeleteTaskResult(String result, String info) {

    }

    @Override
    public void onUpdateTaskResult(String result, String info) {
        int position;
        switch (result) {
            case Constants.SUCCESS:
                position = TimelineListAdapter.getTaskPositionWithID(taskID);

                if (position != -1) {
                    TimelineListAdapter.taskList.get(position).setSendState(Task.SUCCESS);
                    adapter.notifyDataSetChanged();
                }

                break;

            default:
                position = TimelineListAdapter.getTaskPositionWithID(taskID);

                if (position != -1) {
                    TimelineListAdapter.taskList.get(position).setSendState(Task.FAILED);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }
}