package com.jiubai.taskmoment.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.bean.Member;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.ui.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.widget.RippleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class MemberListAdapter extends BaseAdapter {
    public static List<Member> memberList;
    private Context context;
    private ListView listView;

    public MemberListAdapter(Context context, String memberInfo, ListView listView) {
        try {
            this.context = context;
            this.listView = listView;

            memberList = new ArrayList<>();

            JSONObject memberJson = new JSONObject(memberInfo);

            if (!"null".equals(memberJson.getString("info"))) {
                JSONArray memberArray = memberJson.getJSONArray("info");

                for (int i = 0; i < memberArray.length(); i++) {
                    JSONObject obj = new JSONObject(memberArray.getString(i));

                    Member member = new Member(obj.getString("real_name"),
                            obj.getString("mobile"), obj.getString("id"), obj.getString("mid"));

                    if ("null".equals(member.getName()) || "".equals(member.getName())) {
                        member.setName(member.getMobile());
                    }

                    memberList.add(member);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public Object getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_body_member, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Member member = memberList.get(position);

        if (!Config.COMPANY_CREATOR.equals(Config.MID)) {
            holder.btn.setVisibility(View.GONE);
        } else {
            holder.btn.setOnClickListener(v -> {
                if (Config.COMPANY_CREATOR.equals(member.getMid())) {
                    UtilBox.showSnackbar(context, "不能移除公司创建者");
                    return;
                }

                final MaterialDialog dialog = new MaterialDialog(context);
                dialog.setMessage("真的要移除该成员吗？")
                        .setPositiveButton("真的", v1 -> {
                            if (!Config.IS_CONNECTED) {
                                UtilBox.showSnackbar(context, R.string.cant_access_network);
                                return;
                            }

                            dialog.dismiss();

                            String[] key = {"id", "mid"};
                            String[] value = {member.getId(), member.getMid()};

                            VolleyUtil.requestWithCookie(Urls.DELETE_MEMBER, key, value,
                                    response -> deleteCheck(position, response),
                                    volleyError -> UtilBox.showSnackbar(context, "删除失败，请重试"));
                        })
                        .setNegativeButton("我手滑了", v1 -> {
                            dialog.dismiss();
                        })
                        .setCanceledOnTouchOutside(true)
                        .show();
            });
        }

        holder.rv.setOnRippleCompleteListener(rippleView -> {
            Intent intent = new Intent(context, PersonalTimelineActivity.class);
            intent.putExtra("mid", member.getMid());
            context.startActivity(intent);
        });

        holder.tv_nickname.setText(member.getName());
        ImageLoader.getInstance().displayImage(
                UtilBox.getThumbnailImageName(Urls.MEDIA_CENTER_PORTRAIT + member.getMid() + ".jpg",
                        UtilBox.dip2px(context, 45),
                        UtilBox.dip2px(context, 45))
                        + "?t=" + Config.TIME, holder.iv);

        return convertView;
    }

    public interface GetMemberCallBack {
        void successCallback();

        void failedCallback();
    }

    public static Member getMemberWithID(String mid) {
        Member member = new Member("", "", "", "");

        for (Member m : memberList) {
            if (m.getMid().equals(mid)) {
                member = m;
                break;
            }
        }

        return member;
    }

    /**
     * 获取成员列表
     */
    public static void getMemberList(final Context context, final GetMemberCallBack callBack) {
        if (memberList != null && memberList.isEmpty()) {
            callBack.successCallback();
            return;
        }

        if (!Config.IS_CONNECTED) {
            callBack.failedCallback();
            UtilBox.showSnackbar(context, R.string.cant_access_network);
            return;
        }

        VolleyUtil.requestWithCookie(Urls.GET_MEMBER + Config.CID, null, null,
                response -> {

                    try {
                        JSONObject responseJson = new JSONObject(response);

                        String responseStatus = responseJson.getString("status");

                        if (Constants.SUCCESS.equals(responseStatus)) {
                            memberList = new ArrayList<>();

                            JSONObject memberJson = new JSONObject(response);

                            if (!"null".equals(memberJson.getString("info"))) {
                                JSONArray memberArray = memberJson.getJSONArray("info");

                                for (int i = 0; i < memberArray.length(); i++) {
                                    JSONObject obj = new JSONObject(memberArray.getString(i));

                                    Member member = new Member(
                                            obj.getString("real_name"),
                                            obj.getString("mobile"),
                                            obj.getString("id"),
                                            obj.getString("mid"));

                                    if ("null".equals(member.getName()) || "".equals(member.getName())) {
                                        member.setName(member.getMobile());
                                    }

                                    memberList.add(member);
                                }

                            }

                            callBack.successCallback();
                        } else {
                            callBack.failedCallback();
                            UtilBox.showSnackbar(context, "获取成员列表失败");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                volleyError -> {
                    callBack.failedCallback();
                    UtilBox.showSnackbar(context, "获取成员列表失败");
                });
    }

    /**
     * 检查删除返回的json
     *
     * @param position 需要删除的成员的Position
     * @param response 通信返回的json
     */
    private void deleteCheck(int position, String response) {
        try {
            JSONObject json = new JSONObject(response);
            String status = json.getString("status");

            if (Constants.SUCCESS.equals(status)) {
                memberList.remove(position);

                notifyDataSetChanged();

                UtilBox.setListViewHeightBasedOnChildren(listView);
            } else {
                UtilBox.showSnackbar(context, json.getString("info"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class ViewHolder {

        @Bind(R.id.iv_item_body_member) ImageView iv;
        @Bind(R.id.rv_item_body_member) RippleView rv;
        @Bind(R.id.tv_nickname) TextView tv_nickname;
        @Bind(R.id.btn_item_body_member) Button btn;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
