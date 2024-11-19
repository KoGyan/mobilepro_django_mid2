package com.example.phogoviewer;
import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private List<Bitmap> imageList = new ArrayList<>();
    private static final String API_URL = "http://10.0.2.2:8000/api_root/Post/"; // 서버 API 주소
    public myPictureView myPictureView;
    File[] imageFiles = new File[0];
    private Button btnHide, btnShow;

    String imageFname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MODE_PRIVATE);
        File photosDir = new File("/storage/emulated/0/Pictures/PhotosEditor/");


        textView = findViewById(R.id.textView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 어댑터 초기화 및 RecyclerView에 설정
        adapter = new ImageAdapter(imageList);
        recyclerView.setAdapter(adapter);
        btnHide = findViewById(R.id.imagehide);
        btnShow = findViewById(R.id.imageseen);
        myPictureView = findViewById(R.id.myPictureView1);

        Button btnLoad = findViewById(R.id.btn_load);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ImageFetchTask().execute(); // 서버에서 이미지 데이터를 가져오는 작업 시작
            }
        });


        ZoomControls zoomControls = findViewById(R.id.zoomControls);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPictureView.setPivotX(0);
                myPictureView.setPivotY(0);
                myPictureView.setScaleX(myPictureView.getScaleX() + 0.1f);
                myPictureView.setScaleY(myPictureView.getScaleY() + 0.1f);
                Toast.makeText(MainActivity.this, "이미지 확대", Toast.LENGTH_SHORT).show();
            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPictureView.setPivotX(0);
                myPictureView.setPivotY(0);
                myPictureView.setScaleX(myPictureView.getScaleX() - 0.1f);
                myPictureView.setScaleY(myPictureView.getScaleY() - 0.1f);
                Toast.makeText(MainActivity.this, "이미지 축소", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnRotate = findViewById(R.id.btn_rotate);
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPictureView.setPivotX(myPictureView.getWidth() / 2);
                myPictureView.setPivotY(myPictureView.getHeight() / 2);
                myPictureView.setRotation(myPictureView.getRotation() + 90); // 90도 회전
                Toast.makeText(MainActivity.this, "이미지 회전", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnUpload = findViewById(R.id.btn_save);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File imageFile = new File("/storage/emulated/0/Pictures/PhotosEditor/image.jpeg");
                if (imageFile.exists()) {
                    myPictureView.setImagePath();  // 이미지 경로 설정
                    myPictureView.invalidate();  // 뷰 다시 그리기
                    Toast.makeText(MainActivity.this, "이미지가 표시되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "이미지가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("MainActivity", "이미지 파일이 없습니다: " + imageFile.getAbsolutePath());
                }
            }
        });

        btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                myPictureView.setVisibility(View.GONE);
            }
        });

        // 이미지 보기 버튼 클릭 이벤트
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                myPictureView.setVisibility(View.VISIBLE);
            }
        });
    }

    // 서버에서 이미지 목록을 가져오는 비동기 작업
    private class ImageFetchTask extends AsyncTask<Void, Void, List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(Void... voids) {
            List<Bitmap> bitmaps = new ArrayList<>();
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");



                InputStream inputStream = connection.getInputStream();
                StringBuilder response = new StringBuilder();
                int data;
                while ((data = inputStream.read()) != -1) {
                    response.append((char) data);
                }
                inputStream.close();

                // JSON 응답 파싱
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("results"); // "results" 배열 가져오기

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonImageObject = jsonArray.getJSONObject(i);
                    String imageUrl = jsonImageObject.getString("image").replace("127.0.0.1", "10.0.2.2");

                    Bitmap bitmap = downloadImage(imageUrl);
                    if (bitmap != null) {
                        bitmaps.add(bitmap);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(List<Bitmap> result) {
            if (result.isEmpty()) {
                textView.setText("불러올 이미지가 없습니다.");
            } else {
                textView.setText("이미지 로드 성공");
                imageList.addAll(result);
                adapter.notifyDataSetChanged();
            }
        }

        // 이미지 URL을 이용해 Bitmap 객체로 변환
        private Bitmap downloadImage(String imageUrl) {
            Bitmap bitmap = null;
            try {
                imageUrl = imageUrl.replace("127.0.0.1", "10.0.2.2");
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }
}
