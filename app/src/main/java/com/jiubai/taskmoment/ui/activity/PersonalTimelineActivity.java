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
    ImageView iv_portrait;

    @Bind(R.id.tv_loading)
    TextView tv_loading;

    @Bind(R.id.tv_personal_nickname)
    TextView tv_nickname;

    @Bind(R.id.tv_space_comment)
    TextView tv_space_comment;

    @Bind(R.id.tv_space_audit)
    TextView tv_space_audit;

    @Bind(R.id.iv_companyBackground)
    ImageView iv_companyBackground;

    @Bind(R.id.sv_personal)
    BorderScrollView sv;

    @Bind(R.id.lv_personal)
    ListView lv;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private static LinearLayout ll_comment, ll_audit;
    private static PersonalTimelineAdapter adapter;
    public static boolean commentWindowIsShow = false, auditWindowIsShow = false;
    private static ICommentPresenter commentPresenter;
    private static IAuditPresenter auditPresenter;

    private ITimelinePresenter timelinePresenter;
    private View footerView;
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

        lv = (ListView) findViewById(R.id.lv_personal);
        ll_comment = (LinearLayout) findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) findViewById(R.id.ll_audit);

        footerView = LayoutInflater.from(this).inflate(R.layout.load_more_timeline, null);

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

        sv.setOnBorderListener(new BorderScrollView.OnBorderListener() {

            @Override
            public void onTop() {
            }

            @Override
            public void onBottom() {
                // 有footerView并且不是正在加载
                if (lv.getFooterViewsCount() > 0 && !isBottomRefreshing) {
                    isBottomRefreshing = true;

                    // 参数应为最后一条任务的时间减1秒
                    refreshTimeline("loadMore", (PersonalTimelineAdapter.taskList
                            .get(PersonalTimelineAdapter.taskList.size() - 1)
                            .getCreateTime() / 1000 - 1) + "");
                }
            }
        });

        ImageLoader.getInstance().displayImage(
                Config.COMPANY_BACKGROUND + "?t=" + Config.TIME, iv_companyBackground);

        commentPresenter = new CommentPresenterImpl(this, this);
        auditPresenter = new AuditPresenterImpl(this, this);
        timelinePresenter = new TimelinePresenterImpl(this);

        refreshTimeline("refresh", Calendar.getInstance(Locale.CHINA).getTimeInMillis() / 1000 + "");
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * 初始化头像
     */
    private void initPortrait(String name) {
        iv_portrait.setFocusable(true);
        iv_portrait.setFocusableInTouchMode(true);
        iv_portrait.requestFocus();
        ImageLoader.getInstance().displayImage(
                Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg" + "?t=" + Config.TIME, iv_portrait);
        iv_portrait.setOnClickListener(v -> {
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

        timelinePresenter.doPullTimeline(request_time, type, mid, isAudit, isInvolved);
    }

    /**
     * 设置适配器，并重新设置ListView的高度
     *
     * @param type 刷新类型
     */
    private void resetListViewHeight(final String type) {
        runOnUiThread(() -> {
            lv.setAdapter(adapter);
            UtilBox.setListViewHeightBasedOnChildren(lv);

            if ("loadMore".equals(type)) {
                isBottomRefreshing = false;
            } else {
                int svHeight = sv.getHeight();

                int lvHeight = lv.getLayoutParams().height;

                // 312是除去上部其他组件高度后的剩余空间
                if (lvHeight >
                        svHeight - UtilBox.dip2px(PersonalTimelineActivity.this, 312)
                        && lv.getFooterViewsCount() == 0) {

                    lv.addFooterView(footerView);
                    UtilBox.setListViewHeightBasedOnChildren(lv);
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
                toolbar.setTitle(name);
                tv_nickname.setText(name);

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
    public static void showAuditWindow(final Context context, final String taskID) {
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
    public void onEvent(UpdateViewEvent event) {
        int position;
        switch (event.getAction()) {
            case Constants.ACTION_DELETE_TASK:
                position = PersonalTimelineAdapter.getTaskPositionWithID(event.getStringExtra());

                if (position != -1) {
                    PersonalTimelineAdapter.taskList.remove(position);
                    adapter.notifyDataSetChanged();
                    UtilBox.setListViewHeightBasedOnChildren(lv);
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
                        adapter.notifyDataSetChanged();
                        UtilBox.setListViewHeightBasedOnChildren(lv);

                        break;
                    }
                }

                break;

            case Constants.ACTION_AUDIT:
                position = PersonalTimelineAdapter.getTaskPositionWithID(event.getStringExtra());

                if (position != -1) {
                    PersonalTimelineAdapter.taskList.get(
                            position).setAuditResult((String) event.getSerializableExtra());

                    adapter.notifyDataSetChanged();
                    UtilBox.setListViewHeightBasedOnChildren(lv);
                }

                break;

            case Constants.ACTION_CHANGE_NICKNAME:
                toolbar.setTitle(Config.NICKNAME);
                tv_nickname.setText(Config.NICKNAME);
                break;

            case Constants.ACTION_CHANGE_PORTRAIT:
                ImageLoader.getInstance().displayImage(
                        Config.PORTRAIT + "?t=" + Config.TIME, iv_portrait);
                break;
        }
    }

    @Override
    public void onAuditResult(String result, String info) {
        if (Constants.SUCCESS.equals(result)) {
            ll_audit.setVisibility(View.GONE);

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
                    adapter = new PersonalTimelineAdapter(this, true, info);
                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    adapter = new PersonalTimelineAdapter(this, false, info);
                }

                // 设置适配器，并重新设置ListView的高度
                new Handler().post(() -> resetListViewHeight(type));
                break;

            case Constants.NOMORE:
                // 没有更多了，就去掉footerView
                if ("refresh".equals(type)) {
                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
                } else {
                    lv.removeFooterView(footerView);

                    UtilBox.setListViewHeightBasedOnChildren(lv);

                    isBottomRefreshing = false;
                }

                UtilBox.showSnackbar(this, info);
                break;

            case Constants.FAILED:
            default:
                if ("refresh".equals(type)) {
                    timelinePresenter.onSetSwipeRefreshVisibility(Constants.INVISIBLE);
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
            tv_loading.setVisibility(View.VISIBLE);
        } else if (visibility == Constants.INVISIBLE) {
            tv_loading.setVisibility(View.GONE);
        }
    }
}