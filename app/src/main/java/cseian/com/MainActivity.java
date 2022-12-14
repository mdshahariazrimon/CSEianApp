package cseian.com;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH =3500;

    Animation topAnimation,bottomAnimation;
    ImageView logo;
    TextView title,tagline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        topAnimation= AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnimation= AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        logo=findViewById(R.id.logo);
        title=findViewById(R.id.title);
        tagline=findViewById(R.id.tagline);

        logo.setAnimation(topAnimation);
        title.setAnimation(bottomAnimation);
        tagline.setAnimation(bottomAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent= new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        },SPLASH);
    }

}