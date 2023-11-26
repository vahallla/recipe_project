package com.example.recipe_project;

import static com.example.recipe_project.DataAdapter.TAG;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.Query;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Search extends AppCompatActivity {

    Button tomain;
    Button tofavorite;

    EditText getedt;
    String gotedt;
    ImageButton dosearch;
    TextView tag1, tag2, tag3, tag4;

    List<FindItem> searchlist = new ArrayList<>();
    List<FindItem> gotlist = new ArrayList<>();


    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    Context context;
    TextView textView1, textView2;
    String text1, text2, text3;
    String result;

    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        tomain = findViewById(R.id.search_main_btn);
        tofavorite = findViewById(R.id.search_favotie_btn);
        getedt = findViewById(R.id.search_edt);
        tag1 = findViewById(R.id.search_textView);
        tag2 = findViewById(R.id.search_textView2);
        tag3 = findViewById(R.id.search_textView3);
        tag4 = findViewById(R.id.search_textView4);
        dosearch = findViewById(R.id.search_image_btn);

        //스레드 기능을 활용하여 네이버날씨에서 날씨를 크롤링해오는 부분
        new Thread(new Runnable() {
            @Override
            public void run() {
                 result = performWebCrawling();

                // UI 업데이트를 위해 runOnUiThread 사용
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 결과를 처리하는 부분
                        if (result != null) {
                            Log.d("Weather", "Weather: " + result);
                            // 여기서 가져온 결과를 필요에 맞게 처리하세요.
                        } else {
                            Log.e("Weather", "Failed to retrieve weather information");
                        }
                    }
                });
            }
        }).start();

        //리사이클뷰 어뎁터
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        /* initiate adapter */
        MyRecyclerAdapter mRecyclerAdapter = new MyRecyclerAdapter();

        /* initiate recyclerview */
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<FindItem> mfindItems = new ArrayList<>();

        //파이어베이스의 파이어스토어 db연결
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference docref = db.collection("recipe");


        //뷰 항목 클릭시 레시피 페이지로 인텐트 전달
        mRecyclerAdapter.setOnItemClickListener(new MyRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FindItem item) {
                System.out.println("item = " + item);
                Intent intent = new Intent(Search.this, Recipe.class);
                int recipeId = item.getRecipe_ID();
                intent.putExtra("recipeID", recipeId);
                startActivity(intent);

            }
        });
        //앱 실행시 리스트에 파이어스토어에서 가져온 레시피 관련 값 저장후 리사이클뷰에 반영
        docref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        FindItem item = doc.toObject(FindItem.class);
                        searchlist.add(item);

                    }
                    for (FindItem item : searchlist) {
                        Log.d(TAG, "recipe name = " + item.getRecipe_Name());
                        Log.d(TAG, "recipe tag = " + item.getRecipe_ID());
                    }

                    mRecyclerAdapter.setFindList(searchlist);

                } else {
                    Log.d(TAG, "GET FAILED" + task.getException());
                }
            }
        });
        mRecyclerAdapter.setFindList(mfindItems);

        //edit 텍스트가 변경될때마다 edit 텍스트에 적힌 값에 따라 리사이클뷰 변경
        getedt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = getedt.getText().toString();


                gotlist.clear();
                if (text.equals("")) {
                    mRecyclerAdapter.setFindList(searchlist);
                } else {
                    if (text.equals("일식")) {
                        for (int i = 0; i < searchlist.size(); i++) {
                            if (searchlist.get(i).recipe_tag.contains(text)) {
                                gotlist.add(searchlist.get(i));
                            }
                            mRecyclerAdapter.setFindList(gotlist);
                        }
                    } else if (text.equals("양식")) {
                        for (int i = 0; i < searchlist.size(); i++) {
                            if (searchlist.get(i).recipe_tag.contains(text)) {
                                gotlist.add(searchlist.get(i));
                            }
                            mRecyclerAdapter.setFindList(gotlist);
                        }
                    } else if (text.equals("한식")) {
                        for (int i = 0; i < searchlist.size(); i++) {
                            if (searchlist.get(i).recipe_tag.contains(text)) {
                                gotlist.add(searchlist.get(i));
                            }
                            mRecyclerAdapter.setFindList(gotlist);
                        }
                    } else if (text.equals("중식")) {
                        for (int i = 0; i < searchlist.size(); i++) {
                            if (searchlist.get(i).recipe_tag.contains(text)) {
                                gotlist.add(searchlist.get(i));
                            }
                            mRecyclerAdapter.setFindList(gotlist);
                        }
                    } else if (text.equals("밥")) {
                        for (int i = 0; i < searchlist.size(); i++) {
                            if (searchlist.get(i).recipe_tag.contains(text)) {
                                gotlist.add(searchlist.get(i));
                            }
                            mRecyclerAdapter.setFindList(gotlist);
                        }
                    } else if (text.equals("면")) {
                        for (int i = 0; i < searchlist.size(); i++) {
                            if (searchlist.get(i).recipe_tag.contains(text)) {
                                gotlist.add(searchlist.get(i));
                            }
                            mRecyclerAdapter.setFindList(gotlist);
                        }
                    } else if (text.equals("야식")) {
                        for (int i = 0; i < searchlist.size(); i++) {
                            if (searchlist.get(i).recipe_tag.contains(text)) {
                                gotlist.add(searchlist.get(i));
                            }
                            mRecyclerAdapter.setFindList(gotlist);
                        }
                    } else {
                        for (int i = 0; i < searchlist.size(); i++) {
                            if (searchlist.get(i).recipe_name.contains(text)) {
                                gotlist.add(searchlist.get(i));
                            }
                            mRecyclerAdapter.setFindList(gotlist);
                        }
                    }

                }
            }
        });


        //버튼이벤트
        tomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Search.this, MainActivity.class);
                startActivity(intent);
            }
        });
        tofavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Search.this, Favorite.class);
                startActivity(intent);
            }
        });
        tag1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getedt.setText(tag1.getText().toString());
                gotedt = getedt.getText().toString();
            }
        });
        tag2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getedt.setText(tag2.getText().toString());
                gotedt = getedt.getText().toString();
            }
        });
        tag3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getedt.setText(tag3.getText().toString());
                gotedt = getedt.getText().toString();
            }
        });
        tag4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getedt.setText(tag4.getText().toString());
                gotedt = getedt.getText().toString();
            }
        });

        // 현재 시간과 날씨에 따라 랜덤 메뉴 추천 현재 시간에 따라서만 기동.

        dosearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime();
                try {
                    // 예제로 사용할 날짜와 시간을 설정합니다.
                    gotlist.clear();
                    int ran=0;
                    Random r = new Random();
                    // Calendar 객체를 생성하고 날짜 및 시간을 설정합니다.
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mDate);

                    // 시간을 가져옵니다.
                    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

                    // 아침, 점심, 저녁으로 나누어 처리합니다.
                    if (hourOfDay >= 6 && hourOfDay < 12) {
                        System.out.println("아침");

                        for(int i=0; i< searchlist.size();i++)
                        {
                            if (searchlist.get(i).recipe_tag.contains("아침")) {
                                gotlist.add(searchlist.get(i));
                            }
                        }


                        ran = r.nextInt(gotlist.size()-1);

                        int rann = ran;

                       Toast.makeText(getApplicationContext(),"현재 " + hourOfDay + "시 입니다. 아침식사 추천 메뉴: " + gotlist.get(rann).recipe_name , Toast.LENGTH_LONG).show();
                        //mRecyclerAdapter.setFindList(gotlist);
                        Intent intent = new Intent(Search.this, Recipe.class);

                        // 데이터를 인텐트에 추가
                        intent.putExtra("recipeID", gotlist.get(rann).getRecipe_ID());

                        // 다른 액티비티 시작
                        startActivity(intent);

                    }

                     else if (hourOfDay >= 12 && hourOfDay < 18) {


                        for(int i=0; i< searchlist.size();i++)
                        {
                            if (searchlist.get(i).recipe_tag.contains("점심")) {
                                gotlist.add(searchlist.get(i));
                            }
                        }
                        ran = r.nextInt(gotlist.size()-1);
                        int rann = ran;
                        //mRecyclerAdapter.setFindList(gotlist);
                        Intent intent = new Intent(Search.this, Recipe.class);
                        Toast.makeText(getApplicationContext(),"현재 " + hourOfDay + "시 입니다. 점심식사 추천 메뉴: " + gotlist.get(rann).recipe_name , Toast.LENGTH_LONG).show();

                        // 데이터를 인텐트에 추가
                        intent.putExtra("recipeID", gotlist.get(rann).getRecipe_ID());

                        // 다른 액티비티 시작
                       startActivity(intent);

                    } else {
                        System.out.println("저녁");


                        for(int i=0; i< searchlist.size();i++)
                        {
                            if (searchlist.get(i).recipe_tag.contains("야식")) {
                                gotlist.add(searchlist.get(i));
                            }
                        }
                        ran = r.nextInt(gotlist.size()-1);
                        int rann = ran;
                       // mRecyclerAdapter.setFindList(gotlist);
                        Intent intent = new Intent(Search.this, Recipe.class);
                        Toast.makeText(getApplicationContext(),"현재 " + hourOfDay + "시 입니다. 저녁식사 추천 메뉴: " + gotlist.get(rann).recipe_name , Toast.LENGTH_LONG).show();
                        // 데이터를 인텐트에 추가
                        intent.putExtra("recipeID", gotlist.get(rann).getRecipe_ID());

                        // 다른 액티비티 시작
                        startActivity(intent);
                    }

                } catch (Exception e) {

                }

            }
        });


    }

    private String getTime() {
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }



    private String performWebCrawling() {
        try {
            // Jsoup을 사용하여 웹사이트의 HTML 문서를 가져오기
            Document doc = Jsoup.connect("https://weather.naver.com/today/13140121?cpName=KMA").get();

            // <span class="weather"> 태그에서 텍스트 가져오기
            Element weatherElement = doc.select("span.weather").first();
            if (weatherElement != null) {
                return weatherElement.text();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}

