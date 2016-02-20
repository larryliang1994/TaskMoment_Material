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
    ImageView mPortraitImageView;

    @Bind(R.id.btn_audit)
    Button mAuditButton;

    @Bind(R.id.tv_desc)
    TextView mDescTextView;

    @Bind(R.id.tv_grade)
    TextView mGradeTextView;

    @Bind(R.id.tv_nickname)
    TextView mNicknameTextView;

    @Bind(R.id.gv_picture)
    GridView mPictureGridView;

    @Bind(R.id.tv_executor)
    TextView mExecutorTextView;

    @Bind(R.id.tv_supervisor)
    TextView mSupervisorTextView;

    @Bind(R.id.tv_auditor)
    TextView mAuditorTextView;

    @Bind(R.id.tv_publishTime)
    TextView mPublishTimeTextView;

    @Bind(R.id.tv_deadline)
    TextView mDeadlineTextView;

    @Bind(R.id.tv_startTime)
    TextView mStartTimeTextView;

    @Bind(R.id.tv_delete)
    TextView mDeleteTextView;

    @Bind(R.id.tv_audit_result)
    TextView mAuditResultTextView;

    @Bind(R.id.tv_space_comment)
    TextView mCommentSpaceTextView;

    @Bind(R.id.tv_space_audit)
    TextView mAuditSpaceTextView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private static LinearLayout sCommentLinearLayout;
    private static LinearLayout sAuditLinearLayout;
    private ListView mCommentListView;

    private Task task;
    private CommentAdapter mCommentAdapter;
    private static boolean commentWindowIsShow = false;
    private static boolean auditWindowIsShow = false;
    private static ICommentPresenter sCommentPresenter;
    private ITaskPresenter mTaskPresenter;
    private IAuditPresenter mAuditPresenter;

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

        sCommentLinearLayout = (LinearLayout) findViewById(R.id.ll_comment);
        sAuditLinearLayout = (LinearLayout) findViewById(R.id.ll_audit);
        mCommentListView = (ListView) findViewById(R.id.lv_comment);

        mCommentSpaceTextView.setOnTouchListener((v, event) -> {
            if (commentWindowIsShow) {
                sCommentLinearLayout.setVisibility(View.GONE);
                commentWindowIsShow = false;

                // 关闭键盘
                UtilBox.toggleSoftInput(sCommentLinearLayout, false);
            }
            return false;
        });

        mAuditSpaceTextView.setOnTouchListener((v, event) -> {
            if (auditWindowIsShow) {
                sAuditLinearLayout.setVisibility(View.GONE);

                auditWindowIsShow = false;
            }
            return false;
        });

        if (!Config.MID.equals(task.getMid())) {
            mDeleteTextView.setVisibility(View.GONE);
        } else {
            mDeleteTextView.setVisibility(View.VISIBLE);

            mDeleteTextView.setOnClickListener(v -> {
                mDeleteTextView.setBackgroundColor(
                        ContextCompat.getColor(TaskInfoActivity.this, R.color.gray));

                new Handler().postDelayed(() -> mDeleteTextView.setBackgroundColor(
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
                            mTaskPresenter.doDeleteTask(TaskInfoActivity.this, task.getId());
                        })
                        .show();
            });
        }

        if (!Config.MID.equals(task.getAuditor()) || !"1".equals(task.getAuditResult())) {
            mAuditButton.setVisibility(View.GONE);
        }

        if (task.getComments() != null && !task.getComments().isEmpty()) {
            mCommentAdapter = new CommentAdapter(this, task.getComments(), "taskInfo");
            mCommentListView.setVisibility(View.VISIBLE);
            mCommentListView.setAdapter(mCommentAdapter);
            UtilBox.setListViewHeightBasedOnChildren(mCommentListView);
        } else {
            mCommentListView.setVisibility(View.GONE);
        }

        sCommentPresenter = new CommentPresenterImpl(this, this);
        mAuditPresenter = new AuditPresenterImpl(this, this);
        mTaskPresenter = new TaskPresenterImpl(this);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initTaskInfo() {
        ImageLoader.getInstance().displayImage(task.getPortraitUrl() + "?t=" + Config.TIME, mPortraitImageView);
        mPortraitImageView.requestFocus();

        mDescTextView.setText(task.getDesc());
        mNicknameTextView.setText(task.getNickname());
        mAuditResultTextView.setText(task.getAuditResult());
        mGradeTextView.setText(task.getGrade());
        setGradeColor(mGradeTextView, task.getGrade());

        mDeadlineTextView.append(UtilBox.getDateToString(task.getDeadline(), UtilBox.DATE_TIME));
        mStartTimeTextView.append(UtilBox.getDateToString(task.getStartTime(), UtilBox.DATE_TIME));
        mPublishTimeTextView.append(UtilBox.getDateToString(task.getCreateTime(), UtilBox.DATE_TIME));

        mPictureGridView.setAdapter(new TimelinePictureAdapter(this, task.getPictures()));
        UtilBox.setGridViewHeightBasedOnChildren(mPictureGridView, true);

        MemberListAdapter.getMemberList(this, new MemberListAdapter.GetMemberCallBack() {
            @Override
            public void successCallback() {
                mExecutorTextView.append(MemberListAdapter.getMemberWithID(task.getExecutor()).getName());

                mSupervisorTextView.append(MemberListAdapter.getMemberWithID(task.getSupervisor()).getName());

                mAuditorTextView.append(MemberListAdapter.getMemberWithID(task.getAuditor()).getName());
            }

            @Override
            public void failedCallback() {

            }
        });

        if ("1".equals(task.getAuditResult()) || "null".equals(task.getAuditResult())) {
            mAuditResultTextView.setVisibility(View.GONE);
        } else {
            mAuditResultTextView.setVisibility(View.VISIBLE);

            mAuditResultTextView.setText(
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
            sAuditLinearLayout.setVisibility(View.GONE);

            auditWindowIsShow = false;
        }

        sCommentLinearLayout.setVisibility(View.VISIBLE);
        final EditText edt_content = (EditText) sCommentLinearLayout.findViewById(R.id.edt_comment_content);
        if (!"".equals(receiver)) {
            edt_content.setHint("回复" + receiver + ":");
        } else {
            edt_content.setHint("评论");
        }
        edt_content.setText(null);
        edt_content.requestFocus();

        // 弹出键盘
        UtilBox.toggleSoftInput(sCommentLinearLayout, true);

        Button btn_send = (Button) sCommentLinearLayout.findViewById(R.id.btn_comment_send);
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

            sCommentLinearLayout.setVisibility(View.GONE);

            UtilBox.toggleSoftInput(sCommentLinearLayout, false);

            sCommentPresenter.doSendComment(taskID, receiver, receiverID,
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
            sCommentLinearLayout.setVisibility(View.GONE);
            commentWindowIsShow = false;
        }

        sAuditLinearLayout.setVisibility(View.VISIBLE);

        final int[] audit_result = {3};
        RadioGroup radioGroup = (RadioGroup) sAuditLinearLayout.findViewById(R.id.rg_audit);
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

        Button btn_send = (Button) sAuditLinearLayout.findViewById(R.id.btn_audit_send);
        btn_send.setOnClickListener(v -> {
            if (!Config.IS_CONNECTED) {
                UtilBox.showSnackbar(context, R.string.cant_access_network);
                return;
            }

            mAuditPresenter.doAudit(taskID, audit_result[0] + "");
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
                List<Comment> list = mCommentAdapter.commentList;
                list.add((Comment) event.getSerializableExtra());

                mCommentListView.setAdapter(new CommentAdapter(TaskInfoActivity.this, list, "taskInfo"));

                UtilBox.setListViewHeightBasedOnChildren(mCommentListView);
                break;

            case Constants.ACTION_AUDIT:
                mAuditResultTextView.setVisibility(View.VISIBLE);

                task.setAuditResult((String) event.getSerializableExtra());

                mAuditResultTextView.setText(
                        Constants.AUDIT_RESULT[Integer.valueOf(task.getAuditResult())]);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (commentWindowIsShow) {
                sCommentLinearLayout.setVisibility(View.GONE);
                commentWindowIsShow = false;
                UtilBox.toggleSoftInput(sCommentLinearLayout, false);
            } else if (auditWindowIsShow) {
                sAuditLinearLayout.setVisibility(View.GONE);
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
            sAuditLinearLayout.setVisibility(View.GONE);

            mAuditButton.setVisibility(View.GONE);

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