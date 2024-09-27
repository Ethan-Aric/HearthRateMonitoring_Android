package com.lmx.heartbeatratemonitor;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.clj.fastble.BleManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity  {

    public static final int ACTION_HEART_RATE = R.id.action_heart_rate;
    public static final int ACTION_HISTORY = R.id.action_history;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContainer = findViewById(R.id.main_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupBottomNavigationView();
        loadFragment(new HeartRateFragment());
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId()== ACTION_HEART_RATE) {
                loadFragment(new HeartRateFragment());
                return true;

            } else if (item.getItemId()==ACTION_HISTORY) {
                loadFragment(new HistoryDataFragment());
                return true;
            }


//                case R.id.action_user:
//                    loadFragment(new UserManagementFragment());
//                    return true;
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 获取 HeartRateFragment 的实例
        HeartRateFragment fragment = (HeartRateFragment) getSupportFragmentManager().findFragmentByTag("heartRateFragment");

        if (fragment != null) {
            // 调用 BleManager.disconnect() 方法
            BleManager.getInstance().disconnect(fragment.BleDevice);
        }
    }




}