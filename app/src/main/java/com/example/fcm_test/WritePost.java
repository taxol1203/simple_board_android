package com.example.fcm_test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WritePost extends AppCompatActivity {

    static String IP_ADDRESS = "192.168.1.27";
    static String Tag = "phptest";

    EditText mTitle;
    EditText mWriter;
    EditText mContent;
    Button mOkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        mTitle = findViewById(R.id.write_title);
        mWriter = findViewById(R.id.write_writer);
        mContent = findViewById(R.id.write_content);
        mOkButton = findViewById(R.id.write_button_ok_sign);

        mOkButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String title = mTitle.getText().toString();
                String writer = mWriter.getText().toString();
                String content = mContent.getText().toString();

                InsertData task = new InsertData();
                task.execute("http://" + IP_ADDRESS + "/page/board/write_ok.php", title, writer, content);

            }
        });
    }

    class InsertData extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(WritePost.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();
            Log.e(Tag, "Post response - " + s);
        }

        @Override
        protected String doInBackground(String... params) {

            String title = (String)params[1];
            String writer = (String)params[2];
            String content = (String)params[3];

            // 1. PHP 파일을 실행시킬 수 있는 주소와 전송할 데이터를 준비합니다.
            // POST 방식으로 데이터 전달시에는 데이터가 주소에 직접 입력되지 않습니다.
            String serverURL = (String)params[0];

            // HTTP 메시지 본문에 포함되어 전송되기 때문에 따로 데이터를 준비해야 합니다.
            // 전송할 데이터는 “이름=값” 형식이며 여러 개를 보내야 할 경우에는 항목 사이에 &를 추가합니다.
            // 여기에 적어준 이름을 나중에 PHP에서 사용하여 값을 얻게 됩니다.
            String postParameters = "name=" + writer +"&title=" + title + "&content=" + content;

            try{
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);         //5초안에 응답이 오지 않으면 예외가 발생합니다.
                httpURLConnection.setConnectTimeout(5000);      //5초안에 연결이 안되면 예외가 발생합니다.
                httpURLConnection.setRequestMethod("POST");     //요청 방식을 POST로 합니다.
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes(StandardCharsets.UTF_8));    //전송할 데이터가 저장된 변수를 이곳에 입력합니다. 인코딩을 고려해줘야 합니다.
                outputStream.flush();
                outputStream.close();

                //응답을 읽는다.
                //404 - 웹 서버 접속 실패
                //200 - 성공적.
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.e(Tag, "Post response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK){
                    //정상
                    inputStream = httpURLConnection.getInputStream();
                }else{
                    //예외
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }
                bufferedReader.close();

                return sb.toString();
            } catch (Exception e) {
                Log.d(Tag, "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }
        }
    }
}