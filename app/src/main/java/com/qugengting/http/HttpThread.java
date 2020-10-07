package com.qugengting.http;

import android.os.Handler;
import android.os.Message;

/**
 * Created by DQDQ on 16/5/12.
 */
public class HttpThread {
    private final String Url;
    private Handler handler;
    private int action=0;
    private Thread thread;
    public HttpThread(final String Url, Handler handler, final int action)
    {
         this.handler=handler;
         this.Url=Url;
         this.action=action;
    }
    public void Close()
    {
      //  this.
    }
    public void Start()
    {
        try {
       new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpGetXml httpGetXml = new HttpGetXml();
                    String Responese = httpGetXml.getXml(Url);
                    Message msg = new Message();
                    msg.what=action;
                    msg.obj = Responese;
                    handler.sendMessage(msg);
                }
            }).start();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
