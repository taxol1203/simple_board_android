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
import android.widget.TextView;

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
import java.util.Objects;

public class SearchResultActivity extends AppCompatActivity {

    String catgo;
    String keyword;
    String mJsonString;
    static String IP_ADDRESS = "192.168.1.27";
    static String Tag = "Search";
    static ArrayList<PersonalData> mArrayList;
    RecyclerView mRecyclerView;
    SearchAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        getIntentData();

        mRecyclerView = findViewById(R.id.recycler_search);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mArrayList = new ArrayList<>();
        //서버로 부터 json 받아옴
        SearchResultActivity.GetData task = new SearchResultActivity.GetData();
        task.execute("http://" + IP_ADDRESS + "/page/forAndroid/searchjson.php", catgo, keyword);

        //리사이클러 뷰 설정
        mAdapter = new SearchResultActivity.SearchAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);

    }

    void getIntentData(){
        Intent intent = getIntent();
        catgo = Objects.requireNonNull(intent.getExtras()).getString("catgo");
        keyword = Objects.requireNonNull(intent.getExtras()).getString("keyword");

    }


    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SearchResultActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(Tag, "After Search response - " + result);

            if (result == null){
                Log.e(Tag,errorString);
            }
            else {
                mJsonString = result;
                //Json에 담겨있는 Array의 개수를 센다.
                showResult();
                mAdapter.notifyDataSetChanged();;
            }
        }


        @Override
        protected String doInBackground(String... params) {

            //php에 전송할 데이터들을 넣는 작업. &로 각 key를 구분한다.
            String serverURL = params[0];
            String mcatgo = (String)params[1];
            String mkeyword = (String)params[2];
            String postParameters = "catgo=" + mcatgo +"&keyword=" + mkeyword;

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
                Log.d(Tag, "search response code - " + responseStatusCode);

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

            for(int i=0;i<jsonArray.length();i++){
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

    public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.CustomViewHolder> {

        private ArrayList<PersonalData> mList = null;
        private Activity context = null;


        public SearchAdapter(Activity context, ArrayList<PersonalData> list) {
            this.context = context;
            this.mList = list;
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
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

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PersonalData personalData = mArrayList.get(getAdapterPosition());
                        String idx = personalData.getMember_idx();
                        GetDataForRead task = new GetDataForRead();
                        task.execute("http://" + IP_ADDRESS + "/page/forAndroid/read_post_json.php", idx);
                        Log.e("click test", String.valueOf(getAdapterPosition()));
                    }
                });
            }
        }


        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_list, null);
            CustomViewHolder viewHolder = new CustomViewHolder(view);

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

    //글 읽기 위한 php 코드
    //get json file from read_post_json.php which have information about post
    private class GetDataForRead extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SearchResultActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(Tag, "Each post response - " + result);

            if (result == null){
                Log.e(Tag,errorString);
            }
            else {
                mJsonString = result;
                //json으로 부터 데이터를 받아서
                String TAG_JSON="webnautes";
                String TAG_IDX = "idx";
                String TAG_NAME = "writer";
                String TAG_TITLE ="title";
                String TAG_DATE = "date";
                String TAG_HIT = "hit";
                String TAG_CONTENT = "content";

                try {
                    JSONObject jsonObject = new JSONObject(mJsonString);
                    JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                    JSONObject item = jsonArray.getJSONObject(0);

                    String idx = item.getString(TAG_IDX);
                    String writer = item.getString(TAG_NAME);
                    String title = item.getString(TAG_TITLE);
                    String date = item.getString((TAG_DATE));
                    String hit = item.getString((TAG_HIT));
                    String content = item.getString((TAG_CONTENT));
                    //Log.d("check data is right", idx + " + " + writer + " + " + title + " + " + date + " + " + hit + " + " + content);
                    //ReadPost에 전송한다.
                    Intent intent2 = new Intent(getApplicationContext(), ReadPost.class);
                    intent2.putExtra("idx", idx);
                    intent2.putExtra("writer", writer);
                    intent2.putExtra("title", title);
                    intent2.putExtra("date", date);
                    intent2.putExtra("hit", hit);
                    intent2.putExtra("content", content);
                    startActivity(intent2);
                } catch (JSONException e) {
                    Log.d(Tag, "showResult : ", e);
                }
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            //php에 전달할려는 인자 setting
            String postParameters = "idx=" + (String)params[1];

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
                Log.d(Tag , "check params:" +  postParameters); //<- 받아진건가?

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
                Log.d(Tag, "GetData From  : Error ", e);
                errorString = e.toString();
                return null;
            }
        }
    }

}