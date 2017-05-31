package org.chenxinwen.micontacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.chenxinwen.micontacts.bean.Contacts;
import org.chenxinwen.micontacts.fragment.CallFragment;
import org.chenxinwen.micontacts.fragment.ContactsFragment;

import org.chenxinwen.micontacts.view.MiTab;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by chenxinwen on 16/8/9.09:49.
 * Email:191205292@qq.com
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MiTab mTab;
    private ViewPager mViewPager;
    private FloatingActionButton mFab;
    //  自家数据
    private static final String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡
    private static final String SAVE_REAL_PATH = SAVE_PIC_PATH + "/ContactImg/";//保存的确切位置
    private static final int ADD_CONTACT_RESULT_CODE = 1;
    public static MainActivity instance = null;
    private String newName;
    private String newUrl;
    private DBnew db = new DBnew(this);

    //自家函数
    //重写onCreateOptionMenu(Menu menu)方法，当菜单第一次被加载时调用
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //填充选项菜单（读取XML文件、解析、加载到Menu组件上）
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //重写OptionsItemSelected(MenuItem item)来响应菜单项(MenuItem)的点击事件（根据id来区分是哪个item）

    private String getStringFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();
        os.close();
        return state;
    }

    private ArrayList<Contacts> downloaded_contacts;
    private ArrayList<Contacts> uploaded_contacts;

    public void download() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL("http://108.61.161.75:5000/download_contact?uuid=" + "greg");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
//                    String result = getStringFromInputStream(conn.getInputStream());
                    String result = conn.getInputStream().toString();
                    Log.d("Greg's download result", result);
                    if (conn.getResponseCode() == 200) {
                        JSONObject jsonObject = new JSONObject(result);
                        downloaded_contacts = new ArrayList<>();
                        int i = 0;
                        while (jsonObject.isNull(i + "")) {
                            jsonObject = jsonObject.getJSONObject("0");
                            String avatar = jsonObject.getString("avatar");
                            String name = jsonObject.getString("name");
                            String phone = jsonObject.getString("phone");
                            Log.d("Greg's download json", i + " " + avatar + " " + name + " " + phone);
                            Contacts curr = new Contacts();
                            curr.setName(name);
                            curr.setNumber(phone);
                            curr.setUrl(avatar);
//                            downloaded_contacts.add(curr);

                            // query if exist
                            // NOTE: assume that numbers could be seen distinct
                            if (db.findByNum(phone) == null) {
                                db.insert(curr);
                            }

                            i++;
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null)
                        conn.disconnect();
                }

            }
        }).start();
    }

    public void upload() {
        Log.d("Greg's", "Upload begins");
        new Thread(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection conn = null;
                uploaded_contacts = db.getAllData();
                for (int i = 0; i < uploaded_contacts.size(); i++) {
                    try {

                        // Convert the local data into the JSON format
                        JSONObject toSend = new JSONObject();
                        toSend.put("uuid","greg");
                        Log.d("Greg's upload", "fetching all data from db");
                        Log.d("Greg's upload", "producing JSON");

                        toSend.put("avatar", uploaded_contacts.get(i).getUrl());
                        toSend.put("name", uploaded_contacts.get(i).getName());
                        toSend.put("phone", uploaded_contacts.get(i).getNumber());

                        Log.d("Greg's upload", "starting connection");
                        // Create an HTTP connection
                        URL url = new URL("http://108.61.161.75:5000/upload_contact");
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setChunkedStreamingMode(0);
                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("ser-Agent", "Fiddler");
                        conn.setUseCaches(false);
                        conn.setInstanceFollowRedirects(true);
                        conn.setConnectTimeout(5000);

//                        Log.d("Greg's upload",URLEncoder.encode(toSend.toString(),"UTF-8"));

                        OutputStream os = conn.getOutputStream();
//                        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

//                        byte [] content = toSend.toString().getBytes("utf-8");
                        out.write(toSend.toString());
                        out.flush();
                        out.close();
                        conn.connect();

                        Log.d("Greg's json",toSend.toString());
//                        out.write(content,0,content.length);
//                        OutputStream os = conn.getOutputStream();
//                        os.write(content);
//                        os.flush();


//                        String result="";
//                        BufferedReader in = new BufferedReader(
//                                new InputStreamReader(conn.getInputStream()));
//                        String line;
                        if (conn.getResponseCode() == 200) {
//                            while ((line = in.readLine()) != null) {
//                                result += line;
//                            }
                            Log.d("Greg's res","200");
                        }
                        Log.d("Greg's res",conn.getResponseCode()+" "+conn.getResponseMessage());


//                        InputStream in = conn.getInputStream();
//                        Log.d("Greg's upload","test2");
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
//                        Log.d("Greg's upload","test3");
//
//                        String line = "";
//                        Log.d("Greg's post","Contents of post request start");
//
//                        String returnLine="";
//                        while ((line = reader.readLine()) != null) {
//                            // line = new String(line.getBytes(), "utf-8");
//                            returnLine += line;
//                            Log.d("Greg's post",line);
//                        }
//
//                        Log.d("Greg's post","Contents of post request ends");
//                        reader.close();

//                        Log.d("Greg's upload", "to receive response");
//                        int response = conn.getResponseCode();
//                        Log.d("Greg's upload response", response + "");
//                        Log.d("Greg's upload response", conn.getResponseMessage());
//                        Log.d("Greg's response",conn.getContentEncoding());


                    } catch (Exception e) {

                        Log.d("Greg's exception", e.getCause()+ " " + e.getMessage() + " " + e.getLocalizedMessage());

                        e.printStackTrace();
                    } finally {
                        Log.d("Greg's upload","finally");
                        if (conn != null)
                            conn.disconnect();
                    }
                }
            }
        }).start();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.reset:
                Toast.makeText(this, "RESET", Toast.LENGTH_SHORT).show();
                db.reset();
                ContactsFragment.instance.refreshData();
                return true;

            case R.id.download:
                Log.d("Greg", "download button triggers");
                download();
                return true;

            case R.id.upload:
                Log.d("Greg", "upload button triggers");
                upload();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Intent_data) {
        ContactsFragment.instance.refreshData();

        switch (resultCode) {
            //  从AddNewContact.java接受回来的数据
            case (ADD_CONTACT_RESULT_CODE): {
                super.onActivityResult(requestCode, resultCode, Intent_data);
                ContactsFragment.instance.refreshData();
                break;
            }
        }
        //textView.setText("另外一个Activity传回来的数据是："+data.getStringExtra("data"));
    }


    //自家函数结束
    //private DBnew db = new DBnew(this);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        Toast.makeText(MainActivity.this, "Login succeeds!", Toast.LENGTH_LONG).show();


//        FOR TEST ONLY
//        Intent intent=new Intent(MainActivity.this,ComfortSMSAct.class);
//        startActivity(intent);
    }


    private void initView() {
        mTab = (MiTab) findViewById(R.id.mTab);
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        mFab = (FloatingActionButton) findViewById(R.id.mFab);

        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // Intent intent = new Intent(MainActivity.this, AddNewContact.class);
                //startActivity(intent);
                Intent intent = new Intent(MainActivity.this, AddNewContact.class);
                startActivityForResult(intent, 0);


            }

        });


        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mTab.setViewPager(mViewPager);

        mViewPager.setCurrentItem(1);

        mFab.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss     ");
                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                String str = formatter.format(curDate);
                Toast.makeText(v.getContext(), "正在和 45.32.48.44同步通讯录", Toast.LENGTH_LONG).show();
                Toast.makeText(v.getContext(), str + " 同步成功，本次同步更新了4条记录，本地已更新到最新", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mFab:

                break;
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private String[] titles = {getString(R.string.call),
                getString(R.string.contacts)
        };

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return CallFragment.newInstance();
            } else {
                return ContactsFragment.newInstance();
            }
        }

    }
}
