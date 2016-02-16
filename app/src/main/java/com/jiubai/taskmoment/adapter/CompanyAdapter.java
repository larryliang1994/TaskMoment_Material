package com.jiubai.taskmoment.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubai.taskmoment.App;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.bean.Company;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.ui.activity.MainActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.ViewHolder> {
    private List<Company> companyList;
    private Context context;
    private boolean isLogin = false;

    public CompanyAdapter(Context context, String companyInfo, boolean isLogin) {
        try {
            this.context = context;

            this.isLogin = isLogin;

            companyList = new ArrayList<>();
            companyList.clear();

            JSONObject companyJson = new JSONObject(companyInfo);

            if (!"null".equals(companyJson.getString("info"))) {
                JSONArray companyArray = companyJson.getJSONArray("info");

                for (int i = 0; i < companyArray.length(); i++) {
                    JSONObject obj = new JSONObject(companyArray.getString(i));
                    companyList.add(new Company(obj.getString("name"),
                            obj.getString("cid"), obj.getString("mid")));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompanyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_company, parent, false));
    }

    @Override
    public void onBindViewHolder(CompanyAdapter.ViewHolder holder, int position) {
        Company company = companyList.get(position);

        holder.textView.setText(company.getName());

        holder.imageView.setImageBitmap(UtilBox.readBitMap(context, Constants.COMPANY_BACKGROUND[position % 3]));
        ImageLoader.getInstance().displayImage(
                UtilBox.getThumbnailImageName(Urls.MEDIA_CENTER_BACKGROUND + company.getCid() + ".jpg",
                        UtilBox.getWidthPixels(context),
                        UtilBox.dip2px(context, 180))
                        + "?t=" + Config.TIME, holder.imageView);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.imageView.getLayoutParams();
        params.width = UtilBox.getWidthPixels(context);
        holder.imageView.setLayoutParams(params);

        holder.imageView.setOnClickListener(v -> {
            Config.COMPANY_NAME = companyList.get(position).getName();
            Config.CID = companyList.get(position).getCid();
            Config.COMPANY_BACKGROUND
                    = Urls.MEDIA_CENTER_BACKGROUND + Config.CID + ".jpg";
            Config.COMPANY_CREATOR = companyList.get(position).getCreator();
            MemberListAdapter.memberList = null;

            // 保存公司信息
            SharedPreferences.Editor editor = App.sp.edit();
            editor.putString(Constants.SP_KEY_COMPANY_NAME, Config.COMPANY_NAME);
            editor.putString(Constants.SP_KEY_COMPANY_ID, Config.CID);
            editor.putString(Constants.SP_KEY_COMPANY_BACKGROUND, Config.COMPANY_BACKGROUND);
            editor.putString(Constants.SP_KEY_COMPANY_CREATOR, Config.COMPANY_CREATOR);
            editor.apply();

            if (isLogin) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
            ((Activity) context).setResult(Activity.RESULT_OK);
            ((Activity) context).finish();
        });
    }

    @Override
    public int getItemCount() {
        return companyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_companyName) TextView textView;
        @Bind(R.id.iv_companyBackground) ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
