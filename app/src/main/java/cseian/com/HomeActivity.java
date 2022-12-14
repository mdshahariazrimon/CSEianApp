package cseian.com;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cseian.com.Adapter.PostAdapter;
import cseian.com.Model.Post;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer_layout;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    private RecyclerView recyclerView;
    private ProgressBar progress_circular;

    private PostAdapter postAdapter;
    private List<Post>postList;

    private CircleImageView navHeaderImage;
    private TextView navHeaderEmail, navHeaderName;

    private Button logOut;

    private DatabaseReference userRef;

    private SwitchCompat switch_btn;


    private SharedPreferences sharedPreferences=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        fab= findViewById(R.id.fab);
        drawer_layout=findViewById(R.id.drawer_layout);
        toolbar=findViewById(R.id.home_toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("CSEian App");

        progress_circular=findViewById(R.id.progress_circular);

        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        NavigationView navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        /*
        //Code To Change Theme
        switch_btn = findViewById(R.id.switchBtn);

        switch_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                else{
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
            }
        });
         */
        switch_btn = findViewById(R.id.switchBtn);
        sharedPreferences=getSharedPreferences("night",0);
        Boolean booleanValue=sharedPreferences.getBoolean("night_mode",true);
        if (booleanValue){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            switch_btn.setChecked(true);
        }
        else if (booleanValue){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            switch_btn.setChecked(false);
        }
        switch_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    switch_btn.setChecked(true);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putBoolean("night_mode",true);
                    editor.commit();
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    switch_btn.setChecked(false);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putBoolean("night_mode",false);
                    editor.commit();
                }
            }
        });

        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(
                this,drawer_layout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeActivity.this,AskAQuestionActivity.class);
                startActivity(intent);
            }
        });


        navHeaderEmail=navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);
        navHeaderName=navigationView.getHeaderView(0).findViewById(R.id.nav_header_username);
        navHeaderImage=navigationView.getHeaderView(0).findViewById(R.id.nav_header_profile_image);
        logOut=navigationView.getHeaderView(0).findViewById(R.id.logoutBtn);
        userRef=FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                navHeaderName.setText(snapshot.child("username").getValue().toString());
                navHeaderEmail.setText(snapshot.child("email").getValue().toString());
                Glide.with(HomeActivity.this).load(snapshot.child("profileimageurl").getValue()).dontAnimate().into(navHeaderImage);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        });

        postList =new ArrayList<>();
        postAdapter=new PostAdapter(HomeActivity.this,postList);
        recyclerView.setAdapter(postAdapter);

        readQuestionsPost();
    }



    private void readQuestionsPost() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("question posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Post post=dataSnapshot.getValue(Post.class);
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
                progress_circular.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this,error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        item.setChecked(true);
        switch(item.getItemId()){

            case R.id.home:
                Intent homeintent= new Intent(HomeActivity.this,HomeActivity.class);
                homeintent.putExtra("title","finance");
                startActivity(homeintent);
                break;

            case R.id.nav_finance:
                Intent intent= new Intent(HomeActivity.this,CategorySelectedActivity.class);
                intent.putExtra("title","finance");
                startActivity(intent);
                break;

            case R.id.nav_sports:
                Intent intentS= new Intent(HomeActivity.this,CategorySelectedActivity.class);
                intentS.putExtra("title","sports");
                startActivity(intentS);
                break;
            case R.id.nav_food:
                Intent intentF= new Intent(HomeActivity.this,CategorySelectedActivity.class);
                intentF.putExtra("title","food");
                startActivity(intentF);
                break;
            case R.id.blogview:
                Intent intentcseian= new Intent(HomeActivity.this,CSEianBlog.class);
                startActivity(intentcseian);
                break;
            case R.id.about:
                replacefragment(new BlogView());break;
            case R.id.calculator:
                Intent intentcalculator= new Intent(HomeActivity.this, shortnote.class);
                startActivity(intentcalculator);

        }
        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
        super.onBackPressed();
    }

    private void replacefragment(Fragment fragment){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }
}