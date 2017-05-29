package org.chenxinwen.micontacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.chenxinwen.micontacts.bean.Contacts;
import org.chenxinwen.micontacts.fragment.ContactsFragment;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kymjs.kjframe.KJBitmap;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ContactsinfoActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;

    //    private EditText phoneNumber;
    private String phoneNumStr;
    private TextView weatherText;
    private TextView locationText;
    private ImageView image;
    private Contacts curr_contact;
    private TextView phone_tv;
    private TextView email_tv;
    private int id = 0;
    final int UPDATE_LOCATION = 0;
    final int UPDATE_WEATHER = 1;

    private String cityText = "";

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

    public void getLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Greg", "getloc begins");
                if (phoneNumStr.length() == 11) {
                    StringBuilder buf = new StringBuilder("http://sj.apidata.cn/?mobile=");
                    buf.append(phoneNumStr);
                    HttpURLConnection conn = null;
                    String result = "";
                    try {
                        URL url = new URL(buf.toString());
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        result = getStringFromInputStream(conn.getInputStream());
                        if (conn.getResponseCode() == 200) {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject retData = jsonObject.getJSONObject("data");
                            String province = retData.getString("province");
                            String city = retData.getString("city");
                            String zipcode = retData.getString("zipcode");
                            cityText = city;
                            List<String> msgObj = new ArrayList<>();
                            msgObj.add(province);
                            msgObj.add(city);
                            msgObj.add(zipcode);
                            Message message = new Message();
                            message.what = UPDATE_LOCATION;
                            message.obj = msgObj;
                            handler.sendMessage(message);
                        } else
                            Toast.makeText(ContactsinfoActivity.this, "GET提交失败", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.d("Greg", "getloc exceptions");
                        e.printStackTrace();
                    } finally {
                        Log.d("Greg", "getloc disconnect");
                        if (conn != null) {
                            Log.d("Greg", "conn is not null");
                            conn.disconnect();
                        }
                    }
                }

                StringBuilder buf = new StringBuilder("https://api.seniverse.com/v3/weather/now.json");
                buf.append("?key=gasqs7z7hmulguww");
                try {
                    buf.append("&location=" + URLEncoder.encode(cityText, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                HttpURLConnection conn = null;
                String result = "";
                try {
                    URL url = new URL(buf.toString());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    result = getStringFromInputStream(conn.getInputStream());
                    if (conn.getResponseCode() == 200) {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray retArr = jsonObject.getJSONArray("results");
                        JSONObject retData = retArr.getJSONObject(0).getJSONObject("now");
                        String weatherText = retData.getString("text");
                        String weatherTemp = retData.getString("temperature");
                        List<String> msgObj = new ArrayList<>();
                        msgObj.add(weatherText);
                        msgObj.add(weatherTemp);
                        Message message = new Message();
                        message.what = UPDATE_WEATHER;
                        message.obj = msgObj;
                        handler.sendMessage(message);
                    } else
                        Toast.makeText(ContactsinfoActivity.this, "GET提交失败", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }

            }
        }).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == UPDATE_LOCATION) {
                List<String> msgList = (List<String>) msg.obj;
                locationText.setText(msgList.get(0) + msgList.get(1));
                cityText = msgList.get(1);
            }
            if (msg.what == UPDATE_WEATHER) {
                List<String> msgList = (List<String>) msg.obj;
                weatherText.setText(msgList.get(0) + "  " + msgList.get(1) + "℃");
            }

        }
    };


    private DBnew db = new DBnew(this);
    private KJBitmap kjb = new KJBitmap();

    private void refresh() {
        // db.
        curr_contact = db.find(id);
        collapsingToolbarLayout.setTitle(curr_contact.getName());
        kjb.displayWithLoadBitmap(image, curr_contact.getUrl(), R.drawable.default_head_rect);
        phone_tv.setText(curr_contact.getNumber().toString());
        email_tv.setText(curr_contact.getEmail());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init toolbar
        setContentView(R.layout.activity_contactsinfo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Fetch contact info from db and set phone number and email
        // Note: It is likely that the queried contact does not exist!
        Intent intent = getIntent();

        id = intent.getIntExtra("name", id);

        if (id == 0) {
            curr_contact = new Contacts();
            curr_contact.setName("aaa");
            curr_contact.setNumber("123");
            curr_contact.setEmail("aa@aa");
            Toast.makeText(ContactsinfoActivity.this, "=0", Toast.LENGTH_LONG).show();
        } else {
            curr_contact = db.find(id);
        }

        Log.d("Greg", curr_contact.getName());
        Log.d("Greg", curr_contact.getNumber());
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(curr_contact.getName());
        phone_tv = (TextView) findViewById(R.id.phone_NUM);
        phone_tv.setText(curr_contact.getNumber().toString());
        email_tv = (TextView) findViewById(R.id.email_STR);
        Log.d("Greg mail", "" + curr_contact.getEmail().length());
        Log.d("Greg mail", "ends");
        email_tv.setText(curr_contact.getEmail());
        // Set Photo
        Log.d("Greg photo", curr_contact.getUrl());
        image = (ImageView) findViewById(R.id.image);
        kjb.displayWithLoadBitmap(image, curr_contact.getUrl(), R.drawable.default_head_rect);

//        Picasso.with(this).load(curr_contact.getUrl()).into(image, new Callback() {
//            @Override public void onSuccess() {
//                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
//                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//                    public void onGenerated(Palette palette) {
////                        applyPalette(palette);
//                        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(getResources().getColor(R.color.colorPrimary)));
//                        collapsingToolbarLayout.setStatusBarScrimColor(palette.getMutedColor(getResources().getColor(R.color.colorPrimaryDark)));
//                    }
//                });
//            }
//            @Override public void onError() {
//
//            }
//        });

        // Fetch Location & Weather info
        phoneNumStr = curr_contact.getNumber();
        weatherText = (TextView) findViewById(R.id.weather);
        locationText = (TextView) findViewById(R.id.location);
        getLocation();

        //init Edit fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Intent intent = new Intent(MainActivity.this, AddNewContact.class);
                //startActivity(intent);
                Intent intent = new Intent(ContactsinfoActivity.this, EditContactsinfoActivity.class);
                intent.putExtra("id", curr_contact.getId());
                startActivityForResult(intent, 1);
//                ContactsFragment.instance.refreshData();
            }

        });

        Button blacklist = (Button) findViewById(R.id.ViewRecentCalls);
        blacklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Toast.makeText(ContactsinfoActivity.this, "View Recent Calls!", Toast.LENGTH_LONG).show();
            }

        });
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        setResult(ADD_CONTACT_RESULT_CODE, intent);
        super.finish();

    }

    private static final int ADD_CONTACT_RESULT_CODE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Intent_data) {
        switch (resultCode) {
            //  从AddNewContact.java接受回来的数据
            case (ADD_CONTACT_RESULT_CODE): {
                super.onActivityResult(requestCode, resultCode, Intent_data);
                // ContactsFragment.instance.refreshData();
                refresh();
                break;
            }
        }
        //textView.setText("另外一个Activity传回来的数据是："+data.getStringExtra("data"));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_contactsinfo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_blacklist:
                return true;
            case R.id.action_addtag:
                return true;
            case R.id.action_SMS:
                Intent intent = new Intent(ContactsinfoActivity.this, ComfortMsgActivity.class);
                intent.putExtra("phone", curr_contact.getNumber());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    //    private void applyPalette(Palette palette) {
//        int primaryDark = getResources().getColor(R.color.primary_dark);
//        int primary = getResources().getColor(R.color.primary);
//        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
//        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
//        updateBackground((FloatingActionButton) findViewById(R.id.fab), palette);
//        supportStartPostponedEnterTransition();
//    }
}

