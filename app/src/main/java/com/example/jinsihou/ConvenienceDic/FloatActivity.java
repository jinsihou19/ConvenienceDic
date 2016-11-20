package com.example.jinsihou.ConvenienceDic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

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

public class FloatActivity extends Activity {
    private TextView mWordText = null;
    private TextView mPhonetic = null;
    private TextView mBasicText = null;
    private TextView mWebText = null;
    private TextView mTranslate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float);
        mWordText = (TextView) findViewById(R.id.float_word);
        mPhonetic = (TextView) findViewById(R.id.float_phonetic);
        mBasicText = (TextView) findViewById(R.id.float_basic);
        mWebText = (TextView) findViewById(R.id.float_web);
        mTranslate = (TextView) findViewById(R.id.float_translate);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (text != null) {
                searchOnLine(text.toString());
            }
//            boolean readonly = getIntent().getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);
        }
    }

    private void searchOnLine(String keyword) {
        mBasicText.setText("");
        mWordText.setText("");
        mWebText.setText("");
        mPhonetic.setText("");
        mTranslate.setText("");
        String urlStr = null;
        try {
            urlStr = URLEncoder.encode(keyword, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        readUrl(ConstUtils.YOUDAO_URL + urlStr);
    }

    void readUrl(String urlStr) {
        new AsyncTask<String, Void, String>() {

            @Override
            protected void onPostExecute(String s) {
                try {
                    JSONObject f = new JSONObject(s);
                    int resultCode = f.getInt("errorCode");
                    if (0 == resultCode) {//词典正常时，方可出现查询结果卡片
                        mWordText.setText(f.getString("query"));//单词
                        if (!f.isNull("basic")) {//判断存在基本解释
                            JSONObject jo = f.getJSONObject("basic");
                            if (!jo.isNull("uk-phonetic")) {
                                mPhonetic.setText("UK [" + jo.getString("uk-phonetic") + "] / ");//发音
                                mPhonetic.append("US [" + jo.getString("us-phonetic") + "]");
                            }
                            JSONArray ja = jo.getJSONArray("explains");//基本解释
                            for (int i = 0; i < ja.length(); i++) {
                                mBasicText.append(ja.getString(i) + "\n");
                            }
                        } else mBasicText.setText(getString(R.string.warmingWord1));
                        if (!f.isNull("web")) {//判断存在网络解释
                            JSONArray wja = f.getJSONArray("web");//网络解释
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
                        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);// 显示或者隐藏输入法
                    } else {
                        String errorName = getString(R.string.errorHit) + resultCode;
                        Toast.makeText(FloatActivity.this, errorName, Toast.LENGTH_SHORT).show();
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
}
