package io.fogcloud.demo;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import io.fogcloud.sdk.easylink.api.EasyLink;
//import io.fogcloud.sdk.easylink.api.EasylinkP2P;
import io.fogcloud.sdk.easylink.helper.EasyLinkCallBack;
import io.fogcloud.sdk.easylink.helper.EasyLinkParams;
import io.fogcloud.sdk.mdns.api.MDNS;
import io.fogcloud.sdk.mdns.helper.SearchDeviceCallBack;

public class MainActivity extends AppCompatActivity {

    private String TAG = "---main---";
    private Context mContext;// 上下文
    private EditText log_view;
    private int countno;
    private  MDNS mdns;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        final EasyLink el = new EasyLink(MainActivity.this);
        //final EasylinkP2P elp2p = new EasylinkP2P(mContext);


        final TextView easylinktest = (TextView) findViewById(R.id.easylinktest);
//        TextView easylinkstop = (TextView) findViewById(R.id.easylinkstop);

        final EditText psw = (EditText) findViewById(R.id.psw);
        final EditText ssid = (EditText) findViewById(R.id.ssid);
        if(ssid!=null) {
            ssid.setText(el.getSSID());
        }

        log_view = (EditText) findViewById(R.id.log);
        if(log_view != null) {
            log_view.setText("");
        }

        ssid.setText("IvyWang");
        psw.setText("03513947");

        mdns = new MDNS(this);

        if(easylinktest != null & ssid != null & psw != null) {
            easylinktest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (easylinktest.getText().toString().equalsIgnoreCase("发送配网")) {
                        easylinktest.setText("关闭配网");
                        easylinktest.setBackgroundColor(Color.rgb(255, 0, 0));
                        EasyLinkParams elp = new EasyLinkParams();
                        elp.ssid = ssid.getText().toString().trim();
                        elp.password = psw.getText().toString().trim();
                        elp.sleeptime = 50;
                        elp.runSecond = 60000;
                        Toast.makeText(mContext, "open easylink", Toast.LENGTH_SHORT).show();

                        el.startEasyLink(elp, new EasyLinkCallBack() {
                            @Override
                            public void onSuccess(int code, String message) {
//                                Log.d(TAG,">>>>>>>>>>");
                                Log.d(TAG, message);
                                mdns.startSearchDevices("_easylink._tcp.local.", new SearchDeviceCallBack() {
                                    @Override
                                    public void onSuccess(int code, String message) {
                                        super.onSuccess(code, message);
                                        Log.d("---mdns---", message);
                                    }

                                    @Override
                                    public void onFailure(int code, String message) {
                                        super.onFailure(code, message);
                                        Log.d("---mdns---", message);
                                    }

                                    @Override
                                    public void onDevicesFind(int code, JSONArray deviceStatus) {
                                        super.onDevicesFind(code, deviceStatus);
                                        if (!deviceStatus.equals("")) {
                                            Log.d("---mdns---", deviceStatus.toString());
                                        }
                                    }
                                });

                                send2handler(1, message);
                            }

                            @Override
                            public void onFailure(int code, String message) {
                                Log.d(TAG, message);
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        easylinktest.setText("发送配网");
                        easylinktest.setBackgroundColor(Color.rgb(63, 81, 181));
                        Toast.makeText(mContext, "stop easylink", Toast.LENGTH_SHORT).show();
                        //elp2p.stopEasyLink(new EasyLinkCallBack() {
                        mdns.stopSearchDevices(new SearchDeviceCallBack() {
                            public void onSuccess(int code, String message) {
                                Log.d("---mdns---", message);
                            };
                            @Override
                            public void onFailure(int code, String message) {
                                Log.d("---mdns---", message);
                            }
                        });

                        el.stopEasyLink(new EasyLinkCallBack() {
                            @Override
                            public void onSuccess(int code, String message) {

                                Log.d(TAG, message);
                                send2handler(2, message);
                            }

                            @Override
                            public void onFailure(int code, String message) {
                                Log.d(TAG, message);
                            }
                        });
                    }
                }
            });
        }
    }

    private void send2handler(int code, String message) {
        Message msg = new Message();
        msg.what = code;
        msg.obj = message;
        LHandler.sendMessage(msg);
    }

    Handler LHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                log_view.setText(msg.obj.toString().trim() + "\r\n");
            }
            if (msg.what == 2) {
                log_view.setText("");
            }
        }
    };
}

