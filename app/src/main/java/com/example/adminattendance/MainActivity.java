package com.example.adminattendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
{
//defining variables
    Button login_btn;
    Button register_btn;
    EditText roll_no_input;
    EditText password_input;
    TextView login_feed;

    SharedPreferences sharedPreferences;
    String androidId;
    String uniqueID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login_btn = findViewById(R.id.login_btn);
        register_btn = findViewById(R.id.register_btn);
        roll_no_input = findViewById(R.id.roll_no_input);
        password_input = findViewById(R.id.password_input);
        login_feed = findViewById(R.id.login_feed);

    //checking if already loggedIn or not
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String user_id_cookie = sharedPreferences.getString("user_id", "DNE");

        if(user_id_cookie.equals("DNE"))
        {
            //login_feed.setText("No one is logged in");
        }
        else //if someone is already logged in
        {
            //redirecting the list course page
            Intent ListCourseIntent = new Intent(MainActivity.this, Dashboard.class);
            startActivity(ListCourseIntent);
            finish(); //used to delete the last activity history which we want to delete
        }

        //to get unique identification of a phone and displaying it
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); // android id
        uniqueID = android.os.Build.SERIAL; // Serial_no

        //on clicking on login button
        login_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String roll_no = roll_no_input.getText().toString().toUpperCase();
                String password = password_input.getText().toString();
                String type = "verify_admin_login";

                //checking if phone if connected to net or not
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
                {
                    //trying to login the user
                    try
                    {
                        int login_result = Integer.parseInt(new DatabaseActions().execute(type, roll_no, password, androidId, uniqueID).get());

                        if(login_result > 0)
                        {
                            //creating cookie of the logged in user
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("roll_no", new Encryption().encrypt(roll_no));
                            editor.putString("user_id", new Encryption().encrypt(Integer.toString(login_result)));
                            editor.apply();

                            //login_feed.setText(Integer.toString(login_result));

                            //redirecting the list course page
                            Intent ListCourseIntent = new Intent(MainActivity.this, Dashboard.class);
                            startActivity(ListCourseIntent);
                            finish(); //used to delete the last activity history which we don't want to delete
                        }
                        else if(login_result == -1)
                        {
                            login_feed.setText("Database issue found");
                        }
                        else
                        {
                            login_feed.setText("Your login credentials may be incorrect or this may be not your registered phone.");
                        }

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    login_feed.setText("Internet connection is not available");
                }
            }
        });

        //on clicking on register button
        register_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent homeIntent = new Intent(MainActivity.this, Register.class);
                startActivity(homeIntent);
                finish(); //used to delete the last activity history which we want to delete
            }
        });
    }
}
