package com.example.jeogeum;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class YourWritingActivity extends AppCompatActivity{
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private static final String TAG = "ShowMyList";
    private boolean Lock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_writing);

        Button close_btn = (Button) findViewById(R.id.close_btn);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // post 중 Lock 안 걸린 전체 출력
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("post")
                //.whereEqualTo("lock", Lock)  아직 디비 완성 안됨.
                .whereEqualTo("lock", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // 배열은 사이즈가 고정되어 있으므로 ArrayList로 받고 배열로 변환
                            ArrayList<String> array1 = new ArrayList();
                            ArrayList<String> array2 = new ArrayList();
                            ArrayList<String> array3 = new ArrayList();
                            // 데이터 값 ArrayList에서 받기
                            // 들어갈 데이터가 글감
                            String[] check = {"word", "nick", "text"};
                            for(int i=0;i<3;i++) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (i == 0) {
                                        array1.add(document.getData().get(check[i]).toString());
                                    }
                                    else if(i == 1) {
                                        array2.add(document.getData().get(check[i]).toString());
                                    }
                                    else {
                                        array3.add(document.getData().get(check[i]).toString());
                                    }
                                }
                            }
                            // 배열 List크기 만큼 선언 후 값 넣기
                            String[][] myDataset = new String[4][array1.size()];
                            int size = 0;
                            for (String temp : array1) {
                                myDataset[0][size++] = temp;
                            }
                            size = 0;
                            for (String temp : array2) {
                                myDataset[1][size++] = temp;
                            }
                            size = 0;
                            for (String temp : array3) {
                                myDataset[2][size++] = temp;
                            }

                            myDataset[3][0] = "false";
                            myWritingAdapter mAdapter = new myWritingAdapter(myDataset);
                            mAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(mAdapter);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

}