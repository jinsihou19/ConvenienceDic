package com.example.jinsihou.ConvenienceDic.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jinsihou.ConvenienceDic.db.SearchHistoryDBManager;
import com.example.jinsihou.ConvenienceDic.modules.SearchHistory;
import com.example.jinsihou.ConvenienceDic.utils.ConstUtils;
import com.example.jinsihou.ConvenienceDic.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MainActivity extends ActionBarActivity {
    private TextView mWordText = null;
    private TextView mPhonetic = null;
    private TextView mBasicText = null;
    private TextView mWebText = null;
    private TextView mTranslate = null;
    private ListView historyView = null;
    private EditText mEditText = null;
    private Button mClearHistory = null;
    private android.support.v7.widget.CardView cardView = null;
    SearchHistoryDBManager searchHistoryDBManager;
    private String searchWord = "";
    private String searchResult = "";
    private LinearLayout mHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        showHistory();
    }

    private void initViews() {
        mWordText = (TextView) findViewById(R.id.word);
        mPhonetic = (TextView) findViewById(R.id.phonetic);
        mBasicText = (TextView) findViewById(R.id.basic);
        mWebText = (TextView) findViewById(R.id.web);
        mTranslate = (TextView) findViewById(R.id.translate);
        cardView = (android.support.v7.widget.CardView) findViewById(R.id.card);
        historyView = (ListView) findViewById(R.id.history);
        mEditText = (EditText) findViewById(R.id.et);
        mHistoryList = (LinearLayout) findViewById(R.id.historyList);
        Button btn = (Button) findViewById(R.id.btn);
        mClearHistory = (Button) findViewById(R.id.clearHistory);
        assert btn != null;
        searchHistoryDBManager = new SearchHistoryDBManager(MainActivity.this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editText = mEditText.getText().toString();
                searchOnLine(editText);
            }
        });
        mEditText.requestFocus(); // 已经去除自动获取焦点，所以首次进去获取一下焦点
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchOnLine(v.getText().toString());
                }
                return false;
            }
        });
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (((EditText) v).getText().toString().equals("")) {
                    clearCard();
                    mHistoryList.setVisibility(View.VISIBLE);
                    showHistory();
                }
                return false;
            }
        });
        mClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchHistoryDBManager.clearAllHistory();
                mClearHistory.setVisibility(View.GONE);
                showHistory();
            }
        });
    }

    private void showHistory() {
        final ArrayList<Map<String, String>> searchHistories = searchHistoryDBManager.getHistory();
        if (searchHistories.size() > 0) {
            mClearHistory.setVisibility(View.VISIBLE);
        }
        historyView.setAdapter(new SimpleAdapter(MainActivity.this, searchHistories, R.layout.history_list_item, new String[]{"keyword", "summary"}, new int[]{R.id.history_keyword, R.id.history_basic_explain}));
        historyView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String keyword = searchHistories.get(position).get("keyword");
                mEditText.setText(keyword);
                searchOnLine(keyword);
                mHistoryList.setVisibility(View.GONE);
            }
        });
    }

    private void hideIME() {
        mEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private void searchOnLine(String keyword) {
        clearCard();
        String urlStr;
        hideIME();
        try {
            if (!keyword.equals("")) {
                searchWord = keyword;
                urlStr = URLEncoder.encode(keyword, "utf-8");
                readUrl(ConstUtils.YOUDAO_URL + urlStr);
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("UnsupportedEncoding", e.getMessage());
        }
    }

    void readUrl(String urlStr) {
        new AsyncTask<String, Void, String>() {

            @Override
            protected void onPostExecute(String s) {
                try {
                    if (s != null) {
                        JSONObject f = new JSONObject(s);
                        int resultCode = f.getInt("errorCode");
                        if (0 == resultCode) { // 词典正常时，方可出现查询结果卡片
                            String summary = "";
                            mWordText.setText(f.getString("query")); // 单词
                            if (!f.isNull("basic")) { // 判断存在基本解释
                                JSONObject jo = f.getJSONObject("basic");
                                if (!jo.isNull("uk-phonetic")) {
                                    mPhonetic.setText("UK [" + jo.getString("uk-phonetic") + "] / ");//发音
                                    mPhonetic.append("US [" + jo.getString("us-phonetic") + "]");
                                }
                                JSONArray ja = jo.getJSONArray("explains");//基本解释
                                for (int i = 0; i < ja.length(); i++) {
                                    if (i == 0) {
                                        summary = ja.getString(i);
                                    }
                                    mBasicText.append(ja.getString(i) + "\n");
                                }
                            } else mBasicText.setText(getString(R.string.warmingWord1));
                            if (!f.isNull("web")) { // 判断存在网络解释
                                JSONArray wja = f.getJSONArray("web"); // 网络解释
                                for (int i = 0; i < wja.length(); i++) {
                                    JSONObject wjo = wja.getJSONObject(i);
                                    mWebText.append(wjo.getString("key") + "\n");
                                    JSONArray wja1 = wjo.getJSONArray("value");
                                    for (int j = 0; j < wja1.length(); j++) {
                                        mWebText.append(wja1.getString(j) + ";");
                                    }
                                    mWebText.append("\n");
                                }
                            } else mWebText.setText(getString(R.string.warmingWord2));
                            if (!f.isNull("translation")) {
                                JSONArray trans = f.getJSONArray("translation");
                                for (int i = 0; i < trans.length(); i++) {
                                    mTranslate.append(trans.getString(i) + ";");
                                }
                            }
                            storeHook(summary, s);
                            cardView.setVisibility(View.VISIBLE); // 显示卡片
                            mHistoryList.setVisibility(View.GONE);
                        } else {
                            String errorName = getString(R.string.errorHit) + resultCode;
                            Toast.makeText(MainActivity.this, errorName, Toast.LENGTH_SHORT).show();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onPostExecute(s);
            }

            @Override
            protected String doInBackground(String... strings) {
                URL url;
                try {
                    url = new URL(strings[0]);
                    URLConnection uc = url.openConnection();
                    InputStream is = uc.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    is.close();
                    isr.close();
                    br.close();
                    return sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(urlStr);
    }

    private void storeHook(String summary, String fullResult) {
        searchResult = fullResult;
        SearchHistory searchHistory = new SearchHistory(searchWord, new Date().getTime());
        searchHistory.setSummary(summary);
        searchHistory.setFullResult(fullResult);
        searchHistoryDBManager.addHistory(searchHistory);
    }

    private void clearCard() {
        cardView.setVisibility(View.GONE);
        searchResult = "";
        searchWord = "";
        mBasicText.setText("");
        mWordText.setText("");
        mWebText.setText("");
        mPhonetic.setText("");
        mTranslate.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.menu_update) {
            return true;
        }
        if (id == R.id.menu_about) {
            AlertDialog.Builder normalDia = new AlertDialog.Builder(this);
            normalDia.setIcon(R.drawable.bookshelf);
            normalDia.setTitle(getString(R.string.aboutTitle));
            normalDia.setMessage(getString(R.string.aboutMassage));

            normalDia.setPositiveButton(getString(R.string.aboutOK), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            normalDia.create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void popMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.menu_popup, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.pop_settings:
                        return true;
                    case R.id.pop_update:
                        return true;
                    case R.id.pop_about:
                        AlertDialog.Builder normalDia = new AlertDialog.Builder(MainActivity.this);
                        normalDia.setIcon(R.drawable.bookshelf);
                        normalDia.setTitle(getString(R.string.aboutTitle));
                        normalDia.setMessage(getString(R.string.aboutMassage));

                        normalDia.setPositiveButton(getString(R.string.aboutOK), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        normalDia.create().show();
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchHistoryDBManager != null) {
            searchHistoryDBManager.close();
        }
    }
}
