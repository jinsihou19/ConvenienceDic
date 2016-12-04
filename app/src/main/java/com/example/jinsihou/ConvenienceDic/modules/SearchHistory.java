package com.example.jinsihou.ConvenienceDic.modules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jinsihou on 2016/12/3.
 */

public class SearchHistory {
    private String keyword;
    private String summary;
    private String fullResult;
    private long searchTime;

    public SearchHistory(){

    }

    public SearchHistory(String keyword, long searchTime) {
        this.keyword = keyword;
        this.searchTime = searchTime;
    }

    public SearchHistory(String keyword, String summary) {
        this.keyword = keyword;
        this.summary = summary;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(long searchTime) {
        this.searchTime = searchTime;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFullResult() {
        return fullResult;
    }

    public void setFullResult(String fullResult) {
        this.fullResult = fullResult;
    }

    public Map<String, String> toMap() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("keyword", keyword);
        hashMap.put("searchTime", SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT).format(new Date(searchTime)));
        hashMap.put("summary", summary);
        hashMap.put("fullResult", fullResult);
        return hashMap;
    }
}
