package org.chenxinwen.micontacts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.chenxinwen.micontacts.bean.Contacts;
import org.kymjs.kjframe.utils.KJLoger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/3.
 */

public class AddNewContact extends Activity {
    private static final String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() :"/mnt/sdcard";//保存到SD卡
    private static final String SAVE_REAL_PATH = SAVE_PIC_PATH + "/ContactImg/temp/";//保存的确切位置
    private HashMap<Integer,Boolean> ID_map;
    private  EditText name;
    private  EditText phone;
    private EditText email;
    private Button bt_add;
    private Button bt_cancel;
    private ImageView contactImg;
    private Button select;
    private final String IMAGE_TYPE = "image/*";
    private final int IMAGE_CODE = 0;
    private static final int ADD_CONTACT_RESULT_CODE = 1;
    private static final int RESULT_REQUEST_CODE = 2;
    private static final int RESULT_TAG_CODE = 3;
    private String url;
    private String username;
    private String newName;
    private String newNum;
    private String newUrl;
    private String newEmail;
    private DBnew db = new DBnew(this);
    // tag

    private FlowLayout flowLayout;//上面的flowLayout
    private TagFlowLayout allFlowLayout;//所有标签的TagFlowLayout
    private ArrayList<String> label_list = new ArrayList<>();//上面的标签列表
    private List<String> all_label_List = new ArrayList<>();//所有标签列表
    final List<TextView> labels = new ArrayList<>();//存放标签
    final List<Boolean> labelStates = new ArrayList<>();//存放标签状态
    final Set<Integer> set = new HashSet<>();//存放选中的
    private TagAdapter<String> tagAdapter;//标签适配器
    private LinearLayout.LayoutParams params;
    private EditText editText;
    private ArrayList<String> newtag =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);
        name = (EditText) findViewById(R.id.name);
        phone =(EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
        contactImg = (ImageView)findViewById(R.id.contactImg);
        bt_add = (Button) findViewById(R.id.add);
        bt_cancel = (Button) findViewById(R.id.cancel);
        bt_add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                username = name.getText().toString().trim();
                newNum = phone.getText().toString().trim();
                newEmail = email.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    name.setError(getString(R.string.error_field_required));
                    name.requestFocus();
                    return;
                }
                addToDB();
                Intent intent =new Intent();
                // i.putExtra("data",editText.getText().toString());
                intent.putExtra("newName",username);
                intent.putExtra("newUrl",url);
                setResult(ADD_CONTACT_RESULT_CODE,intent);
                onDestory();
                finish();
            }

        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }

        });
        contactImg.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
                getAlbum.setType(IMAGE_TYPE);
                startActivityForResult(getAlbum, IMAGE_CODE);
            }
        });

        initView();
        initData();
        initEdittext();

    }
    public void onDestory(){
        flowLayout = null;//上面的flowLayout
        allFlowLayout = null;//所有标签的TagFlowLayout
        label_list  = null;//上面的标签列表
        all_label_List = null;
        tagAdapter = null;
        editText = null;
        db = null;
        //super.onDestory();
    }



    private void addToDB()
    {
        ArrayList<Contacts> datas = db.getAllData();
        HashMap<Integer, Boolean> ID_map = new HashMap<Integer, Boolean>();
        for (Contacts data : datas) {
            ID_map.put(data.getId(), true);
            Log.d("data:", data.getName());
        }
        Random random = new Random();
        int tmpID = random.nextInt(100000)+10;
        while (ID_map.containsKey(tmpID)) {
            tmpID = random.nextInt(100000)+10;
        }
        //  根据选择照片uri生成并保存该app专用的图片
        try {
            //Log.d("onAyResult url:", url);
            KJLoger.debug("newUrl error"+url);;
            newUrl = url;
            //Log.d("onAyResult newUrl:", newUrl);
            newName = username;
            Log.d("onAyResult newUrl:", username);
            String subForder = SAVE_REAL_PATH;
            String fileName = String.valueOf(tmpID);
            File foder = new File(subForder);
            if (!foder.exists()) {
                foder.mkdirs();
            }
            File myCaptureFile = new File(subForder, fileName);
            if (!myCaptureFile.exists()) {
                try {
                    myCaptureFile.createNewFile();
                } catch (IOException e) {
                    Log.d("IOerror", e.getMessage());
                }
            }
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            } catch (FileNotFoundException e) {
                Log.d("fileNOTFOUND", e.getMessage());
            }
            Bitmap bm = BitmapFactory.decodeFile(newUrl);
            bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            try {
                bos.flush();
            } catch (IOException e) {
                Log.d("bos IOerror", e.getMessage());
            }
            try {
                bos.close();
            } catch (IOException e) {
                Log.d("bos IOerror", e.getMessage());
            }

            newUrl = SAVE_REAL_PATH + fileName;
        }
        catch (Exception e){
            KJLoger.debug("newUrl error"+e.getMessage());;
            newUrl = "noImg";
        }
        Contacts data = new Contacts();
        data.setName(newName);
        data.setUrl(newUrl);
        data.setNumber(newNum);
        data.setId(tmpID);
        data.setEmail(newEmail);
        data.setPinyin(HanziToPinyin.getPinYin(data.getName()));
        data.setTag(label_list);
        db.insert(data);
        // refreshData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d("system.out getresult1",String.valueOf(resultCode));
        if(resultCode==RESULT_TAG_CODE)
        {
            flowLayout.removeAllViews();
            initView();
            super.onActivityResult(requestCode, resultCode, data);
            labels.clear();
            labelStates.clear();
            label_list= data.getStringArrayListExtra("label_list");
            for (int i = 0; i < label_list.size() ; i++) {
                editText = new EditText(getApplicationContext());//new 一个EditText
                editText.setText(label_list.get(i));
                addLabel(editText);//添加标签
            }
            initEdittext();
            return;
        }
        if (resultCode != RESULT_OK) { //此处的 RESULT_OK 是系统自定义得一个常量
// Log.e(TAG,"ActivityResult resultCode error");
            return;
        }
        Bitmap bm = null;
        ContentResolver resolver = getContentResolver();
        if (requestCode == IMAGE_CODE) {
            try {
                Uri originalUri = data.getData(); //获得图片的uri
              /*
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri); //显得到bitmap图片
// 这里开始的第二部分，获取图片的路径：
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);
//按我个人理解 这个是获得用户选择的图片的索引值
                //自添加

                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();

//最后根据索引值获取图片路径
                url = cursor.getString(column_index);
                contactImg.setImageBitmap(bm);
                Log.e("Lostinai",url);
                */
                startPhotoZoom(originalUri);

            }catch (Exception e) {

                Log.e("Lostinai",e.toString());

            }
        }
        if(requestCode == RESULT_REQUEST_CODE)
        {
            try {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    bm = extras.getParcelable("data");
                    Drawable drawable = new BitmapDrawable(this.getResources(), bm);
                    contactImg.setImageDrawable(drawable);

                    //保存图片到本地
                    String subForder = SAVE_REAL_PATH;
                    String fileName = "tempImg";
                    File foder = new File(subForder);
                    if (!foder.exists()) {
                        foder.mkdirs();
                    }
                    File myCaptureFile = new File(subForder, fileName);
                    if (!myCaptureFile.exists()) {

                        myCaptureFile.createNewFile();

                    }
                    BufferedOutputStream bos = null;

                    bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));


                    bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);

                    bos.flush();


                    bos.close();


                    url = SAVE_REAL_PATH + fileName;
                }
            }catch (Exception e) {

                Log.e("Lostinai",e.toString());

            }
        }

    }
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 340);
        intent.putExtra("outputY", 340);
        intent.putExtra("return-data", true);
        startActivityForResult(intent,RESULT_REQUEST_CODE);
    }

    //tag

    private void initView() {
        flowLayout = (FlowLayout) findViewById(R.id.id_flowlayout);
        // allFlowLayout = (TagFlowLayout) findViewById(R.id.id_flowlayout_two);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 20, 20, 20);
        flowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddNewContact.this, AddTag.class);
                intent.putStringArrayListExtra("label_list", label_list);
                startActivityForResult(intent,RESULT_TAG_CODE);
            }
        });
    }
    /**
     * 初始化数据
     */
    private void initData(){
        //初始化上面标签
        label_list.clear();

        for(String temp:newtag)
        {
            label_list.add(temp);
        }


        for (int i = 0; i < label_list.size() ; i++) {
            editText = new EditText(getApplicationContext());//new 一个EditText
            editText.setText(label_list.get(i));
            addLabel(editText);//添加标签
        }

    }


    /**
     * 初始化所有标签列表
     */
    private void initEdittext(){
        editText = new EditText(getApplicationContext());
        editText.setHint("添加标签");
        //设置固定宽度
        editText.setMinEms(4);
        editText.setTextSize(12);
        //设置shape
        editText.setBackgroundResource(R.drawable.label_add);
        editText.setHintTextColor(Color.parseColor("#b4b4b4"));
        editText.setTextColor(Color.parseColor("#000000"));
        editText.setLayoutParams(params);
        editText.setFocusable(false);
        //editText.setEnabled(false);
        editText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(AddNewContact.this, AddTag.class);
                intent.putStringArrayListExtra("label_list", label_list);
                startActivityForResult(intent,RESULT_TAG_CODE);
            }

        });
        ;
        //添加到layout中
        flowLayout.addView(editText);
    }

    /**
     * 添加标签
     * @param editText
     * @return
     */
    private boolean addLabel(EditText editText) {
        String editTextContent = editText.getText().toString();
        //判断输入是否为空
        if (editTextContent.equals(""))
            return true;
        //判断是否重复
        for (TextView tag : labels) {
            String tempStr = tag.getText().toString();
            if (tempStr.equals(editTextContent)) {
                editText.setText("");
                editText.requestFocus();
                return true;
            }
        }
        //添加标签
        final TextView temp = getTag(editText.getText().toString());
        labels.add(temp);
        labelStates.add(false);
        flowLayout.addView(temp);
        //让输入框在最后一个位置上
        editText.bringToFront();
        //清空编辑框
        editText.setText("");
        editText.requestFocus();
        return true;

    }
    private TextView getTag(String label) {
        TextView textView = new TextView(getApplicationContext());
        textView.setTextSize(12);
        textView.setBackgroundResource(R.drawable.label_normal);
        textView.setTextColor(Color.parseColor("#00aa00"));
        textView.setText(label);
        textView.setLayoutParams(params);
        return textView;
    }

}

