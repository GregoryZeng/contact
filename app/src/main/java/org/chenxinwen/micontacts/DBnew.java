package org.chenxinwen.micontacts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.chenxinwen.micontacts.bean.Contacts;
import org.chenxinwen.micontacts.DBOpenHelper;
import org.chenxinwen.micontacts.fragment.ContactsFragment;


import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/25.
 */

public class DBnew {
    DBOpenHelper dbOpenHelper;

    public DBnew(Context context){
        this.dbOpenHelper=new DBOpenHelper(context);
    }
    /**
     * 添加一条数据
     * @param data
     */

    public void insert(Contacts data){
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        try{
            db.execSQL("insert into usertable(id,name,url,number,email,blackList) values(?,?,?,?,?,?)", new Object[]{data.getId(),data.getName(),data.getUrl(),data.getNumber(),data.getEmail(),data.getBlackList()});
            for(String newtag:data.getTag())
            {
                try {
                    db.execSQL("insert into tagtable(id,tag) values(?,?)", new Object[]{data.getId(), newtag});
                }
                catch (Exception e)
                {
                    Log.d("插入tag：",e.getMessage());
                }
            }
            db.close();
        }
        catch (Exception e)
        {
            Log.d("插入：",e.getMessage());
        }

    }
    /**
     * 删除所有数据
     */
    public void reset()
    {
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM usertable");
            db.execSQL("DELETE FROM tagtable");
            db.close();
        }
        catch (Exception e)
        {
            Log.d("清空表：" , e.getMessage());
        }
    }
    /**
     * 删除一条数据
     * @param uid
     */
    public void delete(Integer uid){
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        try {
            db.execSQL("delete from usertable where id=?", new Object[]{uid});
            db.execSQL("delete from tagtable where id=?", new Object[]{uid});
            db.close();
        }
        catch (Exception e)
        {
            Log.d("删除：" , e.getMessage());
        }
    }
    /**
     * 更新一条数据
     * @param data
     */
    public void update(Contacts data){
        SQLiteDatabase db=dbOpenHelper.getWritableDatabase();
        try {
            db.execSQL("update usertable set name=?,url=?,number = ?,email=?,blackList=? where id=?",
                    new Object[]{data.getName(), data.getUrl(), data.getNumber(),data.getEmail(),
                            data.getBlackList(),data.getId()});
            db.execSQL("delete from tagtable where id=?", new Object[]{data.getId()});
            for(String newtag:data.getTag())
            {
                db.execSQL("insert into tagtable(id,tag) values(?,?)", new Object[]{data.getId(),newtag});
            }
            db.close();
        }
        catch (Exception e)
        {
            Log.d("更新：" , e.getMessage());
        }
    }
    /**
     * 查找一条数据
     * @param uid
     */
    public Contacts find(Integer uid){
        SQLiteDatabase db=dbOpenHelper.getReadableDatabase();
        Cursor cursor =db.rawQuery("select * from usertable where id=?", new String[]{uid.toString()});
        Cursor tagcursor =db.rawQuery("select * from tagtable where id=?", new String[]{uid.toString()});
        if(cursor.moveToFirst()){
            int uid2=cursor.getInt(cursor.getColumnIndex("id"));
            String uname=cursor.getString(cursor.getColumnIndex("name"));
            String url=cursor.getString(cursor.getColumnIndex("url"));
            String number =cursor.getString(cursor.getColumnIndex("number"));
            String email = cursor.getString(cursor.getColumnIndex("email"));
            int blackList = cursor.getInt(cursor.getColumnIndex("blackList"));
            // 得到tag用arraylist存起来
            ArrayList<String> tag = new ArrayList<>();
            while(tagcursor.moveToNext())
            {
                String temptag = tagcursor.getString(tagcursor.getColumnIndex("tag"));
                tag.add(temptag);
            }
            Contacts data=new Contacts();
            data.setId(uid2);
            data.setName(uname);
            data.setUrl(url);
            data.setNumber(number);
            data.setEmail(email);
            data.setTag(tag);;
            data.setBlackList(blackList);
            return data;
        }
        tagcursor.close();
        cursor.close();
        return null;
    }
    // 获得全部数据
    public ArrayList<Contacts> getAllData() {
        ArrayList<Contacts> datas = new ArrayList<Contacts>();
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from usertable", null);
            while (cursor.moveToNext()) {
                int uid2 = cursor.getInt(cursor.getColumnIndex("id"));
                String uname = cursor.getString(cursor.getColumnIndex("name"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                String number =cursor.getString(cursor.getColumnIndex("number"));
                String email = cursor.getString(cursor.getColumnIndex("email"));
                int blackList = cursor.getInt(cursor.getColumnIndex("blackList"));
                // 得到tag用arraylist存起来
                Cursor tagcursor =db.rawQuery("select * from tagtable where id=?", new String[]{String.valueOf(uid2)});
                ArrayList<String> tag = new ArrayList<>();
                while(tagcursor.moveToNext())
                {
                    String temptag = tagcursor.getString(tagcursor.getColumnIndex("tag"));
                    tag.add(temptag);
                }
                tagcursor.close();
                Contacts data=new Contacts();
                data.setNumber(number);
                data.setId(uid2);
                data.setName(uname);
                data.setUrl(url);
                data.setPinyin(HanziToPinyin.getPinYin(data.getName()));
                data.setEmail(email);
                data.setTag(tag);;
                data.setBlackList(blackList);
                datas.add(data);
            }
        } catch (Exception e) {
            Log.d("获得全部数据：" , e.getMessage());
        }
        return datas;
    }
    public ArrayList<String> getAllTag() {
        ArrayList<String> datas = new ArrayList<String>();
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from tagtable", null);
            while (cursor.moveToNext()) {
                String tag = cursor.getString(cursor.getColumnIndex("tag"));

                datas.add(tag);
            }
        } catch (Exception e) {
            Log.d("获得全部Tag：" , e.getMessage());
        }
        return datas;
    }
    /**
     * 获取数据总数
     * @return
     */
    public long getCount(){
        SQLiteDatabase db=dbOpenHelper.getReadableDatabase();
        Cursor cursor =db.rawQuery("select count(*) from usertable", null);
        cursor.moveToFirst();
        long reslut=cursor.getLong(0);
        return reslut;
    }

    public Contacts findByNum(String phoneNum){
        SQLiteDatabase db=dbOpenHelper.getReadableDatabase();
        Cursor cursor =db.rawQuery("select * from usertable where number=?", new String[]{phoneNum});
        if(cursor.moveToFirst()){
            int uid2=cursor.getInt(cursor.getColumnIndex("id"));
            String uname=cursor.getString(cursor.getColumnIndex("name"));
            String url=cursor.getString(cursor.getColumnIndex("url"));
            String number =cursor.getString(cursor.getColumnIndex("number"));
            String email = cursor.getString(cursor.getColumnIndex("email"));
            int blackList = cursor.getInt(cursor.getColumnIndex("blackList"));
            // 得到tag用arraylist存起来
            Cursor tagcursor =db.rawQuery("select * from tagtable where id=?", new String[]{String.valueOf(uid2)});
            ArrayList<String> tag = new ArrayList<>();
            while(tagcursor.moveToNext())
            {
                String temptag = tagcursor.getString(tagcursor.getColumnIndex("tag"));
                tag.add(temptag);
            }
            tagcursor.close();
            Contacts data=new Contacts();
            data.setId(uid2);
            data.setName(uname);
            data.setUrl(url);
            data.setNumber(number);
            data.setEmail(email);
            data.setTag(tag);;
            data.setBlackList(blackList);
            return data;
        }
        cursor.close();
        return null;
    }
}
