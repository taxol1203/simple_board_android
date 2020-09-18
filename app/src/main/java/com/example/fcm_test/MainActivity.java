package com.example.fcm_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import static android.content.ContentValues.TAG;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static String IP_ADDRESS = "192.168.1.45";
    static String Tag = "MainBoard";
    String mJsonString;
    ArrayList<PersonalData> mArrayList;

    Button writeButton;
    RecyclerView mRecyclerView;
    BoardAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        writeButton = findViewById(R.id.goto_write);
        mRecyclerView = findViewById(R.id.recycler_board);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        writeButton.setOnClickListener(this);

        mArrayList = new ArrayList<>();
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/page/forAndroid/getjson.php", "");

        Log.d("is end?","yes");
        mAdapter = new BoardAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCMTest", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        // Log and toast
                        Log.d(Tag, token);
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });

        /*FirebaseMessaging.getInstance().subscribeToTopic("weather")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Topic weather is subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Topic weather subscribing failed";
                        }
                        Log.d("FCMTest", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic("windy")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Topic sunny is subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Topic sunny subscribing failed";
                        }
                        Log.d("FCMTest", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });*/
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.goto_write:
                Intent intent1 = new Intent(getApplicationContext(), WritePost.class);
                startActivity(intent1);
        }
    }
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(Tag, "all response - " + result);

            if (result == null){
                Log.e(Tag,errorString);
            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(Tag, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString().trim();


            } catch (Exception e) {
                Log.d(Tag, "GetData : Error ", e);
                errorString = e.toString();
                return null;
            }
        }
    }

    private  void showResult(){
        String TAG_JSON="webnautes";
        String TAG_IDX = "idx";
        String TAG_NAME = "writer";
        String TAG_TITLE ="title";
        String TAG_DATE = "date";
        String TAG_HIT = "hit";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0; i<jsonArray.length(); i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String idx = item.getString(TAG_IDX);
                String writer = item.getString(TAG_NAME);
                String title = item.getString(TAG_TITLE);
                String date = item.getString((TAG_DATE));
                String hit = item.getString((TAG_HIT));

                PersonalData personalData = new PersonalData();

                personalData.setMember_idx(idx);
                personalData.setMember_writer(writer);
                personalData.setMember_title(title);
                personalData.setMember_date(date);
                personalData.setMember_hit(hit);
                //Log.d("check data is right", idx + " + " + writer + " + " + title + " + " + date + " + " + hit + " + " + jsonArray.length());

                mArrayList.add(personalData);
            }

        } catch (JSONException e) {
            Log.d(Tag, "showResult : ", e);
        }
    }

    public static class PersonalData {
        private String member_idx;
        private String member_writer;
        private String member_title;
        private String member_date;
        private String member_hit;

        public String getMember_idx() {
            return member_idx;
        }
        public String getMember_title() {
            return member_title;
        }
        public String getMember_writer() {
            return member_writer;
        }
        public String getMember_date() {
            return member_date;
        }
        public String getMember_hit() {
            return member_hit;
        }

        public void setMember_idx(String member_idx) {
            this.member_idx = member_idx;
        }

        public void setMember_writer(String member_writer) {
            this.member_writer = member_writer;
        }

        public void setMember_title(String member_title) {
            this.member_title = member_title;
        }
        public void setMember_date(String member_date) {
            this.member_date = member_date;
        }
        public void setMember_hit(String member_hit) {
            this.member_hit = member_hit;
        }
    }

    public static class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.CustomViewHolder> {

        private ArrayList<PersonalData> mList = null;
        private Activity context = null;
        public BoardAdapter(Activity context, ArrayList<PersonalData> list) {
            this.context = context;
            this.mList = list;
        }

        public static class CustomViewHolder extends RecyclerView.ViewHolder {
            protected TextView idx;
            protected TextView writer;
            protected TextView title;
            protected TextView date;
            protected TextView hit;

            public CustomViewHolder(View view) {
                super(view);
                this.idx = (TextView) view.findViewById(R.id.post_idx);
                this.writer = (TextView) view.findViewById(R.id.post_writer);
                this.title = (TextView) view.findViewById(R.id.post_title);
                this.date = (TextView) view.findViewById(R.id.post_date);
                this.hit = (TextView) view.findViewById(R.id.post_hit);
            }
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_list, null);
            CustomViewHolder viewHolder = new CustomViewHolder(view);;
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {
            viewholder.idx.setText(mList.get(position).getMember_idx());
            viewholder.writer.setText(mList.get(position).getMember_writer());
            viewholder.title.setText(mList.get(position).getMember_title());
            viewholder.date.setText(mList.get(position).getMember_date().substring(5));
            viewholder.hit.setText(mList.get(position).getMember_hit());
        }

        @Override
        public int getItemCount() {
            return (null != mList ? mList.size() : 0);
        }

    }
}
