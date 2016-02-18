package com.jiubai.taskmoment.ui.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.MemberListAdapter;
import com.jiubai.taskmoment.adapter.PublishPictureAdapter;
import com.jiubai.taskmoment.bean.Member;
import com.jiubai.taskmoment.bean.MyDate;
import com.jiubai.taskmoment.bean.MyTime;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.widget.DateDialog;
import com.jiubai.taskmoment.widget.TimeDialog;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.ITaskPresenter;
import com.jiubai.taskmoment.presenter.TaskPresenterImpl;
import com.jiubai.taskmoment.ui.iview.ITaskView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * 发布任务
 */
public class TaskPublishActivity extends BaseActivity implements ITaskView,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    @Bind(R.id.tv_space)
    TextView tv_space;

    @Bind(R.id.edt_deadline)
    EditText edt_deadline;

    @Bind(R.id.edt_startTime)
    EditText edt_startTime;

    @Bind(R.id.edt_desc)
    EditText edt_desc;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.gv_publish)
    GridView gv;

    @Bind(R.id.tv_grade)
    TextView tv_grade;

    @Bind(R.id.sb_grade)
    SeekBar sb_grade;

    private ITaskPresenter taskPresenter;
    private String date;
    private boolean isDeadline = false, isStartTime = false;
    private PublishPictureAdapter adpt_publishPicture;
    private int grade = 4;
    private int executor = -1, supervisor = -1, auditor = -1;
    private int year_deadline = 0, month_deadline = 0, day_deadline = 0,
            hour_deadline = 0, minute_deadline = 0,
            year_startTime = 0, month_startTime = 0, day_startTime = 0,
            hour_startTime = 0, minute_startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_task_publish);

        ButterKnife.bind(this);

        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_ok:
                UtilBox.toggleSoftInput(edt_desc, false);

                if (!Config.IS_CONNECTED) {
                    UtilBox.showSnackbar(this, R.string.cant_access_network);
                } else if (edt_desc.getText().toString().length() == 0) {
                    UtilBox.showSnackbar(this, "请填入任务描述");
                } else if (executor == -1) {
                    UtilBox.showSnackbar(this, "请选择执行者");
                } else if (supervisor == -1) {
                    UtilBox.showSnackbar(this, "请选择监督者");
                } else if (auditor == -1) {
                    UtilBox.showSnackbar(this, "请选择审核者");
                } else if (year_deadline == 0) {
                    UtilBox.showSnackbar(this, "请填入完成期限");
                } else if (year_startTime == 0) {
                    UtilBox.showSnackbar(this, "请填入开始时间");
                } else {
                    // 先把多余的添加图片入口删掉
                    if (adpt_publishPicture.pictureList != null
                            && !adpt_publishPicture.pictureList.isEmpty()
                            && adpt_publishPicture.actualCount < 9) {
                        adpt_publishPicture.pictureList.remove(
                                adpt_publishPicture.pictureList.size() - 1);
                    }

                    taskPresenter.doPublishTask(
                            String.valueOf(grade),
                            edt_desc.getText().toString(),
                            MemberListAdapter.memberList.get(executor).getMid(),
                            MemberListAdapter.memberList.get(supervisor).getMid(),
                            MemberListAdapter.memberList.get(auditor).getMid(),
                            UtilBox.getStringToDate(edt_deadline.getText().toString()) / 1000 + "",
                            UtilBox.getStringToDate(edt_startTime.getText().toString()) / 1000 + ""
                    );
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        initToolbar();

        adpt_publishPicture = new PublishPictureAdapter(this, new ArrayList<>());
        gv.setAdapter(adpt_publishPicture);

        tv_space.setOnTouchListener((v, event) -> {
            UtilBox.toggleSoftInput(edt_desc, false);

            return false;
        });

        sb_grade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0: tv_grade.setText("D");  grade = 5;  break;
                    case 1: tv_grade.setText("C");  grade = 4;  break;
                    case 2: tv_grade.setText("B");  grade = 3;  break;
                    case 3: tv_grade.setText("A");  grade = 2;  break;
                    case 4: tv_grade.setText("S");  grade = 1;  break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        taskPresenter = new TaskPresenterImpl(this);
    }

    /**
     * 设置返回结果并关掉当前aty
     */
    private void setResultAndFinish(String taskID) {
        Intent intent = new Intent();
        intent.putExtra("grade", tv_grade.getText().toString());
        intent.putExtra("content", edt_desc.getText().toString());
        intent.putExtra("pictureList", adpt_publishPicture.pictureList);
        intent.putExtra("executor", MemberListAdapter.memberList.get(executor).getMid());
        intent.putExtra("supervisor", MemberListAdapter.memberList.get(supervisor).getMid());
        intent.putExtra("auditor", MemberListAdapter.memberList.get(auditor).getMid());
        intent.putExtra("taskID", taskID);
        intent.putExtra("deadline",
                UtilBox.getStringToDate(edt_deadline.getText().toString()));
        intent.putExtra("publish_time",
                UtilBox.getStringToDate(edt_startTime.getText().toString()));
        intent.putExtra("create_time",
                Calendar.getInstance(Locale.CHINA).getTimeInMillis());

        setResult(RESULT_OK, intent);
        finish();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @OnClick({R.id.edt_deadline, R.id.edt_startTime,
            R.id.edt_executor, R.id.edt_supervisor, R.id.edt_auditor})
    public void onClick(View view) {
        MyDate myDate;
        DateDialog dateDialog;

        switch (view.getId()) {

            case R.id.edt_deadline:
                isDeadline = true;

                myDate = getMyDate();
                dateDialog = new DateDialog(this, this,
                        myDate.getYear(), myDate.getMonth(), myDate.getDay());
                dateDialog.show();
                break;

            case R.id.edt_startTime:
                isStartTime = true;

                myDate = getMyDate();
                dateDialog = new DateDialog(this, this,
                        myDate.getYear(), myDate.getMonth(), myDate.getDay());
                dateDialog.show();
                break;

            case R.id.edt_executor:

                MemberListAdapter.getMemberList(this, new MemberListAdapter.GetMemberCallBack() {
                    @Override
                    public void successCallback() {
                        showMemberList(R.id.edt_executor, "executor");
                    }

                    @Override
                    public void failedCallback() {
                    }
                });

                break;

            case R.id.edt_supervisor:

                MemberListAdapter.getMemberList(this, new MemberListAdapter.GetMemberCallBack() {
                    @Override
                    public void successCallback() {
                        showMemberList(R.id.edt_supervisor, "supervisor");
                    }

                    @Override
                    public void failedCallback() {
                    }
                });

                break;

            case R.id.edt_auditor:

                MemberListAdapter.getMemberList(this, new MemberListAdapter.GetMemberCallBack() {
                    @Override
                    public void successCallback() {
                        showMemberList(R.id.edt_auditor, "auditor");
                    }

                    @Override
                    public void failedCallback() {
                    }
                });

                break;
        }
    }

    /**
     * 弹出成员选择对话框
     *
     * @param viewID   显示控件的Id
     * @param whichExt 正在选择哪个
     */
    private void showMemberList(final int viewID, final String whichExt) {
        String[] members = new String[MemberListAdapter.memberList.size()];

        for (int i = 0; i < MemberListAdapter.memberList.size(); i++) {
            members[i] = MemberListAdapter.memberList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(TaskPublishActivity.this);
        builder.setItems(members, (dialog, which) -> {
            Member member = MemberListAdapter.memberList.get(which);

            ((TextView) TaskPublishActivity.this.findViewById(viewID)).setText(member.getName());

            switch (whichExt) {
                case "executor":
                    executor = which;
                    break;

                case "supervisor":
                    supervisor = which;
                    break;

                case "auditor":
                    auditor = which;
                    break;
            }
        });

        builder.setCancelable(true);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * 获取日期
     *
     * @return 当前日期或已设定的日期
     */
    private MyDate getMyDate() {
        int year, month, day;

        if ((isDeadline && year_deadline != 0)
                || (isStartTime && year_deadline != 0 && year_startTime == 0)) {
            year = year_deadline;
            month = month_deadline;
            day = day_deadline;
        } else if ((isDeadline) || (isStartTime && year_startTime == 0)) {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        } else {
            year = year_startTime;
            month = month_startTime;
            day = day_startTime;
        }

        return new MyDate(year, month, day);
    }

    /**
     * 获取时间
     *
     * @return 当前时间或已设定的时间
     */
    private MyTime getMyTime() {
        int hour, minute;

        if ((isDeadline && hour_deadline != 0)
                || (isStartTime && hour_deadline != 0 && hour_startTime == 0)) {
            hour = hour_deadline;
            minute = minute_deadline;
        } else if ((isDeadline) || (isStartTime && hour_startTime == 0)) {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        } else {
            hour = hour_startTime;
            minute = minute_startTime;
        }

        return new MyTime(hour, minute);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

        if (isDeadline) {
            year_deadline = year;
            month_deadline = monthOfYear;
            day_deadline = dayOfMonth;
        } else if (isStartTime) {
            year_startTime = year;
            month_startTime = monthOfYear;
            day_startTime = dayOfMonth;
        }

        MyTime myTime = getMyTime();
        TimeDialog timeDialog = new TimeDialog(this, this, myTime.getHour(), myTime.getMinute(), true);
        timeDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (minute < 10) {
            date += " " + hourOfDay + ":0" + minute;
        } else {
            date += " " + hourOfDay + ":" + minute;
        }

        if (isDeadline) {
            hour_deadline = hourOfDay;
            minute_deadline = minute;

            isDeadline = false;

            edt_deadline.setText(date);
        } else if (isStartTime) {
            hour_startTime = hourOfDay;
            minute_startTime = minute;

            isStartTime = false;

            edt_startTime.setText(date);
        }

        date = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_MULTIPLE_PICTURE:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> path
                            = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

                    adpt_publishPicture.insertPicture(path);
                    gv.setAdapter(adpt_publishPicture);
                    UtilBox.setGridViewHeightBasedOnChildren(gv, false);
                }
                break;

            case Constants.CODE_CHECK_PICTURE:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> path = data.getStringArrayListExtra("pictureList");
                    adpt_publishPicture.insertPicture(path);
                    gv.setAdapter(adpt_publishPicture);
                    UtilBox.setGridViewHeightBasedOnChildren(gv, false);
                }
        }
    }

    @Override
    public void onPublishTaskResult(String result, String info) {
        switch (result) {
            case Constants.SUCCESS:
                // 为了能马上显示出来
                setResultAndFinish(info);
                break;

            default:
                UtilBox.showSnackbar(this, info);
                break;
        }
    }

    @Override
    public void onDeleteTaskResult(String result, String info) {

    }

    @Override
    public void onUpdateTaskResult(String result, String info) {
        switch (result) {
            case Constants.SUCCESS:
                break;

            default:
                UtilBox.showSnackbar(this, info);
                break;
        }
    }
}