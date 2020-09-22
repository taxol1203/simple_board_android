package com.example.fcm_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import static android.content.ContentValues.TAG;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    static String IP_ADDRESS = "192.168.1.27";
    static String Tag = "MainBoard";
    String mJsonString;
    static ArrayList<PersonalData> mArrayList;
    boolean isLoading = false;

    Button writeButton;
    RecyclerView mRecyclerView;
    BoardAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //툴바
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //플로팅 바
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(getApplicationContext(), WritePost.class);
                startActivity(intent1);
            }
        });

        writeButton = findViewById(R.id.goto_write);
        mRecyclerView = findViewById(R.id.recycler_board);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        writeButton.setOnClickListener(this);

        mArrayList = new ArrayList<>();
        //서버로 부터 json 받아옴
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/page/forAndroid/getjson.php", "");

        //리사이클러 뷰 설정
        mAdapter = new BoardAdapter(this, mArrayList);
        mRecyclerView.setAdapter(mAdapter);

        initScrollListener();


        /*FirebaseInstanceId.getInstance().getInstanceId()
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
                });*/

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
    protected void onRestart() {
        super.onRestart();

        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/page/forAndroid/getjson.php", "");
    }

    //toolbar 시작
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.search :
                Toast.makeText(getApplicationContext(), "Search Click", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.option :
                Toast.makeText(getApplicationContext(), "Option Click", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //toolbar 끝
    //click listener 들
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
                showResult(0,5);
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

    private  void showResult(int start, int end){
        String TAG_JSON="webnautes";
        String TAG_IDX = "idx";
        String TAG_NAME = "writer";
        String TAG_TITLE ="title";
        String TAG_DATE = "date";
        String TAG_HIT = "hit";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=start; i<end; i++){

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

    private void initScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == mArrayList.size() - 1) {
                        //bottom of list!
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });


    }

    private void loadMore() {
        mArrayList.add(null);
        mAdapter.notifyItemInserted(mArrayList.size() - 1);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mArrayList.remove(mArrayList.size() - 1);
                int scrollPosition = mArrayList.size();
                mAdapter.notifyItemRemoved(scrollPosition);
                int currentSize = scrollPosition;
                int nextLimit = currentSize + 5;
                Log.e("hihihi", "currentSize:" + String.valueOf(currentSize) + "nextLimit: " + String.valueOf(nextLimit));
                showResult(currentSize , nextLimit);
                /*while (currentSize - 1 < nextLimit) {
                    mArrayList.add("Item " + currentSize);
                    currentSize++;
                }*/

                mAdapter.notifyDataSetChanged();
                isLoading = false;
            }
        }, 2000);


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

    public class BoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<PersonalData> mList = null;
        private Activity context = null;
        //for loading
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;

        public BoardAdapter(Activity context, ArrayList<PersonalData> list) {
            this.context = context;
            this.mList = list;
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
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
                //만약 각 post를 클릭 하였을 시에 GetDataForRead 호출
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
        private class LoadingViewHolder extends RecyclerView.ViewHolder {
            ProgressBar progressBar;

            public LoadingViewHolder(@NonNull View itemView) {
                super(itemView);
                progressBar = itemView.findViewById(R.id.progressBar);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_list, null);
                CustomViewHolder viewHolder = new CustomViewHolder(view);;
                return viewHolder;
            }else {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading, viewGroup, false);
                return new LoadingViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewholder, int position) {
            if (viewholder instanceof CustomViewHolder) {
                populateItemRows((CustomViewHolder) viewholder, position);
            }else if (viewholder instanceof LoadingViewHolder) {
                showLoadingView((LoadingViewHolder) viewholder, position);
            }

        }

        @Override
        public int getItemCount() {
            return (null != mList ? mList.size() : 0);
        }

        @Override
        public int getItemViewType(int position) {
            return mList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        private void showLoadingView(LoadingViewHolder viewHolder, int position) {
            //ProgressBar would be displayed

        }

        private void populateItemRows(CustomViewHolder viewHolder, int position) {

            viewHolder.idx.setText(mList.get(position).getMember_idx());
            viewHolder.writer.setText(mList.get(position).getMember_writer());
            viewHolder.title.setText(mList.get(position).getMember_title());
            viewHolder.date.setText(mList.get(position).getMember_date().substring(5));
            viewHolder.hit.setText(mList.get(position).getMember_hit());
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
            progressDialog = ProgressDialog.show(MainActivity.this,
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


