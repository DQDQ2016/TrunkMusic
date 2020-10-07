package com.qugengting.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by DQDQ on 16/5/12.
 */
public class HttpGetXml {

     private URL url;
     private HttpURLConnection urlConnection;
     private InputStream inputStream;
     private StringBuilder builder;
     private String line;
     private BufferedReader reader;

    public String getXml(String mUrl)
    {
        try {
//            mUrl+="?testid=dq123&testid2=rex";
            url = new URL(mUrl);
            urlConnection=(HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(8000);
            urlConnection.setReadTimeout(8000);
            inputStream=urlConnection.getInputStream();
            reader=new BufferedReader(new InputStreamReader(inputStream));
            builder=new StringBuilder();

            while((line = reader.readLine()) != null)
               builder.append(line);

            return  builder.toString();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (urlConnection!=null)
                urlConnection.disconnect();
        }
        return "";
    }
}
