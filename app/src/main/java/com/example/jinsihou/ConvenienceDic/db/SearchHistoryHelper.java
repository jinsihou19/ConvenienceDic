package com.example.jinsihou.ConvenienceDic.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jinsihou on 2016/12/3.
 */

public class SearchHistoryHelper extends SQLiteOpenHelper {

    public SearchHistoryHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE history(_id INTEGER PRIMARY KEY AUTOINCREMENT, keyword TEXT, summary TEXT, fullResult TEXT, searchTime TIME)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("version", "version" + oldVersion + newVersion);
    }
}
