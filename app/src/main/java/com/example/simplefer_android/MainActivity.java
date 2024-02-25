package com.example.simplefer_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.simplefer_android.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MainActivity extends AppCompatActivity
{
    private com.example.simplefer_android.databinding.ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private boolean isPieMenuOpen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.pieMenu.setOnClickListener(
            view -> {
                if (isPieMenuOpen)
                {
                    closePieMenu();
                } else
                {
                    openPieMenu();
                }
            }
        );
        binding.cameraButton.setOnClickListener(
            view -> {
                try {
                    Intent intent = new Intent(this, CameraPreviewActivity.class);
                    startActivity(intent);
                }
                catch (Error e){
                    e.printStackTrace();
                }
            }
        );

    }


    private void openPieMenu()
    {
    // 展开动画
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.show_pie_menu);
        binding.pieMenuChildren.startAnimation(animation);
        binding.pieMenuChildren.setVisibility(View.VISIBLE);

        isPieMenuOpen = true;
    }

    private void closePieMenu() {
        // 收缩动画
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.hide_pie_menu);
        binding.pieMenuChildren.startAnimation(animation);
        binding.pieMenuChildren.setVisibility(View.GONE);

        isPieMenuOpen = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
