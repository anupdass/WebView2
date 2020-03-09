package com.example.webview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    private RelativeLayout relativeLayout;
    private Button btn;
    ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private WebView mywebView;
    private final String URL = "http://ums.seu.edu.bd/index.xhtml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //status bar remove code

//        Window window = getWindow();
//        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        mywebView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading please wait...");

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(URL);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        btn = findViewById(R.id.retry);
        relativeLayout = findViewById(R.id.relativeLayout);

        checakConnection();

        // Download File..........
        mywebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, long contentLength) {
                Dexter.withActivity(MainActivity.this)
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {

                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                request.setMimeType(mimetype);
                                String cookes = CookieManager.getInstance().getCookie(userAgent);
                                request.addRequestHeader("cookies",cookes);
                                request.addRequestHeader("cookies",userAgent);
                                request.setDescription("Downloding file....");
                                request.setTitle(URLUtil.guessFileName(userAgent,contentDisposition,mimetype));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,URLUtil.guessFileName(
                                        userAgent,contentDisposition,mimetype
                                ));
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });



        mywebView.getSettings().setJavaScriptEnabled(true);
        mywebView.setWebViewClient(new WebViewClient());

        mywebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

               // progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                //setTitle("Loding......");
                progressDialog.show();

                if (newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                   setTitle(view.getTitle());
                    progressDialog.dismiss();
                }

                super.onProgressChanged(view, newProgress);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(mywebView.canGoBack()){
            mywebView.goBack();
        }
        else
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            finish();
                        }
                    }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menufile,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.back:
                onBackPressed();
                break;
            case R.id.forward:
                if(mywebView.canGoForward())
                    mywebView.goForward();
                break;
            case R.id.reload:
                checakConnection();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checakConnection() {

        ConnectivityManager connectivityManager =(ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(connectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(connectivityManager.TYPE_MOBILE);

        if(wifi.isConnected()){
            mywebView.loadUrl(URL);
            mywebView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
        }
        else if(mobileNetwork.isConnected()){
            mywebView.loadUrl(URL);
            mywebView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);
        }
        else
        {
            mywebView.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
        }

    }

    public void retry(View view) {
        checakConnection();
    }
}
