package com.example.jinsihou.ConvenienceDic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.update.UmengUpdateAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class MainActivity extends ActionBarActivity {
    private Button btn = null;
    private TextView mWordText = null;
    private TextView mPhonetic = null;
    private TextView mBasicText = null;
    private TextView mWebText = null;
    private TextView mTransate = null;
    private EditText et = null;
    private android.support.v7.widget.CardView cardView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UmengUpdateAgent.update(this);
        btn = (Button) findViewById(R.id.btn);
        mWordText = (TextView) findViewById(R.id.word);
        mPhonetic = (TextView) findViewById(R.id.phonetic);
        mBasicText = (TextView) findViewById(R.id.basic);
        mWebText = (TextView) findViewById(R.id.web);
        mTransate = (TextView) findViewById(R.id.translate);
        et = (EditText) findViewById(R.id.et);//
        cardView = (android.support.v7.widget.CardView) findViewById(R.id.card);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = "http://fanyi.youdao.com/openapi.do?keyfrom=fdfafds&key=1003799844&type=data&doctype=json&version=1.1&q=";
                cardView.setVisibility(View.GONE);
                mBasicText.setText("");
                mWordText.setText("");
                mWebText.setText("");
                mPhonetic.setText("");
                mTransate.setText("");
                String urlStr = null;
                try {
                    urlStr = URLEncoder.encode(et.getText().toString(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                readUrl(str + urlStr);
                et.setText("");
            }
        });
    }

    void readUrl(String urlstr) {
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
                                mTransate.append(trans.getString(i) + ";");
                            }
                        }
                        cardView.setVisibility(View.VISIBLE);//显示卡片
                        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);// 显示或者隐藏输入法
                    } else {
                        String errorName = getString(R.string.errorHit) + resultCode;
                        Toast.makeText(MainActivity.this, errorName, Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onPostExecute(s);

            }

            @Override
            protected String doInBackground(String... strings) {
                URL url = null;
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
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(urlstr);
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
            UmengUpdateAgent.forceUpdate(this);
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
                        UmengUpdateAgent.forceUpdate(MainActivity.this);
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

}
