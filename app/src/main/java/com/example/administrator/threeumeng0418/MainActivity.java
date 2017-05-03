package com.example.administrator.threeumeng0418;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.HashMap;

import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.twitter.Twitter;

public class MainActivity extends Activity implements View.OnClickListener, PlatformActionListener, Handler.Callback {
    private static final int MSG_USERID_FOUND = 1;
    private static final int MSG_LOGIN = 2;
    private static final int MSG_AUTH_CANCEL = 3;
    private static final int MSG_AUTH_ERROR = 4;
    private static final int MSG_AUTH_COMPLETE = 5;
    private static final String TAG = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化分享的内容
        ShareSDK.initSDK(this);
        setContentView(R.layout.activity_main);
        //初始化控件
        initView();

        Log.i(TAG, "onCreate执行了");
    }

    private void initView() {
        findViewById(R.id.tvWeibo).setOnClickListener(this);
        findViewById(R.id.tvQq).setOnClickListener(this);
        findViewById(R.id.tvOther).setOnClickListener(this);
    }
//销毁分享的功能
    protected void onDestroy() {
        Log.i(TAG, "onDestroy执行了");
        ShareSDK.stopSDK(this);
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvWeibo: {
                authorize(new SinaWeibo(this));
            }
            break;
            case R.id.tvQq: {
                Log.i(TAG, "onClick执行了");
                authorize(new QZone(this));
            }
            break;
            case R.id.tvOther: {
                authorize(null);
            }
            break;
            case R.id.tvFacebook: {
                Dialog dlg = (Dialog) v.getTag();
                dlg.dismiss();
                authorize(new Facebook(this));
            }
            break;
            case R.id.tvTwitter: {
                Dialog dlg = (Dialog) v.getTag();
                dlg.dismiss();
                authorize(new Twitter(this));
            }
            break;
        }
    }

    private void authorize(Platform plat) {
        Log.i(TAG, "authorize执行了");
        if (plat == null) {
            popupOthers();
            return;
        }

        if (plat.isValid()) {
            String userId = plat.getDb().getUserId();
            if (userId != null) {
                UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
                login(plat.getName(), userId, null);

                Log.i(TAG, "id:" + userId);
                Log.i(TAG, "getExpiresIn:" + plat.getDb().getExpiresIn());
                Log.i(TAG, "getExpiresTime:" + plat.getDb().getExpiresTime());
                Log.i(TAG, "getPlatformNname:"
                        + plat.getDb().getPlatformNname());
                Log.i(TAG, "getPlatformVersion:"
                        + plat.getDb().getPlatformVersion());
                Log.i(TAG, "getToken:" + plat.getDb().getToken());
                Log.i(TAG, "getTokenSecret:" + plat.getDb().getTokenSecret());
                Log.i(TAG, "getUserIcon:" + plat.getDb().getUserIcon());
                Log.i(TAG, "getUserId:" + plat.getDb().getUserId());
                Log.i(TAG, "getUserName:" + plat.getDb().getUserName());
                return;
            }
        }
        plat.setPlatformActionListener(this);
        plat.SSOSetting(true);
        plat.showUser(null);
    }

    private void popupOthers() {
        Log.i(TAG, "popupOthers执行了");
        //设置一个Dialog对话框的实力
        Dialog dlg = new Dialog(this);
        //找到所需要的控件
        View dlgView = View.inflate(this, R.layout.other_plat_dialog, null);
        View tvFacebook = dlgView.findViewById(R.id.tvFacebook);
        tvFacebook.setTag(dlg);
        tvFacebook.setOnClickListener(this);
        View tvTwitter = dlgView.findViewById(R.id.tvTwitter);
        tvTwitter.setTag(dlg);
        tvTwitter.setOnClickListener(this);

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(dlgView);
        dlg.show();
    }

    public void onComplete(Platform platform, int action,
                           HashMap<String, Object> res) {
        Log.i(TAG, "onComplete执行了");
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_COMPLETE, this);
            login(platform.getName(), platform.getDb().getUserId(), res);
        }
        Log.i(TAG, res.toString());
    }

    public void onError(Platform platform, int action, Throwable t) {
        Log.i(TAG, "onError执行了");
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
        }
        t.printStackTrace();
    }

    public void onCancel(Platform platform, int action) {
        Log.i(TAG, "onCancel执行了");
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
        }
    }

    private void login(String plat, String userId,
                       HashMap<String, Object> userInfo) {
        Log.i(TAG, "login执行了");
        Message msg = new Message();
        msg.what = MSG_LOGIN;
        msg.obj = plat;
        UIHandler.sendMessage(msg, this);
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_USERID_FOUND: {
                Toast.makeText(this, R.string.userid_found, Toast.LENGTH_SHORT)
                        .show();
            }
            break;
            case MSG_LOGIN: {
                String text = getString(R.string.logining, msg.obj);
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.if_register_needed);
                builder.setMessage(R.string.after_auth);
                builder.setPositiveButton(R.string.ok, null);
                builder.create().show();
            }
            break;
            case MSG_AUTH_CANCEL: {
                Toast.makeText(this, R.string.auth_cancel, Toast.LENGTH_SHORT)
                        .show();
            }
            break;
            case MSG_AUTH_ERROR: {
                Toast.makeText(this, R.string.auth_error, Toast.LENGTH_SHORT)
                        .show();
            }
            break;
            case MSG_AUTH_COMPLETE: {
                Toast.makeText(this, R.string.auth_complete, Toast.LENGTH_SHORT)
                        .show();
            }
            break;
        }
        return false;
    }

}


