package com.example.jinsihou.ConvenienceDic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.jinsihou.ConvenienceDic.modules.SearchHistory;
import com.example.jinsihou.ConvenienceDic.utils.ConstUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by jinsihou on 2016/12/3.
 */

public class SearchHistoryDBManager {
    private final String TABLE = "history";
    private SearchHistoryHelper searchHistoryHelper;
    private SQLiteDatabase database;

    public SearchHistoryDBManager(Context context) {
        searchHistoryHelper = new SearchHistoryHelper(context, ConstUtils.DB_NAME, 1);
        database = searchHistoryHelper.getReadableDatabase();
    }

    public long addHistory(SearchHistory searchHistory) {
        Cursor cursor = database.rawQuery("select * from history where keyword=?", new String[]{searchHistory.getKeyword()});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
//            database.rawQuery("update history set searchTime=? where _id=?", new String[]{Long.toString(new Date().getTime()), Integer.toString(id)});
            ContentValues contentValues = new ContentValues();
            contentValues.put("searchTime", Long.toString(new Date().getTime()));
            int update = database.update(TABLE, contentValues, "_id=?", new String[]{Integer.toString(id)});
            cursor.close();
            return update;
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put("keyword", searchHistory.getKeyword());
            contentValues.put("summary", searchHistory.getSummary());
            contentValues.put("fullResult", searchHistory.getFullResult());
            contentValues.put("searchTime", searchHistory.getSearchTime());
            return database.insert(TABLE, null, contentValues);
        }
    }

    public ArrayList<Map<String, String>> getHistory() {
        Cursor cursor = database.rawQuery("select * from history ORDER BY searchTime DESC", new String[]{});
        ArrayList<Map<String, String>> arrayList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String keyword = cursor.getString(cursor.getColumnIndex("keyword"));
            String summary = cursor.getString(cursor.getColumnIndex("summary"));
            arrayList.add(new SearchHistory(keyword, summary).toMap());
        }
        cursor.close();
        return arrayList;
    }

    public Cursor getHistoryCursor() {
        return database.rawQuery("select * from history ORDER BY searchTime DESC", new String[]{});
    }

    public int clearAllHistory() {
        return database.delete(TABLE, "_id>?", new String[]{"0"});
    }

    public void close() {
        searchHistoryHelper.close();
    }
}
