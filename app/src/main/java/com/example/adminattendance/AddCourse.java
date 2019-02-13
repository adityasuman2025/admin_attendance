package com.example.adminattendance;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class AddCourse extends AppCompatActivity
{
//defining variables
    EditText course_code_input;
    EditText course_name_input;
    EditText degree_input;
    EditText branch_input;

    EditText course_from_input;
    EditText course_to_input;
    Calendar myCalendar;

    TextView add_course_feed;
    Button add_course_btn;

    SharedPreferences sharedPreferences;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        course_code_input = findViewById(R.id.course_code_input);
        course_name_input = findViewById(R.id.course_name_input);
        degree_input = findViewById(R.id.degree_input);
        branch_input = findViewById(R.id.branch_input);
        course_from_input = findViewById(R.id.course_from_input);
        course_to_input = findViewById(R.id.course_to_input);

        add_course_feed = findViewById(R.id.add_course_feed);
        add_course_btn = findViewById(R.id.add_course_btn);

    //getting the info of the logged user
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        final String user_id_cookie = new Encryption().decrypt(sharedPreferences.getString("user_id", "DNE"));

    //for getting date picker in course_from
        myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date_course_from = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabelCourseFrom();
            }
        };

        course_from_input.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddCourse.this, date_course_from, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    //for getting date picker in course_to
        final DatePickerDialog.OnDateSetListener date_course_to = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabelCourseTo();
            }
        };

        course_to_input.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddCourse.this, date_course_to, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    //on clicking on add button
        add_course_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
            //checking if phone if connected to net or not
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
                {
                    String course_code = course_code_input.getText().toString().toUpperCase();
                    String course_name = course_name_input.getText().toString();
                    String degree = degree_input.getText().toString();
                    String branch = branch_input.getText().toString();

                    String course_from = course_from_input.getText().toString();
                    String course_to = course_to_input.getText().toString();

                    if(course_code.length() !=0 && course_code.length() !=0 && course_name.length() !=0 && degree.length() !=0 && branch.length() !=0 && course_from.length() !=0 && course_to.length() !=0)
                    {
                        try
                        {
                        //to check if that course code already exist in db
                            type = "check_course_code_exist";
                            String check_course_code_existResult = new DatabaseActions().execute(type, course_code).get();

                            if(!check_course_code_existResult.equals("1"))
                            {
                            //inserting the new course in the database
                                type = "add_new_course_in_db";
                                String add_new_course_in_dbResult = new DatabaseActions().execute(type, course_code, course_name, degree, branch, course_from, course_to).get();

                                if(add_new_course_in_dbResult.equals("1"))
                                {
                                    finish();
                                }
                                else
                                {
                                    add_course_feed.setText("Something went wrong while adding new course");
                                }
                            }
                            else
                            {
                                add_course_feed.setText("This course had already been added");
                            }
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        add_course_feed.setText("Fill all the input fields");
                    }
                }
                else
                {
                    add_course_feed.setText("Internet Connection is not available.");
                }
            }
        });
    }

//for showing picked dates in course_from and course_to input
    private void updateLabelCourseFrom()
    {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        course_from_input.setText(sdf.format(myCalendar.getTime()));
    }

    private void updateLabelCourseTo()
    {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

        course_to_input.setText(sdf.format(myCalendar.getTime()));
    }
}
