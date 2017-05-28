package org.chenxinwen.micontacts;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.chenxinwen.micontacts.bean.Contacts;
import org.kymjs.kjframe.utils.KJLoger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class EditContactsinfoActivity extends AppCompatActivity {
    private static final String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() :"/mnt/sdcard";//保存到SD卡
    private static final String SAVE_REAL_PATH = SAVE_PIC_PATH + "/ContactImg/temp/";//保存的确切位置
    private HashMap<Integer,Boolean> ID_map;
    private EditText name;
    private EditText phone_et;
    private EditText email_et;
    private Button bt_add;
    private Button bt_cancel;
    private ImageView contactImg;
    private Button select;
    private final String IMAGE_TYPE = "image/*";
    private final int IMAGE_CODE = 0;
    private static final int ADD_CONTACT_RESULT_CODE = 1;
    private static final int RESULT_REQUEST_CODE = 2;
    private String url;
    private String username;
    private String phone;
    private String newName;
    private String newUrl;
    private String email;
    private DBnew db = new DBnew(this);
    private int targetID=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contactsinfo);
        Intent intent=getIntent();
        targetID  = intent.getIntExtra("id",targetID);

        name = (EditText) findViewById(R.id.name);
        phone_et=(EditText)findViewById(R.id.phone);
        email_et=(EditText)findViewById(R.id.email);
        contactImg = (ImageView)findViewById(R.id.contactImg);
        bt_add = (Button) findViewById(R.id.add);
        bt_cancel = (Button) findViewById(R.id.cancel);
        bt_add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                username = name.getText().toString().trim();
                phone = phone_et.getText().toString().trim();
                email = email_et.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    name.setError(getString(R.string.error_field_required));
                    name.requestFocus();
                    return;
                }
                updataToDB();
                Intent intent =new Intent();
                // i.putExtra("data",editText.getText().toString());
                //      intent.putExtra("newName",username);
                //      intent.putExtra("newUrl",url);
                setResult(ADD_CONTACT_RESULT_CODE,intent);
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
    }
    private void updataToDB()
    {
        ArrayList<Contacts> datas = db.getAllData();
        HashMap<Integer, Boolean> ID_map = new HashMap<Integer, Boolean>();
        for (Contacts data : datas) {
            ID_map.put(data.getId(), true);
            Log.d("data:", data.getName());
        }
        Random random = new Random();
        int tmpID = random.nextInt(10000);
        while (ID_map.containsKey(tmpID)) {
            tmpID = random.nextInt(10000);
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
            //Log.d("newUrl error",e.getMessage());
            newUrl = "noImg";
        }
        Contacts data = db.find(targetID);
        data.setName(newName);
        data.setNumber(phone);
        data.setUrl(newUrl);
        data.setId(targetID);
        data.setEmail(email);
        data.setPinyin(HanziToPinyin.getPinYin(data.getName()));



        db.update(data);
        // refreshData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
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
}

