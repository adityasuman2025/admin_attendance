package com.example.adminattendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class SettingsFragment extends Fragment {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ListView settingsLV;
    TextView text;
    TextView studentDetails;

    String data[] = {"Add Course", "Delete Course", "Log Out"};
    ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_settings, null);

        settingsLV = view.findViewById(R.id.settingsLV);
        text = view.findViewById(R.id.text);
        studentDetails = view.findViewById(R.id.studentDetails);

        text.setText("Settings");

        //getting the info of the logged user
        sharedPreferences = this.getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String user_id_cookie = sharedPreferences.getString("user_id", "DNE");
        editor = sharedPreferences.edit();

        String user_id = new Encryption().decrypt(user_id_cookie);

        //checking if phone if connected to net or not
        ConnectivityManager connMgr = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) //phone is connected
        {
            //showing prof details
            String studentDetailsResults = null;
            try {
                String type = "get_admin_details";
                studentDetailsResults = new DatabaseActions().execute(type, user_id).get();

                //parse JSON and getting data
                JSONArray ja = new JSONArray(studentDetailsResults);
                JSONObject jo = ja.getJSONObject(0);

                String name = jo.getString("name");
                String username = jo.getString("username");
                String email_id = jo.getString("email_id");
                String phone = jo.getString("phone");

                studentDetails.setText("Name: " + name + "\nUsername: " + username + "\nEmail ID: " + email_id + "\nPhone No: " + phone);
            }
            catch (JSONException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else
        {
            studentDetails.setText("");
        }

        //listing setting options in listview
        adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, data);
        settingsLV.setAdapter(adapter);

        //on clicking on any list item
        settingsLV.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                //on clicking on setting options
                String listViewText = ((TextView)view).getText().toString();

                if(listViewText.equals(data[0])) //add course
                {
                    //redirecting to the manage courses page
                    Intent manageCoursePage = new Intent(getActivity().getApplicationContext(), AddCourse.class);
                    startActivity(manageCoursePage);
                }
                if(listViewText.equals(data[1])) //add course
                {
                    //redirecting to the manage courses page
                    Intent manageCoursePage = new Intent(getActivity().getApplicationContext(), DeleteCourse.class);
                    startActivity(manageCoursePage);
                }
                else if(listViewText.equals(data[2])) //logout
                {
                    //removing all cookies
                    editor.remove("user_id");
                    editor.remove("username");
                    editor.remove("course_id");
                    editor.remove("studentCourses");
                    editor.commit();

                    //redirecting to the login page
                    Intent loginPage = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                    startActivity(loginPage);
                    getActivity().finish();
                }
            }
        });

        return view;
    }
}
