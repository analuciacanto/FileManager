package com.urmobo.filemanager.activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;

import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.urmobo.filemanager.ModelFile;
import com.urmobo.filemanager.R;
import com.urmobo.filemanager.fragments.InternalStorageFragment;
import com.urmobo.filemanager.fragments.SDCardFragment;

import java.util.ArrayList;

public class MainActivity extends  AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ArrayList<ModelFile> filesToPaste;
    private ArrayList<ModelFile> filesToMove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        filesToPaste = new ArrayList<>();
        filesToMove = new ArrayList<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open_Drawer, R.string.Close_Drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new InternalStorageFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_internal);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_internal:
                InternalStorageFragment internalStorageFragment = new InternalStorageFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, internalStorageFragment).addToBackStack(null).commit();
                break;
            case R.id.nav_card:
                 SDCardFragment sdCardFragment = new SDCardFragment();
                 getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, sdCardFragment).addToBackStack(null).commit();
                 break;
            case R.id.nav_about:
                Toast.makeText(this, R.string.about, Toast.LENGTH_SHORT).show();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager().popBackStackImmediate();

        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public ArrayList<ModelFile> getFilesToPaste() {
        return filesToPaste;
    }

    public void setFilesToPaste(ArrayList<ModelFile> filesToPaste) {
        this.filesToPaste = filesToPaste;
    }

    public ArrayList<ModelFile> getFilesToMove() {
        return filesToMove;
    }

    public void setFilesToMove(ArrayList<ModelFile> filesToMove) {
        this.filesToMove = filesToMove;
    }
}