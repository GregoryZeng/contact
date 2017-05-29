package org.chenxinwen.micontacts.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thinkcool.circletextimageview.CircleTextImageView;

import org.chenxinwen.micontacts.ContactsinfoActivity;
import org.chenxinwen.micontacts.DividerDecoration;
import org.chenxinwen.micontacts.R;
import org.chenxinwen.micontacts.bean.Contacts;
import org.chenxinwen.micontacts.bean.RecordEntity;
import org.chenxinwen.micontacts.call_cluster;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by chenxinwen on 16/8/9.10:07.
 * Email:191205292@qq.com
 */
public class CallFragment extends Fragment {


    private RecyclerView recyclerView;
    private List<RecordEntity> recordEntityList = new ArrayList<>();


    public static CallFragment newInstance() {
        return new CallFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);
//        CallLog.Calls.INCOMING_TYPE
        initView(view);
        return view;
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recordEntityList.size() > 0)
            return;
        checkPermission();
    }

    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;
    private static final int CALL_PHONE_PERMISSIONS_REQUEST = 2;
    private static int globlePositon;
    private void checkPermission() {
        //版本判断
        if (Build.VERSION.SDK_INT >= 23) {
            //减少是否拥有权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_CALL_LOG);

            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CALL_LOG}, READ_CONTACTS_PERMISSIONS_REQUEST);


            } else {
                initRecord();
            }
        } else {
            initRecord();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initRecord();
                return;
            } else {
                Toast.makeText(getActivity(), "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == CALL_PHONE_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                call(globlePositon);
                return;
            } else {
                Toast.makeText(getActivity(), "Read call phone denied", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initRecord() {
        Uri uri = CallLog.Calls.CONTENT_URI;
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CALL_LOG}, READ_CONTACTS_PERMISSIONS_REQUEST);
            return;
        }

        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {

                RecordEntity recordEntity = new RecordEntity();
                //号码
                recordEntity.setNumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
                //呼叫类型
                recordEntity.setType(Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))));


                SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))));
                //呼叫时间
                recordEntity.setlDate(sfd.format(date));
                //联系人
                recordEntity.setName(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)));

                Log.e("---------->", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))+"");

                //通话时间,单位:s
                recordEntity.setDuration(cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)));

                recordEntityList.add(recordEntity);
            } while (cursor.moveToNext());
        }
        Collections.sort(recordEntityList, new Comparator() {
            @Override

            public int compare(Object obj1, Object obj2) {
                RecordEntity o1=(RecordEntity) obj1;
                RecordEntity o2=(RecordEntity) obj2;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date1=new Date();
                Date date2=new Date();
                try {
                    date1 = sdf.parse(o1.getlDate());
                    date2 = sdf.parse(o2.getlDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date1.before(date2)) {
                    return 1;
                } else {
                    return -1;
                }

            }

        });
        getActivity().startManagingCursor(cursor);//cursor的生命周期托管给activity


        //设置列表数据和浮动header
        final LinearLayoutManager layoutManager = new
                LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        RecordAdapter recordAdapter = new RecordAdapter();
        recyclerView.setAdapter(recordAdapter);

        // Add decoration for dividers between list items
        recyclerView.addItemDecoration(new DividerDecoration(getActivity()));


    }
    private  void call(int position){
        RecordEntity enti = recordEntityList.get(position);
        String phone = enti.getNumber();
        Intent intent2 = new Intent(Intent.ACTION_CALL);//创建一个意图对象，用来激发拨号的Activity
        intent2.setData(Uri.parse("tel:" + phone));
        startActivity(intent2);
    }

    class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                    getActivity()).inflate(R.layout.adapter_call, parent,
                    false));

//            holder.itemView.setOnClickListener(new View.OnClickListener(){
//                public void onClick(View v){
//                    int pos=holder.getAdapterPosition();
//                    RecordEntity record=recordEntityList.get(pos);
//                    Toast.makeText(v.getContext(),"you clicked"+record.getDuration(),Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getActivity(),ContactsinfoActivity.class);
//
//                    startActivity(intent);
//                }
//            });


            holder.InfoButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int position=holder.getAdapterPosition();
                    RecordEntity enti=recordEntityList.get(position);
                    Intent intent=new Intent(getActivity(),ContactsinfoActivity.class);
                    String phoneNumber=enti.getNumber();
                    List<RecordEntity> myList = new ArrayList<>();
                    for (int j = 0; j < recordEntityList.size(); j++) {

                        if(recordEntityList.get(j).getNumber().equals(phoneNumber))
                            myList.add(recordEntityList.get(j));
                    }

                    intent.putExtra("List",(Serializable) myList);
                    intent.putExtra("PhoneNumber",phoneNumber);
                    startActivity(intent);
                }
            });
            holder.recyclerview2.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int position=holder.getAdapterPosition();
                    globlePositon=position;
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // 没有获得授权，申请授权
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                Manifest.permission.CALL_PHONE)) {
                            // 返回值：
//                          如果app之前请求过该权限,被用户拒绝, 这个方法就会返回true.
//                          如果用户之前拒绝权限的时候勾选了对话框中”Don’t ask again”的选项,那么这个方法会返回false.
//                          如果设备策略禁止应用拥有这条权限, 这个方法也返回false.
                            // 弹窗需要解释为何需要该权限，再次请求授权
                            Toast.makeText(v.getContext(), "请授权！", Toast.LENGTH_LONG).show();
//
//                            // 帮跳转到该应用的设置界面，让用户手动授权
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package", "org.chenxinwen.micontacts.fragment;", null);
//                            intent.setData(uri);
//                            startActivity(intent);
                        } else {
                            // 不需要解释为何需要该权限，直接请求授权
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE},CALL_PHONE_PERMISSIONS_REQUEST );
                        }
                    } else {
                        // 已经获得授权，可以打电

                        call(globlePositon);

                    }
                }
            });
            return holder;
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
            ImageButton InfoButton;
            public MyViewHolder(View view) {
                super(view);
                recyclerview2=view;
                mLayout = (LinearLayout) view.findViewById(R.id.mLayout);
                mUserPhoto = (CircleTextImageView) view.findViewById(R.id.mUserPhoto);
                mName = (TextView) view.findViewById(R.id.mName);
                mNumber = (TextView) view.findViewById(R.id.mNumber);
                mTime = (TextView) view.findViewById(R.id.mTime);
                InfoButton=(ImageButton) view.findViewById(R.id.Contact_info);
            }
        }
    }

}
