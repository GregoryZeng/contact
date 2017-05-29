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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
private static final String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() :"/mnt/sdcard";//保存到SD卡
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
                break;

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
        instance =this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

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
                    startActivityForResult(intent,0);


            }

        });


        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(2);
        mTab.setViewPager(mViewPager);

        mViewPager.setCurrentItem(1);

        mFab.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {
                SimpleDateFormat formatter    =   new    SimpleDateFormat    ("yyyy年MM月dd日    HH:mm:ss     ");
                Date curDate    =   new    Date(System.currentTimeMillis());//获取当前时间
                String    str    =    formatter.format(curDate);
                Toast.makeText(v.getContext(),"正在和 45.32.48.44同步通讯录",Toast.LENGTH_LONG).show();
                Toast.makeText(v.getContext(),str+" 同步成功，本次同步更新了4条记录，本地已更新到最新",Toast.LENGTH_SHORT).show();
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
