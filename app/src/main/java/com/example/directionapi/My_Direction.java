package com.example.directionapi;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class My_Direction {

    private LatLng Start;
    private LatLng End;
    private String mode = "driving";
    private ArrayList<LatLng> overview_polyline = new ArrayList<LatLng>();

    private String Location_url = "";
    private String web_text = "";
    private String key ="";

    private String Location_url_1 = "https://maps.googleapis.com/maps/api/directions/json?destination=";
    private String Location_url_2 = "&mode=";
    private String Location_url_3 = "&origin=";
    private String Location_url_4 = "&language=zh-TW&key=";


    public My_Direction(LatLng Origin, ArrayList<LatLng> Destination, String KEY){
        Start = Origin;
        End   = Destination.get(0);
        key   = KEY;
        if(Destination.size()==1) {
            String start = Start.latitude + "," + Start.longitude;
            String end = End.latitude + "," + End.longitude;
            Location_url = Location_url_1 + end + Location_url_2 + mode + Location_url_3 + start + Location_url_4 + key;
        }
        else{
            //multiple
        }
    }
    public void SearchDirection(final onDataReadyCallback callback){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                web_text = run_content(Location_url);
                get_Direction(web_text);
                callback.onDataReady(overview_polyline);
//                //callback.onDataReady("New Data");
            }
        };
        //runnable.run();
        Thread t2 = new Thread(runnable);
        t2.start();
    }

    private String run_content(String api_url){
        String text = "";
        HttpURLConnection connection = null;
        try {
            System.out.println(api_url);
            // 初始化 URL
            URL url = new URL(api_url);
            // 取得連線物件
            connection = (HttpURLConnection) url.openConnection();
            // 設定 request timeout
            connection.setReadTimeout(1500);
            connection.setConnectTimeout(1500);
            // 模擬 Chrome 的 user agent, 因為手機的網頁內容較不完整
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");
            // 設定開啟自動轉址
            connection.setInstanceFollowRedirects(true);

            // 若要求回傳 200 OK 表示成功取得網頁內容
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                // 讀取網頁內容
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String tempStr;
                StringBuffer stringBuffer = new StringBuffer();

                while ((tempStr = bufferedReader.readLine()) != null) {
                    stringBuffer.append(tempStr);
                }

                bufferedReader.close();
                inputStream.close();

                // 取得網頁內容類型
                String mime = connection.getContentType();
                boolean isMediaStream = false;

                // 判斷是否為串流檔案
                if (mime.indexOf("audio") == 0 || mime.indexOf("video") == 0) {
                    isMediaStream = true;
                }

                // 網頁內容字串
                String responseString = stringBuffer.toString();

                text = responseString;
                //System.out.println(web_text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 中斷連線
            if (connection != null) {
                connection.disconnect();
            }
        }
        return text;
    }

    public void get_Direction(String text){
        ArrayList<String> routes = new ArrayList<String>();
        ArrayList<String> polyline_overview = new ArrayList<String>();
        ArrayList<String> points = new ArrayList<String>();
        ArrayList<String> legs = new ArrayList<String>();
        ArrayList<String> steps = new ArrayList<String>();
        ArrayList<String> end_location_s = new ArrayList<String>();
        ArrayList<String> html_instrustions     = new ArrayList<String>();
        ArrayList<String> html_instrustions_tmp = new ArrayList<String>();
        ArrayList<String> maneuver = new ArrayList<String>();
        ArrayList<String> lat = new ArrayList<String>();
        ArrayList<String> lng = new ArrayList<String>();
        get_json(text, routes, "routes");
        get_json(routes, polyline_overview, "overview_polyline");
        get_json(polyline_overview, points, "points");
        //get every point
        get_json(routes, legs, "legs");
        get_json(legs, steps, "steps");


        get_json(steps, end_location_s, "end_location");
        get_json(steps, html_instrustions_tmp, "html_instructions");
        //my_json.get_json(steps, maneuver, "maneuver");

        //取得Lat Lng
        //my_json.get_json(Start_location_s, lat, "lat");
        get_json(end_location_s, lat, "lat");
        get_json(end_location_s, lng, "lng");

        Polyline_decoder(points, overview_polyline);

        //overview_polyline = change_ArrayList(lat, lng);
    }
    public void get_json(String text, ArrayList<String> arraylist, String tag){
        //JsonConfig conf = new JsonConfig();
        //往JSONArray中新增JSONObject物件。
        //發現JSONArray跟JSONObject的區別就是JSONArray比JSONObject多中括號[]
        if (text.charAt(0) != '['){
            text = "[" + text + "]";
        }

        try {
            //建立一個JSONArray並帶入JSON格式文字，getString(String key)取出欄位的數值
            JSONArray array = new JSONArray(text);
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                arraylist.add(json.getString(tag));
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
    public void get_json(ArrayList<String> text, ArrayList<String> arraylist, String tag){
        //JsonConfig conf = new JsonConfig();
        //往JSONArray中新增JSONObject物件。
        //發現JSONArray跟JSONObject的區別就是JSONArray比JSONObject多中括號[]
        for(int i=0 ;i <text.size();i++) {
            String tmp = text.get(i);
            if (tmp.charAt(0) != '[') {
                tmp= "[" + tmp + "]";
            }
            try {
                //建立一個JSONArray並帶入JSON格式文字，getString(String key)取出欄位的數值
                JSONArray array = new JSONArray(tmp);
                for (int j = 0; j < array.length(); j++) {
                    JSONObject json = array.getJSONObject(j);
                    //String ch = delete_english(json.getString(tag));
                    //arraylist.add(ch);
                    arraylist.add(json.getString(tag));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public ArrayList<LatLng> change_ArrayList(ArrayList<String> lat, ArrayList<String> lng){
        ArrayList<LatLng> ans = new ArrayList<LatLng>();
        for(int i=0; i<lat.size(); i++){
            double Lat = Double.parseDouble(lat.get(i));
            double Lng = Double.parseDouble(lng.get(i));
            LatLng tmp = new LatLng(Lat, Lng);
            ans.add(tmp);
        }
        return ans;
    }
    public void Polyline_decoder(ArrayList<String> list, ArrayList<LatLng> Poly_List) {
        //get all the polylines point
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
            String encoded = list.get(i);
            int index = 0, len = encoded.length();
            int decoded_lat = 0;
            int decoded_lng = 0;
            //get one char in loop
            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    //get on char to calculate decoder
                    b = encoded.charAt(index++);
                    //step 1: number reduce 63
                    b = b - 63;
                    //step 2: number logic operation(AND) 0x1f and then left shift one bit
                    result |= (b & 0x1f) << shift;
                    //step 3: five bit for one block
                    shift += 5;
                } while (b >= 0x20);
                //step 4: if first bit is one need to bit upside down, and do shift on right one bit.
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                decoded_lat += dlat;
                shift = 0;
                result = 0;
                //do the same thing with lng
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                decoded_lng += dlng;
                LatLng p = new LatLng((((double) decoded_lat / 1E5)), (((double) decoded_lng / 1E5)));
                Poly_List.add(p);
            }
        }
    }
    interface onDataReadyCallback {
        void onDataReady(ArrayList<LatLng> data);
    }
}
