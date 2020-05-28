package com.ahm.codeprorssreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class FeedDatabaseHandler extends SQLiteOpenHelper {

    public FeedDatabaseHandler(Context c, String name,
                               SQLiteDatabase.CursorFactory factory,
                               int v){
        super(c, "FeedDatabase.db", factory, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table urls_table(id int primary key, " +
                "url text not null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists urls_table;");
        onCreate(sqLiteDatabase);
    }

    public void addItem(String url){
        ContentValues values = new ContentValues();
        values.put("url", url);
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert("urls_table", null, values);
        db.close();
    }

    public void deleteItem(String item){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from urls_table where " +
                "url = \"" + item + "\";");
        db.close();
    }

    public ArrayList<String> loadUrls(){
        ArrayList<String> urls = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from urls_table;" , null);
        if(c.moveToFirst()){
            while(!c.isAfterLast()){
                String url = c.getString(1);
                urls.add(url);
                c.moveToNext();
            }
        }
        db.close();
        return urls;
    }
}
