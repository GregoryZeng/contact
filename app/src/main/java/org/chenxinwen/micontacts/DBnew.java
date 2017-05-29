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
            db.execSQL("insert into usertable(id,name,url,number,email,tag,blackList) values(?,?,?,?,?,?,?)", new Object[]{data.getId(),data.getName(),data.getUrl(),data.getNumber(),data.getEmail(),data.getTag(),data.getBlackList()});
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
            db.execSQL("update usertable set name=?,url=?,number = ?,email=?,tag=?,blackList=? where id=?",
                    new Object[]{data.getName(), data.getUrl(), data.getNumber(),data.getEmail(),data.getTag(),
                            data.getBlackList(),data.getId()});
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
        if(cursor.moveToFirst()){
            int uid2=cursor.getInt(cursor.getColumnIndex("id"));
            String uname=cursor.getString(cursor.getColumnIndex("name"));
            String url=cursor.getString(cursor.getColumnIndex("url"));
            String number =cursor.getString(cursor.getColumnIndex("number"));
            String email = cursor.getString(cursor.getColumnIndex("email"));
            String tag = cursor.getString(cursor.getColumnIndex("tag"));
            int blackList = cursor.getInt(cursor.getColumnIndex("blackList"));
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
    /**
     * 分页查找数据
     * @param offset 跳过多少条数据
     * @param maxResult 每页多少条数据
     * @return
     */
    public ArrayList<Contacts> getScrollData(int offset, int maxResult){
        ArrayList<Contacts> datas=new ArrayList<Contacts>();
        SQLiteDatabase db=dbOpenHelper.getReadableDatabase();
        Cursor cursor =db.rawQuery("select * from usertable order by id asc limit ?,?", new String[]{String.valueOf(offset), String.valueOf(maxResult)});
        while(cursor.moveToNext()){
            int uid2=cursor.getInt(cursor.getColumnIndex("id"));
            String uname=cursor.getString(cursor.getColumnIndex("name"));
            String url=cursor.getString(cursor.getColumnIndex("url"));
            String number =cursor.getString(cursor.getColumnIndex("number"));
            Contacts data=new Contacts();
            data.setNumber(number);
            data.setId(uid2);
            data.setName(uname);
            data.setUrl(url);
            data.setPinyin(HanziToPinyin.getPinYin(data.getName()));
            datas.add(data);
        }
        return datas;
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
                String tag = cursor.getString(cursor.getColumnIndex("tag"));
                int blackList = cursor.getInt(cursor.getColumnIndex("blackList"));
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
            String tag = cursor.getString(cursor.getColumnIndex("tag"));
            int blackList = cursor.getInt(cursor.getColumnIndex("blackList"));
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
