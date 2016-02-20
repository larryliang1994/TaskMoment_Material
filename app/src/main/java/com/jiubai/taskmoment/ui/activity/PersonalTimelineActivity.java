package com.jiubai.taskmoment.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.MemberListAdapter;
import com.jiubai.taskmoment.adapter.PersonalTimelineAdapter;
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.News;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.receiver.UpdateViewEvent;
import com.jiubai.taskmoment.widget.BorderScrollView;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.AuditPresenterImpl;
import com.jiubai.taskmoment.presenter.CommentPresenterImpl;
import com.jiubai.taskmoment.presenter.IAuditPresenter;
import com.jiubai.taskmoment.presenter.ICommentPresenter;
import com.jiubai.taskmoment.presenter.ITimelinePresenter;
import com.jiubai.taskmoment.presenter.TimelinePresenterImpl;
import com.jiubai.taskmoment.ui.iview.IAuditView;
import com.jiubai.taskmoment.ui.iview.ICommentView;
import com.jiubai.taskmoment.ui.iview.ITimelineView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 个人情况
 */
public class PersonalTimelineActivity extends BaseActivity
        implements ITimelineView, ICommentView, IAuditView {
    @Bind(R.id.iv_portrait)
    ImageView mPortraitImageView;

    @Bind(R.id.tv_loading)
    TextView mLoadingTextView;

    @Bind(R.id.tv_personal_nickname)
    TextView mNicknameTextView;

    @Bind(R.id.tv_space_comment)
    TextView mCommentSpaceTextView;

    @Bind(R.id.tv_space_audit)
    TextView mAuditSpaceTextView;

    @Bind(R.id.iv_companyBackground)
    ImageView mCompanyBackgroundImageView;

    @Bind(R.id.sv_personal)
    BorderScrollView mScrollView;

    @Bind(R.id.lv_personal)
    ListView mListView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private static LinearLayout sCommentLinearLayout, sAuditLinearLayout;
    private PersonalTimelineAdapter mAdapter;
    public static boolean commentWindowIsShow = false, auditWindowIsShow = false;
    private static ICommentPresenter mCommentPresenter;
    private static IAuditPresenter mAuditPresenter;

    private ITimelinePresenter mTimelinePresenter;
    private View mFooterView;
    private String mid, isAudit, isInvolved;
    private boolean isBottomRefreshing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_personal_timeline);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        mid = intent.getStringExtra("mid");
        isAudit = intent.getBooleanExtra("isAudit", false) ? "1" : "0";
        isInvolved = intent.getBooleanExtra("isInvolved", false) ? "1" : "0";

        initView();

        EventBus.getDefault().register(this);
    }

    /**
     * 初始化组件
     */
    @SuppressLint("InflateParams")
    private void initView() {
        initToolbar();

        getUserInfo();

        mListView = (ListView) findViewById(R.id.lv_personal);
        sCommentLinearLayout = (LinearLayout) findViewById(R.id.ll_comment);
        sAuditLinearLayout = (LinearLayout) findViewById(R.id.ll_audit);

        mFooterView = LayoutInflater.from(this).inflate(R.layout.load_more_timeline, null);

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

        mScrollView.setOnBorderListener(new BorderScrollView.OnBorderListener() {

            @Override
            public void onTop() {
            }

            @Override
            public void onBottom() {
                // 有footerView并且不是正在加载
                if (mListView.getFooterViewsCount() > 0 && !isBottomRefreshing) {
                    isBottomRefreshing = true;

                    // 参数应为最后一条任务的时间减1秒
                    refreshTimeline("loadMore", (PersonalTimelineAdapter.taskList
                            .get(PersonalTimelineAdapter.taskList.size() - 1)
                            .getCreateTime() / 1000 - 1) + "");
                }
            }
        });

        ImageLoader.getInstance().displayImage(
                Config.COMPANY_BACKGROUND + "?t=" + Config.TIME, mCompanyBackgroundImageView);

        mCommentPresenter = new CommentPresenterImpl(this, this);
        mAuditPresenter = new AuditPresenterImpl(this, this);
        mTimelinePresenter = new TimelinePresenterImpl(this);

        refreshTimeline("refresh", Calendar.getInstance(Locale.CHINA).getTimeInMillis() / 1000 + "");
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * 初始化头像
     */
    private void initPortrait(String name) {
        mPortraitImageView.setFocusable(true);
        mPortraitImageView.setFocusableInTouchMode(true);
        mPortraitImageView.requestFocus();
        ImageLoader.getInstance().displayImage(
                Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg" + "?t=" + Config.TIME, mPortraitImageView);
        mPortraitImageView.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalTimelineActivity.this, PersonalInfoActivity.class);

            intent.putExtra("nickname", name);
            intent.putExtra("mid", mid);

            startActivity(intent);
        });
    }

    /**
     * 获取时间线
     *
     * @param type         类别 refresh或loadMore
     * @param request_time 需要获取哪个时间之后的数据
     */
    private void refreshTimeline(final String type, String request_time) {
        if (!Config.IS_CONNECTED) {
            UtilBox.showSnackbar(this, R.string.cant_access_network);
            return;
        }

        mTimelinePresenter.doPullTimeline(request_time, type, mid, isAudit, isInvolved);
    }

    /**
     * 设置适配器，并重新设置ListView的高度
     *
     * @param type 刷新类型
     */
    private void resetListViewHeight(final String type) {
        runOnUiThread(() -> {
            mListView.setAdapter(mAdapter);
            UtilBox.setListViewHeightBasedOnChildren(mListView);

            if ("loadMore".equals(type)) {
                isBottomRefreshing = false;
            } else {
                int svHeight = mScrollView.getHeight();

                int lvHeight = mListView.getLayoutParams().height;

                // 312是除去上部其他组件高度后的剩余空间
                if (lvHeight >
                        svHeight - UtilBox.dip2px(PersonalTimelineActivity.this, 312)
                        && mListView.getFooterViewsCount() == 0) {

                    mListView.addFooterView(mFooterView);
                    UtilBox.setListViewHeightBasedOnChildren(mListView);
                }
            }
        });
    }

    /**
     * 从memberList中读取用户信息
     */
    private void getUserInfo() {
        MemberListAdapter.getMemberList(this, new MemberListAdapter.GetMemberCallBack() {
            @Override
            public void successCallback() {
                String name = MemberListAdapter.getMemberWithID(mid).getName();
                mToolbar.setTitle(name);
                mNicknameTextView.setText(name);

                initPortrait(name);
            }

            @Override
            public void failedCallback() {

            }
        });
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

            mCommentPresenter.doSendComment(taskID, receiver, receiverID,
                    edt_content.getText().toString());
        });
    }

    /**
     * 弹出审核窗口
     */
    public static void showAuditWindow(final Context context, final String taskID) {
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
    public void onEvent(UpdateViewEvent event) {
        int position;
        switch (event.getAction()) {
            case Constants.ACTION_DELETE_TASK:
                position = PersonalTimelineAdapter.getTaskPositionWithID(event.getStringExtra());

                if (position != -1) {
                    PersonalTimelineAdapter.taskList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    UtilBox.setListViewHeightBasedOnChildren(mListView);
                }

                break;

            case Constants.ACTION_SEND_COMMENT:
                position = PersonalTimelineAdapter.getTaskPositionWithID(event.getStringExtra());

                if (position != -1) {
                    Task task = PersonalTimelineAdapter.taskList.get(position);
                    Comment comment = (Comment) event.getSerializableExtra();

                    if (task.getComments().isEmpty() ||
                            task.getComments().get(task.getComments().size() - 1).getTime()
                            != comment.getTime()) {

                        PersonalTimelineAdapter.taskList.get(position).getComments().add(comment);
                        mAdapter.notifyDataSetChanged();
                        UtilBox.setListViewHeightBasedOnChildren(mListView);

                        break;
                    }
                }

                break;

            case Constants.ACTION_AUDIT:
                position = PersonalTimelineAdapter.getTaskPositionWithID(event.getStringExtra());

                if (position != -1) {
                    PersonalTimelineAdapter.taskList.get(
                            position).setAuditResult((String) event.getSerializableExtra());

                    mAdapter.notifyDataSetChanged();
                    UtilBox.setListViewHeightBasedOnChildren(mListView);
                }

                break;

            case Constants.ACTION_CHANGE_NICKNAME:
                mToolbar.setTitle(Config.NICKNAME);
                mNicknameTextView.setText(Config.NICKNAME);
                break;

            case Constants.ACTION_CHANGE_PORTRAIT:
                ImageLoader.getInstance().displayImage(
                        Config.PORTRAIT + "?t=" + Config.TIME, mPortraitImageView);
                break;
        }
    }

    @Override
    public void onAuditResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            sAuditLinearLayout.setVisibility(View.GONE);

            auditWindowIsShow = false;
        }

        UtilBox.showSnackbar(this, info);
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
    public void onPullTimelineResult(String result, final String type, String info) {
        switch (result) {
            case Constants.SUCCESS:
                // 先实例出适配器
                if ("refresh".equals(type)) {
                    mAdapter = new PersonalTimelineAdapter(this, true, info);
                    mTimelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    mAdapter = new PersonalTimelineAdapter(this, false, info);
                }

                // 设置适配器，并重新设置ListView的高度
                new Handler().post(() -> resetListViewHeight(type));
                break;

            case Constants.NOMORE:
                // 没有更多了，就去掉footerView
                if ("refresh".equals(type)) {
                    mTimelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    mListView.removeFooterView(mFooterView);

                    UtilBox.setListViewHeightBasedOnChildren(mListView);

                    isBottomRefreshing = false;
                }

                UtilBox.showSnackbar(this, info);
                break;

            case Constants.FAILED:
            default:
                if ("refresh".equals(type)) {
                    mTimelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    isBottomRefreshing = false;
                }

                UtilBox.showSnackbar(this, info);
                break;
        }
    }

    @Override
    public void onGetNewsResult(int result, News news) {

    }

    @Override
    public void onSetSwipeRefreshVisibility(int visibility) {
        if (visibility == Constants.VISIBLE) {
            mLoadingTextView.setVisibility(View.VISIBLE);
        } else if (visibility == Constants.INVISIBLE) {
            mLoadingTextView.setVisibility(View.GONE);
        }
    }
}