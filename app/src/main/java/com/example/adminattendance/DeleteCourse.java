package com.example.adminattendance;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.ExecutionException;

public class DeleteCourse extends AppCompatActivity
{
//defining variables
    SharedPreferences sharedPreferences;

    TextView text;
    String type;

    ListView deleteCourseLV;
    String data[];
    String course_ids[];
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_course);

        text = findViewById(R.id.text);
        deleteCourseLV = findViewById(R.id.deleteCourseLV);

    //getting the info of the logged user
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String user_id_cookie = new Encryption().decrypt(sharedPreferences.getString("user_id", "DNE"));

        //checking if phone if connected to net or not
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
        {
            try
            {
            //getting the list of all courses in the database
                type = "get_courses";
                String get_courseResults = new DatabaseActions().execute(type).get();

                if (get_courseResults != "0" && get_courseResults != "-1" && get_courseResults != "Something went wrong") {
                    //parse JSON and getting data
                    JSONArray ja = new JSONArray(get_courseResults);
                    JSONObject jo = null;

                    data = new String[ja.length()];
                    course_ids = new String[ja.length()];

                    for (int i = 0; i < ja.length(); i++) {
                        jo = ja.getJSONObject(i);

                        String course_id = jo.getString("id");
                        String course_code = jo.getString("course_code");

                        data[i] = course_code;
                        course_ids[i] = course_id;
                    }

                    //showing the list of student courses present in the db (to show in the drop down menu for adding new courses)
                    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, data);
                    deleteCourseLV.setAdapter(adapter);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        //on clicking on any course for deleting them
            deleteCourseLV.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    final String course_id_to_delete = course_ids[i];

                    //asking for confirm deletion by creating a dialog box
                    new AlertDialog.Builder(DeleteCourse.this)
                            .setTitle("Confirm Deletion")
                            .setMessage("Are you sure to delete this course from your registered courses")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    //deleting that course from everywhere like courses, student_courses, prof_courses, courses_class_dates, student_attendance
                                    try
                                    {
                                        type = "delete_course_from_everywhere";
                                        String delete_course_id_from_student_coursesResult = (new DatabaseActions().execute(type, course_id_to_delete).get());

                                        if(delete_course_id_from_student_coursesResult.equals("1"))
                                        {
                                            //reloading this activity
                                            finish();
                                            startActivity(getIntent());
                                        }
                                        else
                                        {
                                            text.setText("Something went wrong while deleting that course");
                                        }
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                }
            });
        }
        else
        {
            text.setText("Internet Connection is not available");
        }
    }
}
