package com.example.jeogeum;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main_WriteContent extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FirebaseFirestore db;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    SharedPreferences sharedPreferences, word_save_preference;
    String shared = "file", word_save = "word_save";

    public static final String Text_KEY = "text";
    public static final String Lock_KEY = "lock";
    public static final String Nick_KEY = "nick";
    public static final String Date_KEY = "date";
    public static final String Word_KEY = "word";
    public static final String Used_KEY= "used";

    String date, word, email, nick;
    EditText write_text;
    CheckBox checkBox;
    TextView main_word;

    //private long backBtnTime = 0;

    //ProgressBar progressBar;
    private static final String TAG = "Main_Writecontent";

    public static int count= 0;
    SimpleDateFormat today = new SimpleDateFormat ("yyyyMMdd");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__write_content);

        sharedPreferences = getSharedPreferences(shared, 0);
        word_save_preference = getSharedPreferences(word_save, 0);

        email = getIntent().getStringExtra("email");
        db = FirebaseFirestore.getInstance();
        //progressBar = findViewById(R.id.progress);

        main_word = findViewById(R.id.main_word);
        //settingWord();

        //????????? ????????? ????????? ?????? ????????? ?????? ?????????
        checkwordused();
        find_nick();
        Button main_complete_btn = findViewById(R.id.main_complete_btn);
        main_complete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progressBar.setVisibility(View.VISIBLE);
                saveText(v);
            }
        });

        navbar();
        set_nick();
        //practice();
    }

    public void navbar(){
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.openNavDrawer,
                R.string.closeNavDrawer
        );

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("????????? ??????????????????.");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(Main_WriteContent.this, SearchText.class);
                intent.putExtra("searchdata", query);
                startActivity(intent);

                Toast.makeText(Main_WriteContent.this, "???????????? : " + query, Toast.LENGTH_SHORT).show();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;
    }

    public void set_nick(){
        DocumentReference UserRef = db.collection("user").document(email);
        UserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                inflater.inflate(R.layout.nav_header_layout, null , false);
                TextView user = (TextView)findViewById(R.id.user);
                if (documentSnapshot.exists()) {
                    String nick = documentSnapshot.getString("nickname");
                    user.setText(nick + " ???");
                }
            }
        });
    }

    /*private void set_nick(){
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.nav_header_layout, null , false);

        TextView user = (TextView)findViewById(R.id.user);

        String value = sharedPreferences.getString("nick", "");
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
        //user.setText((CharSequence) value);
        //user.setText(value+ " ???");
    }*/

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.write) {
            Toast.makeText(this, "?????? ??????????????????.", Toast.LENGTH_LONG).show();
        } else if (id == R.id.my) {
            Intent intent = new Intent(Main_WriteContent.this, MyWritingActivity.class);
            intent.putExtra("nick", nick);
            startActivity(intent);
        } else if (id == R.id.your) {
            Intent intent = new Intent(Main_WriteContent.this, YourWritingActivity.class);
            intent.putExtra("nick", nick);
            startActivity(intent);
        } else if (id == R.id.intro) {
            Intent intent = new Intent(Main_WriteContent.this, StartActivity.class);
            startActivity(intent);
        } else if (id == R.id.logout) {
            Toast.makeText(this, "???????????? ??????.", Toast.LENGTH_LONG).show();
            firebaseAuth.signOut();
            finish();
        } else if (id == R.id.setting) {
            //Toast.makeText(this, "???????????? ??????.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Main_WriteContent.this, PreSettingsActivity.class);
            intent.putExtra("email", email);
            //intent.putExtra("nick", nick);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    public void onBackPressed() {
        //???????????? ?????? ????????? ??????????????? ????????? ??????

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        //???????????? ?????? ????????? ??? ??????
//        long curTime = System.currentTimeMillis();
//        long gapTime = curTime - backBtnTime;
//        if(0 <= gapTime && 2000 >= gapTime){
//            super.onBackPressed();
//        } else {
//            backBtnTime = curTime;
//            Toast.makeText(Main_WriteContent.this, "?????? ??? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
//        }

    }

    public void find_nick(){
        DocumentReference UserRef = db.collection("user").document(email);

        UserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    nick = documentSnapshot.getString("nickname");
                }
            }
        });
    }
    public void saveText(View view) {
        write_text = findViewById(R.id.write_text);
        String text = write_text.getText().toString();
        Date currentTime = Calendar.getInstance().getTime();

        if (text.isEmpty()) {
            //progressBar.setVisibility(View.GONE);
            Toast.makeText(Main_WriteContent.this, "?????? X", Toast.LENGTH_SHORT).show();
        } else {
            Map<String, Object> post = new HashMap<>();
            post.put(Text_KEY, text);
            post.put(Lock_KEY, checkcheckbox());
            post.put(Nick_KEY, nick);
            post.put(Date_KEY, currentTime);
            post.put(Word_KEY, word);

            db.collection("post")
                    .add(post)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            //progressBar.setVisibility(View.GONE);
                            Toast.makeText(Main_WriteContent.this, "?????? ??????", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            Intent intent = new Intent(Main_WriteContent.this, Showtext.class);
                            intent.putExtra("text", write_text.getText().toString());
                            write_text.setText("");
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
        }

    }

    public Boolean checkcheckbox() {
        checkBox = findViewById(R.id.checkBox);

        return checkBox.isChecked();
    }

    public void updateword(){
        int check = 0;
        ArrayList line = new ArrayList();
        String[] temp = {"??????","?????????","?????????","?????????","?????????"
                ,"?????????","?????????","??????","??????","?????????","????????????"
                ,"?????????","??????","?????????","??????","?????????","??????","????????????"
                ,"??????","??????","??????","?????????","????????????","??????","?????? ??????"
                ,"?????????","??????","???","??????","??????","???????????????","??????"
                ,"??????","??????","??????","???","??????","?????????","??????","??????"
                ,"??????","?????????","??????","??????","??????","??????","???","???????????????"
                ,"?????????","??????","SNS","??????","???","???","??????","?????????"
                ,"??????","????????????","??????","??????","??????","?????????","????????????"
                ,"??????","??????","??????","??????","??????","?????????","??????","?????????"
                ,"??????","??????","??????","??????","??????","??????","?????????","?????????"
                ,"??????","??????","??????","??????","??????","??????","??????","??????","?????????"
                ,"?????????","????????? ","?????????","??????","???","??????","?????????","??????"
                ,"???","???","??????","??????","??????","??????"};
        for(int i=0;i<temp.length;i++) {
            line.add(i,temp[i]);
        }

        while(check < line.size()){
            Map<String, Object> word = new HashMap<>();
            String qq = settingdate();
            word.put(Date_KEY, qq);
            word.put(Used_KEY, false);
            word.put(Word_KEY, line.get(check));
            db.collection("word").document(qq)
                    .set(word)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
            Log.d(TAG, "pass End");
            check++;
        }

    }
    public void practice(){
        db.collection("user")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Toast.makeText(Main_WriteContent.this, document.getId().toString() + document.getData().toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void checkwordused(){
        //?????? ????????????
        String date = today.format(new Date());

        DocumentReference WordRef = db.collection("word").document(date);

        WordRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    word = documentSnapshot.getString(Word_KEY);
                    main_word.setText(word);
                }
            }
        });

        //????????? ?????? used => true??? ?????????
        WordRef.update(Used_KEY, true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() { 
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

    }

    public String settingdate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2021 , Calendar.FEBRUARY , 13);  // 2021??? 2??? 11???
        cal.add(Calendar.DAY_OF_MONTH , count++); // 2021??? 2??? 11???
        if((cal.get(Calendar.MONTH)+1) < 10 && cal.get(Calendar.DAY_OF_MONTH) < 10){
            date = cal.get(Calendar.YEAR) + "0" + (cal.get(Calendar.MONTH) + 1) + "0" + cal.get(Calendar.DAY_OF_MONTH);
        }
        else if((cal.get(Calendar.MONTH)+1) < 10){
            date = cal.get(Calendar.YEAR) + "0" + (cal.get(Calendar.MONTH) + 1) + "" + cal.get(Calendar.DAY_OF_MONTH);
        }
        else if(cal.get(Calendar.DAY_OF_MONTH) < 10){
            date = cal.get(Calendar.YEAR) + "" + (cal.get(Calendar.MONTH) + 1) + "0" + cal.get(Calendar.DAY_OF_MONTH);
        }
        return date;
    }

}