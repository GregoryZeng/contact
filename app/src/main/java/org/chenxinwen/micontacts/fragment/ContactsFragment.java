package org.chenxinwen.micontacts.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.quicksidebar.QuickSideBarTipsView;
import com.bigkoo.quicksidebar.QuickSideBarView;
import com.bigkoo.quicksidebar.listener.OnQuickSideBarTouchListener;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.chenxinwen.micontacts.CharacterParser;
import org.chenxinwen.micontacts.ContactsinfoActivity;
import org.chenxinwen.micontacts.DBnew;
import org.chenxinwen.micontacts.DividerDecoration;
import org.chenxinwen.micontacts.HanziToPinyin;
import org.chenxinwen.micontacts.MainActivity;
import org.chenxinwen.micontacts.MyApplication;
import org.chenxinwen.micontacts.R;
import org.chenxinwen.micontacts.adapter.ContactsListAdapter;
import org.chenxinwen.micontacts.bean.Contacts;
import org.kymjs.kjframe.KJBitmap;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import android.os.Handler;

import java.util.logging.LogRecord;

import static android.R.attr.button;


/**
 * Created by chenxinwen on 16/8/9.10:09.
 * Email:191205292@qq.com
 */
public class ContactsFragment extends Fragment implements OnQuickSideBarTouchListener {


    private final int READ_LOCAL_CONTACTS =1;
    private final int UPDATE_CONTACTS =2;
    private Context context = MyApplication.getInstance();
    private ContactsListWithHeadersAdapter adapter = new ContactsListWithHeadersAdapter();
    public static ContactsFragment instance = null;
    private RecyclerView recyclerView;
    private QuickSideBarTipsView quickSideBarTipsView;
    private QuickSideBarView quickSideBarView;
    private HashMap<String, Integer> letters = new HashMap<>();
    private ArrayList<Contacts> contacts = new ArrayList<Contacts>();
    private CharacterParser characterParser;
    private DBnew db = new DBnew(MainActivity.instance);
    private TextWatcher mWatcher;
    private EditText mSearchInput;
    private KJBitmap kjb = new KJBitmap();
    private ArrayList<Contacts> tempDatas = new ArrayList<>();
    //
    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        instance = this;
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        characterParser = CharacterParser.getInstance();
        initView(view);

        if (contacts.size() == 0)
            checkPermission();
        return view;
    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ_LOCAL_CONTACTS: {
                    upDateFromDB();
                    updateState();
                    break;
                }
                case UPDATE_CONTACTS:
                {
                    Log.d("refreshData","thread");
                    contacts.clear();
                    for(Contacts contact:tempDatas)
                    {
                        contacts.add(contact);
                    }
                    updateState();

                    break;
                }
                default:
                    break;
            }
        }
    };

    private void updateState() {
        ArrayList<String> customLetters = new ArrayList<>();
        letters.clear();
        int position = 0;
        for (Contacts contact : contacts) {
            String letter = contact.getSortKey();
            //如果没有这个key则加入并把位置也加入
            if (!letters.containsKey(letter)) {
                letters.put(letter, position);
                customLetters.add(letter);
            }
            position++;
        }

        //不自定义则默认26个字母
        // quickSideBarView.setLetters(customLetters);
        adapter.clear();
        adapter.addAll(contacts);

        adapter.notifyDataSetChanged();
    }

    private void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        quickSideBarTipsView = (QuickSideBarTipsView) view.findViewById(R.id.quickSideBarTipsView);
        quickSideBarView = (QuickSideBarView) view.findViewById(R.id.quickSideBarView);
        mSearchInput = (EditText) view.findViewById(R.id.school_friend_member_search_input);
        //设置监听
        quickSideBarView.setOnQuickSideBarTouchListener(this);
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("onTextChanged----->", "有" + count + "个字符仅仅从" + start + "开始要替换" + before + "个旧的字符");
                Log.e("onTextChanged----->", String.valueOf(s));
                upDateFromDB();
                ArrayList<Contacts> tmp = new ArrayList<Contacts>();
                for (int i = 0; i < contacts.size(); i++) {
                    Contacts data = contacts.get(i);
                    if (data.getName().contains(s) || data.getPinyin().contains(s)) {
                        tmp.add(data);
                    }
                    else{
                        ArrayList<String> tags = data.getTag();
                        for(String tag:tags)
                        {
                            if(tag.contains(s))
                                tmp.add(data);
                        }
                    }
                }
                contacts = tmp;
                updateState();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                //  Log.e("beforeTextChanged----->", "有"+count+"个字符从"+start+" 位置开始  已经被"+ after+"个字符所替换");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public void onResume() {
        //refreshData();
        Log.d("onresume","fragment");
        super.onResume();/*
        if (contacts.size() > 0)
            return;
        checkPermission();*/
    }

    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;

    private void checkPermission() {
        //版本判断
        if (Build.VERSION.SDK_INT >= 23) {
            //减少是否拥有权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_CONTACTS);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                // Show our own UI to explain to the user why we need to read the contacts
                // before actually requesting the permission and showing the default UI

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSIONS_REQUEST);

            } else {
                initContants();
            }
        } else {
            initContants();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initContants();
                return;
            } else {
                Toast.makeText(getActivity(), "Read Contacts permission denied", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void upDateFromDB() {
        contacts.clear();
        ArrayList<Contacts> tmpDatas = new ArrayList<>();
        tmpDatas = db.getAllData();
        for (int i = 0; i < tmpDatas.size(); i++) {
            //   datas.add(tmpDatas.get(i));
            Contacts data = tmpDatas.get(i);
            String sortKey = data.getPinyin().toUpperCase();
            if (sortKey.length() > 0) sortKey = sortKey.substring(0, 1);
            else sortKey = "#";
            if (!sortKey.matches("[A-Z]")) {
                sortKey = "#";
            }
            data.setSortKey(sortKey);
            Log.d("获得全部数据ID：", String.valueOf(data.getId()));
            Log.d("获得全部数据名字：", data.getName());
            Log.d("获得全部数据url：", data.getUrl());
            contacts.add(data);
        }
        Collections.sort(contacts);
    }

    public void newrefreshData() {
        Log.d("refreshData","normal");
        upDateFromDB();
        updateState();
    }
    public void refreshData() {
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                tempDatas = db.getAllData();
                for (int i = 0; i < tempDatas.size(); i++) {
                    //   datas.add(tmpDatas.get(i));
                    Contacts data = tempDatas.get(i);
                    String sortKey = data.getPinyin().toUpperCase();
                    if (sortKey.length() > 0) sortKey = sortKey.substring(0, 1);
                    else sortKey = "#";
                    if (!sortKey.matches("[A-Z]")) {
                        sortKey = "#";
                    }
                    data.setSortKey(sortKey);
                }
                Collections.sort(tempDatas);
                Message message = new Message();
                message.what = UPDATE_CONTACTS;
                mHandler.sendMessage(message);
            }

        });
        updateThread.start();
        /*upDateFromDB();
        updateState();*/
        // recyclerView.setAdapter(adapter);


        // Add decoration for dividers between list items
        // recyclerView.addItemDecoration(new DividerDecoration(getActivity()));
    }

    private void initContants() {
        Log.d("initContact","run");
        upDateFromDB();
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("initContants","2");
                HashMap<Integer, Boolean> ID_map = new HashMap<Integer, Boolean>();
                String id = "";
                String name = "";
                String phoneNumber = "";
                String email = "";
                //ContentResolver contentResolver = this.getContentResolver();
                Cursor cursor = getActivity().getContentResolver().query(android.provider.ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);
                while (cursor.moveToNext()) {
                    id = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID));
                    name = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME));

                    //Fetch Phone Number
                    Cursor phoneCursor = getActivity().getContentResolver().query(
                            android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(
                                phoneCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //System.out.println("id="+id+" name="+name+" phoneNumber="+phoneNumber);
                    }
                    phoneCursor.close();

                    //Fetch email
                    Cursor emailCursor = getActivity().getContentResolver().query(
                            android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null, android.provider.ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + id, null, null);
                    while (emailCursor.moveToNext()) {
                        email = emailCursor.getString(
                                emailCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Email.DATA));
                        // System.out.println("id="+id+" name="+name+" email="+email);
                    }
                    emailCursor.close();
                    Contacts contact = new Contacts();
                    //  Log.d("get num:", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

                    Random random = new Random();
                    int tmpID = random.nextInt(100000) + 10;
                    while (ID_map.containsKey(tmpID)) {
                        tmpID = random.nextInt(100000) + 10;
                    }
                    ID_map.put(tmpID, true);
                    contact.setId(tmpID);
                    contact.setNumber(phoneNumber);

                    contact.setName(name);
                    contact.setPinyin(HanziToPinyin.getPinYin(name));
                    String sortKey = contact.getPinyin().toUpperCase();
                    if (sortKey.length() > 0) sortKey = sortKey.substring(0, 1);
                    else sortKey = "#";
                    if (!sortKey.matches("[A-Z]")) {
                        sortKey = "#";
                    }
                    contact.setSortKey(sortKey);
                    db.insert(contact);
                    contacts.add(contact);
                }
                if(Build.VERSION.SDK_INT < 14) {
                    cursor.close();
                }
                getActivity().startManagingCursor(cursor);//cursor的生命周期托管给activity

                Message message = new Message();
                message.what = READ_LOCAL_CONTACTS;
                mHandler.sendMessage(message);
            }
        });
        if (contacts.size() == 0) {
            new AlertDialog.Builder(getActivity()).setTitle("读取本地联系人？")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 点击“确认”后的操作
                            Toast.makeText(getActivity(), "正在读取本地联系人...", Toast.LENGTH_LONG).show();
                            thread.start();

                        }
                    })
                    .setNegativeButton("返回", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 点击“返回”后的操作,这里不设置没有任何操作
                        }
                    }).show();

        }
        upDateFromDB();
        letters = new HashMap<>();


        //设置列表数据和浮动header
        LinearLayoutManager layoutManager = new
                LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Add the sticky headers decoration


        ArrayList<String> customLetters = new ArrayList<>();

        int position = 0;
        for (Contacts contact : contacts) {
            String letter = contact.getSortKey();
            //如果没有这个key则加入并把位置也加入
            if (!letters.containsKey(letter)) {
                letters.put(letter, position);
                customLetters.add(letter);
            }
            position++;
        }

        customLetters.clear();
        ;
        customLetters.add("#");
        char letter = 'A';
        for (int i = 0; i < 26; i++) {
            customLetters.add(String.valueOf((char) (letter + i)));
        }

        //不自定义则默认26个字母
        quickSideBarView.setLetters(customLetters);
        adapter.addAll(contacts);
        recyclerView.setAdapter(adapter);

        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(adapter);
        recyclerView.addItemDecoration(headersDecor);

        // Add decoration for dividers between list items
        recyclerView.addItemDecoration(new DividerDecoration(getActivity()));
        Log.d("initContact","end");
    }

    /**
     * 获取sort key的首个字符，如果是英文字母就直接返回，否则返回#。
     *
     * @param sortKeyString 数据库中读取出的sort key
     * @return 英文字母或者#
     */
    private String getSortKey(String sortKeyString) {


        String pinyin = characterParser.getSelling(sortKeyString);

        String key = pinyin.substring(0, 1).toUpperCase();
        //key = sortKeyString.substring(0,1).toUpperCase();
        if (key.matches("[A-Z]")) {
            return key;
        }
        return "#";
    }


    @Override
    public void onLetterChanged(String letter, int position, float y) {
        quickSideBarTipsView.setText(letter, position, y);
        //有此key则获取位置并滚动到该位置
        if (letters.containsKey(letter)) {
            recyclerView.scrollToPosition(letters.get(letter));
        }
    }

    @Override
    public void onLetterTouching(boolean touching) {
        //可以自己加入动画效果渐显渐隐
        quickSideBarTipsView.setVisibility(touching ? View.VISIBLE : View.INVISIBLE);
    }


    private class ContactsListWithHeadersAdapter extends ContactsListAdapter<RecyclerView.ViewHolder>
            implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_contacts_item, parent, false);
            //  return new RecyclerView.ViewHolder(view) {
            //  };
            final RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) {
            };
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    Contacts contact = contacts.get(pos);
                    Toast.makeText(v.getContext(), "you clicked" + contact.getName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), ContactsinfoActivity.class);
                    intent.putExtra("name", contact.getId());
                    startActivityForResult(intent, 0);
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            View itemView = holder.itemView;
            TextView mName = (TextView) itemView.findViewById(R.id.mName);
            //CircleTextImageView mUserPhoto = (CircleTextImageView) itemView.findViewById(R.id.mUserPhoto);
            ImageView mUserPhoto = (ImageView) itemView.findViewById(R.id.mUserPhoto);
            Log.e("Img Url:", getItem(position).getUrl());
            String tmp = "noImg";
            if (getItem(position).getUrl().equals(tmp)) {
                Log.d("noImg:", getItem(position).getUrl());
                mUserPhoto.setImageResource(R.drawable.default_head_rect);
            } else
                kjb.displayWithLoadBitmap(mUserPhoto, getItem(position).getUrl(), R.drawable.default_head_rect);
            LinearLayout mBottomLayout = (LinearLayout) itemView.findViewById(R.id.mBottomLayout);
            if (position < contacts.size() - 1) {
                if (getItem(position).getSortKey().equals(getItem(position + 1).getSortKey())) {
                    mBottomLayout.setVisibility(View.GONE);
                } else {
                    mBottomLayout.setVisibility(View.VISIBLE);
                }
            } else {
                mBottomLayout.setVisibility(View.GONE);
            }


            String name = getItem(position).getName();

            mName.setText(name);
            if (name.substring(name.length() - 1).equals("(") ||
                    name.substring(name.length() - 1).equals(")") ||
                    name.substring(name.length() - 1).equals("[") ||
                    name.substring(name.length() - 1).equals("]") ||
                    name.substring(name.length() - 1).equals("（") ||
                    name.substring(name.length() - 1).equals("）") ||
                    name.substring(name.length() - 1).equals("【") ||
                    name.substring(name.length() - 1).equals("】")) {
                //   mUserPhoto.setText(name.substring(name.length() - 2, name.length() - 1));
            } else {
                //   mUserPhoto.setText(name.substring(name.length() - 1));
            }
            Log.d("onBindViewHolder:", "end");
        }

        @Override
        public long getHeaderId(int position) {
            return getItem(position).getSortKey().charAt(0);
        }

        @Override
        public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_contacts_head, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
            View itemView = holder.itemView;

            TextView mHead = (TextView) itemView.findViewById(R.id.mHead);
            mHead.setText(String.valueOf(getItem(position).getSortKey()));
//            holder.itemView.setBackgroundColor(getRandomColor());
        }


        private int getRandomColor() {
            SecureRandom rgen = new SecureRandom();
            return Color.HSVToColor(150, new float[]{
                    rgen.nextInt(359), 1, 1
            });
        }

    }
}
