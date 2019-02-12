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

public class Register extends AppCompatActivity
{
    //defining variables
    EditText reg_name_input;
    EditText reg_email_input;
    EditText reg_phone_input;
    EditText reg_username_input;
    EditText reg_pass_input;
    EditText reg_con_pass_input;

    TextView reg_feed;
    Button reg_new_btn;

    SharedPreferences sharedPreferences;
    String androidId;
    String uniqueID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reg_name_input = findViewById(R.id.reg_name_input);
        reg_email_input = findViewById(R.id.reg_email_input);
        reg_phone_input = findViewById(R.id.reg_phone_input);
        reg_username_input = findViewById(R.id.reg_username_input);
        reg_pass_input = findViewById(R.id.reg_pass_input);
        reg_con_pass_input = findViewById(R.id.reg_con_pass_input);

        reg_feed = findViewById(R.id.reg_feed);
        reg_new_btn = findViewById(R.id.reg_new_btn);

    //to get unique identification of a phone and displaying it
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); // android id
        uniqueID = android.os.Build.SERIAL; // Serial_no

        //on clicking register button
        reg_new_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String reg_name = reg_name_input.getText().toString();
                String reg_email = reg_email_input.getText().toString();
                String reg_phone = reg_phone_input.getText().toString();

                String reg_username = reg_username_input.getText().toString();
                String reg_pass = reg_pass_input.getText().toString();

                String reg_con_pass = reg_con_pass_input.getText().toString();

                //checking if phone if connected to net or not
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
                {
                    //phone is connected to internet
                    if (reg_pass.equals(reg_con_pass) && reg_pass.length() != 0 && reg_con_pass.length() != 0)
                    {
                        //check if that phone is already registered or not
                        String type = "check_admin_phone_registered";
                        try
                        {
                            int check_phone_result = Integer.parseInt(new DatabaseActions().execute(type, androidId, uniqueID).get());

                            if (check_phone_result == 1)//yes phone is already registered
                            {
                                reg_feed.setText("This phone is already registered for a professor.");
                            }
                            else if (check_phone_result == -1)
                            {
                                reg_feed.setText("Database issue found");
                            }
                            else
                            {
                                //checking if all input fields have been filled or not
                                if (reg_name.length() != 0 && reg_email.length() != 0 && reg_phone.length() != 0 && reg_username.length() != 0)
                                {
                                    //check if that username is already registered or not
                                    type = "check_admin_username_exist";
                                    int check_roll_result = Integer.parseInt(new DatabaseActions().execute(type, reg_username).get());

                                    if (check_roll_result == 1)//yes username already exist
                                    {
                                        reg_feed.setText("This username is already taken.");
                                    }
                                    else if (check_roll_result == -1)
                                    {
                                        reg_feed.setText("Database issue found");
                                    }
                                    else
                                    {
                                        //if everything fine then registering the new user in the database
                                        type = "register_new_admin_in_db";
                                        int register_new_user_result = Integer.parseInt(new DatabaseActions().execute(type, reg_name, reg_email, reg_phone, reg_username, reg_pass, androidId, uniqueID).get());

                                        if (register_new_user_result > 0)//successfully registered
                                        {
                                            //creating cookie of the logged prof
                                            sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("username", Encryption.encrypt(reg_username));
                                            editor.putString("user_id", Encryption.encrypt(Integer.toString(register_new_user_result)));
                                            editor.apply();

                                            //redirecting the prof dashboard page
                                            Intent dashboardIntent = new Intent(Register.this, Dashboard.class);
                                            startActivity(dashboardIntent);
                                            finish(); //used to delete the last activity history which we want to delete
                                        }
                                        else if (register_new_user_result == -1)
                                        {
                                            reg_feed.setText("Database issue found");
                                        }
                                        else
                                        {
                                            reg_feed.setText("Something went wrong registering user");
                                        }
                                    }
                                }
                                else
                                {
                                    reg_feed.setText("Please fill all the input fields");
                                }
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        reg_feed.setText("Password do not match");
                    }
                } else {
                    reg_feed.setText("Internet connection is not available");
                }
            }
        });
    }
}
