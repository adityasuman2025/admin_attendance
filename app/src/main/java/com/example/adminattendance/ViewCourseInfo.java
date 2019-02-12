package com.example.adminattendance;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class ViewCourseInfo extends AppCompatActivity
{
    TextView courseCode;
    TextView text;
    ListView profLV;
    ListView studLV;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String user_id_cookie;
    String course_id_cookie;
    String data1[];
    String data2[];

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_course_info);

        courseCode = findViewById(R.id.courseCode);
        text = findViewById(R.id.text);

        profLV = findViewById(R.id.profLV);
        studLV = findViewById(R.id.studLV);

        //getting values from cookie
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        user_id_cookie = sharedPreferences.getString("user_id", "DNE");
        String course_code_cookie = sharedPreferences.getString("course_code", "");
        course_id_cookie = sharedPreferences.getString("course_id", "DNE");

        courseCode.setText(course_code_cookie);

        //checking if phone if connected to net or not
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
        {
        //getting the professors list for a course
            String type = "get_profs_of_a_course";

            try
            {
                String get_profs_of_a_courseResult = (new DatabaseActions().execute(type, course_id_cookie).get());

                if(!get_profs_of_a_courseResult.equals("0") && !get_profs_of_a_courseResult.equals("-1") && !get_profs_of_a_courseResult.equals("Something went wrong"))
                {
                //parse JSON data
                    JSONArray ja = new JSONArray(get_profs_of_a_courseResult);
                    JSONObject jo = null;

                    data1 = new String[ja.length()];

                    for (int i =0; i<ja.length(); i++)
                    {
                        jo = ja.getJSONObject(i);

                        String name = jo.getString("name");

                        data1[i] = name;
                    }

                    //listing courses in listview
                    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data1);
                    profLV.setAdapter(adapter);
                }

            //getting the professors list for a course
                type = "get_studs_of_a_course";

                String get_studs_of_a_courseResult = (new DatabaseActions().execute(type, course_id_cookie).get());

                if(!get_studs_of_a_courseResult.equals("0") && !get_studs_of_a_courseResult.equals("-1") && !get_studs_of_a_courseResult.equals("Something went wrong"))
                {
                    //parse JSON data
                    JSONArray ja = new JSONArray(get_studs_of_a_courseResult);
                    JSONObject jo = null;

                    data2 = new String[ja.length()];

                    for (int i =0; i<ja.length(); i++)
                    {
                        jo = ja.getJSONObject(i);

                        String name = jo.getString("name");

                        data2[i] = name;
                    }

                    //listing courses in listview
                    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data2);
                    studLV.setAdapter(adapter);
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
        {
            text.setText("Internet connection is not available");
        }
    }
}
