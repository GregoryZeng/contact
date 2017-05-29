package org.chenxinwen.micontacts.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thinkcool.circletextimageview.CircleTextImageView;

import org.chenxinwen.micontacts.R;
import org.chenxinwen.micontacts.bean.RecordEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxinwen on 16/8/9.10:07.
 * Email:191205292@qq.com
 */

public class Call_for_one extends RecyclerView.Adapter<Call_for_one.MyViewHolder> {
    private List<RecordEntity> recordEntityList = new ArrayList<>();
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                parent.getContext()).inflate(R.layout.adapter_call_cluster, parent,
                false));
        holder.recyclerview2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int position=holder.getAdapterPosition();
                RecordEntity enti=recordEntityList.get(position);
            }
        });
        return holder;
    }
    public Call_for_one(List<RecordEntity> myEntityList){
        recordEntityList=myEntityList;
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        try {
            if (recordEntityList.get(position).getName().isEmpty()) {
                holder.mName.setVisibility(View.GONE);
            } else {
                holder.mName.setVisibility(View.VISIBLE);
                holder.mName.setText(recordEntityList.get(position).getName());
            }
        }catch (Exception e){
            holder.mName.setVisibility(View.GONE);
        }


        holder.mNumber.setText(recordEntityList.get(position).getNumber());

        if (recordEntityList.get(position).getType() == 1) {
            //incoming
            holder.mTime.setText(recordEntityList.get(position).getlDate()
                    + " 呼入" + recordEntityList.get(position).getDuration() + "秒");
            holder.mName.setTextColor(Color.parseColor("#000000"));
            holder.mNumber.setTextColor(Color.parseColor("#666666"));
        } else if (recordEntityList.get(position).getType() == 2) {
            //outgoing
            holder.mTime.setText(recordEntityList.get(position).getlDate()
                    + " 呼出" + recordEntityList.get(position).getDuration() + "秒");
            holder.mName.setTextColor(Color.parseColor("#000000"));
            holder.mNumber.setTextColor(Color.parseColor("#666666"));
        } else if (recordEntityList.get(position).getType() == 3) {
            //missed
            holder.mTime.setText(recordEntityList.get(position).getlDate());

            holder.mName.setTextColor(Color.parseColor("#e63c31"));
            holder.mNumber.setTextColor(Color.parseColor("#e63c31"));
        } else if (recordEntityList.get(position).getType() == 4) {
            //voicemails
            holder.mTime.setText(recordEntityList.get(position).getlDate());
            holder.mNumber.setTextColor(Color.parseColor("#e63c31"));
            holder.mName.setTextColor(Color.parseColor("#e63c31"));
        }


        try {
            if (recordEntityList.get(position).getName().substring(
                    recordEntityList.get(position).getName().length() - 1).equals("(") ||
                    recordEntityList.get(position).getName().substring(recordEntityList.get(position).getName().length() - 1).equals(")") ||
                    recordEntityList.get(position).getName().substring(recordEntityList.get(position).getName().length() - 1).equals("[") ||
                    recordEntityList.get(position).getName().substring(recordEntityList.get(position).getName().length() - 1).equals("]") ||
                    recordEntityList.get(position).getName().substring(recordEntityList.get(position).getName().length() - 1).equals("（") ||
                    recordEntityList.get(position).getName().substring(recordEntityList.get(position).getName().length() - 1).equals("）") ||
                    recordEntityList.get(position).getName().substring(recordEntityList.get(position).getName().length() - 1).equals("【") ||
                    recordEntityList.get(position).getName().substring(recordEntityList.get(position).getName().length() - 1).equals("】")) {
                holder.mUserPhoto.setText(recordEntityList.get(position).getName().substring(recordEntityList.get(position).getName().length() - 2, recordEntityList.get(position).getName().length() - 1));
            } else {
                holder.mUserPhoto.setText(recordEntityList.get(position).getName().substring(recordEntityList.get(position).getName().length() - 1));
            }
        }catch (Exception e){
            holder.mUserPhoto.setText("Mi");
        }


    }

    @Override
    public int getItemCount() {
        return recordEntityList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mLayout;
        CircleTextImageView mUserPhoto;

        View recyclerview2;
        TextView mName;
        TextView mNumber;
        TextView mTime;

        public MyViewHolder(View view) {
            super(view);
            recyclerview2=view;
            mLayout = (LinearLayout) view.findViewById(R.id.mLayout_cluster);
            mUserPhoto = (CircleTextImageView) view.findViewById(R.id.mUserPhoto_cluster);
            mName = (TextView) view.findViewById(R.id.mName_cluster);
            mNumber = (TextView) view.findViewById(R.id.mNumber_cluster);
            mTime = (TextView) view.findViewById(R.id.mTime_cluster);
        }
    }
}
