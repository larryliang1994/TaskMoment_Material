package com.jiubai.taskmoment.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.CommentAdapter;
import com.jiubai.taskmoment.adapter.MemberListAdapter;
import com.jiubai.taskmoment.adapter.TimelinePictureAdapter;
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.AuditPresenterImpl;
import com.jiubai.taskmoment.presenter.CommentPresenterImpl;
import com.jiubai.taskmoment.presenter.IAuditPresenter;
import com.jiubai.taskmoment.presenter.ICommentPresenter;
import com.jiubai.taskmoment.presenter.ITaskPresenter;
import com.jiubai.taskmoment.presenter.TaskPresenterImpl;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.ui.iview.IAuditView;
import com.jiubai.taskmoment.ui.iview.ICommentView;
import com.jiubai.taskmoment.ui.iview.ITaskView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * 任务详情页面
 */
public class TaskInfoActivity extends BaseActivity implements ICommentView, IAuditView, ITaskView {

    @Bind(R.id.iv_portrait)
    ImageView iv_portrait;

    @Bind(R.id.btn_audit)
    Button btn_audit;

    @Bind(R.id.tv_desc)
    TextView tv_desc;

    @Bind(R.id.tv_grade)
    TextView tv_grade;

    @Bind(R.id.tv_nickname)
    TextView tv_nickname;

    @Bind(R.id.gv_picture)
    GridView gv_picture;

    @Bind(R.id.tv_executor)
    TextView tv_executor;

    @Bind(R.id.tv_supervisor)
    TextView tv_supervisor;

    @Bind(R.id.tv_auditor)
    TextView tv_auditor;

    @Bind(R.id.tv_publishTime)
    TextView tv_publishTime;

    @Bind(R.id.tv_deadline)
    TextView tv_deadline;

    @Bind(R.id.tv_startTime)
    TextView tv_startTime;

    @Bind(R.id.tv_delete)
    TextView tv_delete;

    @Bind(R.id.tv_audit_result)
    TextView tv_audit_result;

    @Bind(R.id.tv_space_comment)
    TextView tv_space_comment;

    @Bind(R.id.tv_space_audit)
    TextView tv_space_audit;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private static LinearLayout ll_comment;
    private static LinearLayout ll_audit;
    private static ListView lv_comment;

    private Task task;
    private static CommentAdapter adapter_comment;
    private static boolean commentWindowIsShow = false;
    private static boolean auditWindowIsShow = false;
    private static ICommentPresenter commentPresenter;
    private ITaskPresenter taskPresenter;
    private IAuditPresenter auditPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_taskinfo);

        ButterKnife.bind(this);

        task = (Task) getIntent().getSerializableExtra("task");

        initView();

        EventBus.getDefault().register(this);
    }

    /**
     * 初始化所有view
     */
    private void initView() {
        initToolbar();

        initTaskInfo();

        ll_comment = (LinearLayout) findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) findViewById(R.id.ll_audit);
        lv_comment = (ListView) findViewById(R.id.lv_comment);

        tv_space_comment.setOnTouchListener((v, event) -> {
            if (commentWindowIsShow) {
                ll_comment.setVisibility(View.GONE);
                commentWindowIsShow = false;

                // 关闭键盘
                UtilBox.toggleSoftInput(ll_comment, false);
            }
            return false;
        });

        tv_space_audit.setOnTouchListener((v, event) -> {
            if (auditWindowIsShow) {
                ll_audit.setVisibility(View.GONE);

                auditWindowIsShow = false;
            }
            return false;
        });

        if (!Config.MID.equals(task.getMid())) {
            tv_delete.setVisibility(View.GONE);
        } else {
            tv_delete.setVisibility(View.VISIBLE);

            tv_delete.setOnClickListener(v -> {
                tv_delete.setBackgroundColor(
                        ContextCompat.getColor(TaskInfoActivity.this, R.color.gray));

                new Handler().postDelayed(() -> tv_delete.setBackgroundColor(
                        ContextCompat.getColor(TaskInfoActivity.this, R.color.transparent)), 100);

                final MaterialDialog dialog = new MaterialDialog(TaskInfoActivity.this);
                dialog.setMessage("真的要删除吗")
                        .setCanceledOnTouchOutside(true)
                        .setNegativeButton("我手滑了", v1 -> {
                            dialog.dismiss();
                        })
                        .setPositiveButton("真的", v1 -> {
                            if (!Config.IS_CONNECTED) {
                                UtilBox.showSnackbar(this, R.string.cant_access_network);
                                return;
                            }

                            dialog.dismiss();
                            taskPresenter.doDeleteTask(TaskInfoActivity.this, task.getId());
                        })
                        .show();
            });
        }

        if (!Config.MID.equals(task.getAuditor()) || !"1".equals(task.getAuditResult())) {
            btn_audit.setVisibility(View.GONE);
        }

        if (task.getComments() != null && !task.getComments().isEmpty()) {
            adapter_comment = new CommentAdapter(this, task.getComments(), "taskInfo");
            lv_comment.setVisibility(View.VISIBLE);
            lv_comment.setAdapter(adapter_comment);
            UtilBox.setListViewHeightBasedOnChildren(lv_comment);
        } else {
            lv_comment.setVisibility(View.GONE);
        }

        commentPresenter = new CommentPresenterImpl(this, this);
        auditPresenter = new AuditPresenterImpl(this, this);
        taskPresenter = new TaskPresenterImpl(this);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initTaskInfo() {
        ImageLoader.getInstance().displayImage(task.getPortraitUrl() + "?t=" + Config.TIME, iv_portrait);
        iv_portrait.requestFocus();

        tv_desc.setText(task.getDesc());
        tv_nickname.setText(task.getNickname());
        tv_audit_result.setText(task.getAuditResult());
        tv_grade.setText(task.getGrade());
        setGradeColor(tv_grade, task.getGrade());

        tv_deadline.append(UtilBox.getDateToString(task.getDeadline(), UtilBox.DATE_TIME));
        tv_startTime.append(UtilBox.getDateToString(task.getStartTime(), UtilBox.DATE_TIME));
        tv_publishTime.append(UtilBox.getDateToString(task.getCreateTime(), UtilBox.DATE_TIME));

        gv_picture.setAdapter(new TimelinePictureAdapter(this, task.getPictures()));
        UtilBox.setGridViewHeightBasedOnChildren(gv_picture, true);

        MemberListAdapter.getMemberList(this, new MemberListAdapter.GetMemberCallBack() {
            @Override
            public void successCallback() {
                tv_executor.append(MemberListAdapter.getMemberWithID(task.getExecutor()).getName());

                tv_supervisor.append(MemberListAdapter.getMemberWithID(task.getSupervisor()).getName());

                tv_auditor.append(MemberListAdapter.getMemberWithID(task.getAuditor()).getName());
            }

            @Override
            public void failedCallback() {

            }
        });

        if ("1".equals(task.getAuditResult()) || "null".equals(task.getAuditResult())) {
            tv_audit_result.setVisibility(View.GONE);
        } else {
            tv_audit_result.setVisibility(View.VISIBLE);

            tv_audit_result.setText(
                    Constants.AUDIT_RESULT[Integer.valueOf(task.getAuditResult())]);
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
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.S));
                break;

            case "A":
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.A));
                break;

            case "B":
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.B));
                break;

            case "C":
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.C));
                break;

            case "D":
                tv_grade.setTextColor(ContextCompat.getColor(this, R.color.D));
                break;
        }
    }

    @OnClick({R.id.btn_audit, R.id.btn_comment, R.id.tv_nickname, R.id.iv_portrait})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_audit:
                showAuditWindow(this, task.getId());
                break;

            case R.id.btn_comment:
                showCommentWindow(this, task.getId(), "", "");
                break;

            case R.id.tv_nickname:
            case R.id.iv_portrait:
                Intent intent = new Intent(this, PersonalTimelineActivity.class);
                intent.putExtra("mid", task.getMid());
                startActivity(intent);
                break;
        }
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

            commentPresenter.doSendComment(taskID, receiver, receiverID,
                    edt_content.getText().toString());
        });
    }

    /**
     * 弹出审核窗口
     */
    @SuppressWarnings("unused")
    public void showAuditWindow(final Context context, final String taskID) {
        auditWindowIsShow = true;

        if (commentWindowIsShow) {
            ll_comment.setVisibility(View.GONE);
            commentWindowIsShow = false;
        }

        ll_audit.setVisibility(View.VISIBLE);

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
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(UpdateViewEvent event){
        switch (event.getAction()) {
            case Constants.ACTION_SEND_COMMENT:
                List<Comment> list = adapter_comment.commentList;
                list.add((Comment) event.getSerializableExtra());

                lv_comment.setAdapter(new CommentAdapter(TaskInfoActivity.this, list, "taskInfo"));

                UtilBox.setListViewHeightBasedOnChildren(lv_comment);
                break;

            case Constants.ACTION_AUDIT:
                tv_audit_result.setVisibility(View.VISIBLE);

                task.setAuditResult((String) event.getSerializableExtra());

                tv_audit_result.setText(
                        Constants.AUDIT_RESULT[Integer.valueOf(task.getAuditResult())]);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (commentWindowIsShow) {
                ll_comment.setVisibility(View.GONE);
                commentWindowIsShow = false;
                UtilBox.toggleSoftInput(ll_comment, false);
            } else if (auditWindowIsShow) {
                ll_audit.setVisibility(View.GONE);
                auditWindowIsShow = false;
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAuditResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            ll_audit.setVisibility(View.GONE);

            btn_audit.setVisibility(View.GONE);

            auditWindowIsShow = false;
        } else {
            UtilBox.showSnackbar(this, info);
        }
    }

    @Override
    public void onSendCommentResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            commentWindowIsShow = false;
        } else if (Constants.FAILED.equals(result)) {
            UtilBox.showSnackbar(this, info);
        }
    }

    @Override
    public void onPublishTaskResult(String result, String info) {

    }

    @Override
    public void onDeleteTaskResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            finish();
        } else {
            UtilBox.showSnackbar(this, info);
        }
    }

    @Override
    public void onUpdateTaskResult(String result, String info) {

    }
}