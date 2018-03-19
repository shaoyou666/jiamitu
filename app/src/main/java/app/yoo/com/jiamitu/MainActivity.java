package app.yoo.com.jiamitu;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button bt_start;
    private Switch aSwitch;
    private WebView web;
    //private TextView textView;
    private boolean isStart = false,isFaster = false;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_start = (Button) findViewById(R.id.bt_start);
        web = (WebView) findViewById(R.id.webview);
        //textView = (TextView) findViewById(R.id.textView);
        aSwitch = (Switch) findViewById(R.id.aSwitch);

        isFaster = aSwitch.isChecked();

        configWebView(web, false);
        String url = "https://jiamitu.mi.com/home";
        synCookies(this, url, "uid=11");
        synCookies(this, url, "name=xyw");
        synCookies(this, url, "agent=android");
        synCookies(this, url, "age=20;sex=1;time=today");
        web.loadUrl(url );

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isFaster = b;
            }
        });

        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //textView.setText("正在领免中..");
                isStart = !isStart;
                if(isStart){
                    count = 0;
                    bt_start.setText("正在领免中...点击暂停");
                    if(isFaster){
                        bt_start.setText("正在领免中...加速进行中...");
                    }
                    new StartTask().execute((Void) null);
                }else {
                    bt_start.setText("开始领免");
                }
            }
        });
    }

    public class StartTask extends AsyncTask<Void,Void,String>{
        String url = "https://jiamitu.mi.com/pet/rush/pet?followUp=https:%2F%2Fjiamitu.mi.com%2Fhome%3FuserId%3D12948538";
        OkHttpClient okHttpClient;

        Request request;
        public StartTask() {
            CookieManager cookieManager = CookieManager.getInstance();
            String cookieStr = cookieManager.getCookie("https://jiamitu.mi.com/home");
            okHttpClient = new OkHttpClient();
            request = new Request.Builder().url(url).header("Cookie",cookieStr).build();

        }

        @Override
        protected void onPostExecute(String s) {
            if(!isFaster) {
                if (isStart) {
                    web.loadData(s,"text/html; charset=UTF-8", null);
                    //textView.setText(s);
                    bt_start.setText("正在领免中...点击暂停" + count);
                    new StartTask().execute((Void) null);
                } else {
                    //textView.setText("已停止领免");
                    bt_start.setText("开始领免" + count);
                    web.loadUrl("https://jiamitu.mi.com/home" );
                }
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            if(isStart){
                count++;
                Call call = okHttpClient.newCall(request);
                try{
                    Response response = call.execute();
                    if(isFaster&&isStart){
                        call.execute();
                    }
                    result = response.body().string();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            return result;
        }
    }
    /**
     * 设置Cookie
     *
     * @param context
     * @param url
     * @param cookie  格式：uid=21233 如需设置多个，需要多次调用
     */
    public void synCookies(Context context, String url, String cookie) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(url, cookie);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();
    }

    /**
     * 清除Cookie
     *
     * @param context
     */
    public static void removeCookie(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        CookieSyncManager.getInstance().sync();
    }


    protected void configWebView(WebView webView, boolean needCache) {

        if (!needCache) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        //启用数据库
        webView.getSettings().setDatabaseEnabled(true);
        //设置定位的数据库路径
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        webView.getSettings().setGeolocationDatabasePath(dir);
        //启用地理定位
        webView.getSettings().setGeolocationEnabled(true);
        //开启DomStorage缓存
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setWebViewClient(new WebViewClient(){});
        webView.setWebChromeClient(new WebChromeClient());
    }

}
