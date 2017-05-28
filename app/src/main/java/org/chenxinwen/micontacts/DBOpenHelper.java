package org.chenxinwen.micontacts;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



/**
 * Created by Administrator on 2017/4/25.
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    public DBOpenHelper(Context context) {
        super(context, "lrz.db", null, 1);
    }

    //数据库第一次创建时候调用，
    public void onCreate(SQLiteDatabase db) {
        //db=SQLiteDatabase.openOrCreateDatabase("/data/databases/lrz.db",null);
        try {
            //db.execSQL("DROP TABLE uesertable");
            db.execSQL("create table usertable(id integer primary key autoincrement,name varchar(50),url varchar(50),number varchar(50),email varchar(50),tag varchar(50),blackList integer)");
        }
        catch (Exception e)
        {
            Log.d("新建数据库表格：" , e.getMessage());
        }

    }


    //数据库文件版本号发生变化时调用
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

    }
}
