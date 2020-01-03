package com.example.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;

import java.util.Arrays;
import java.util.List;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    public BottomNavigationView bottomNavigationView;
    public FrameLayout mMainFrame;

    public static HomeFragment homeFragment;
    private DoctorSelectFragment doctorSelectFragment;
    private AccountFragment accountFragment;


    private static final int MY_REQUEST_CODE = 1781;
    List<AuthUI.IdpConfig> providers;

    public static String userID;
    //Get user
    public static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        mMainFrame = findViewById(R.id.main_frame);

        homeFragment = new HomeFragment();
        doctorSelectFragment = new DoctorSelectFragment();
        accountFragment = new AccountFragment();

        setFragment(homeFragment);

        if(user!=null){
            userID=user.getUid();
        }



        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.home:
                        Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                        setFragment(homeFragment);
                        break;

                    case R.id.orders:
                        Toast.makeText(MainActivity.this, "Doctor Select", Toast.LENGTH_SHORT).show();
                        setFragment(doctorSelectFragment);
                        break;

                    case R.id.account:
                        if(user == null){
                            signIn();
                        }else{
                            if(user.getEmail() == null){
                                Toast.makeText(MainActivity.this, ""+user.getPhoneNumber(), Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MainActivity.this, "" + user.getEmail(), Toast.LENGTH_SHORT).show();
                            }
                            setFragment(accountFragment);
                        }
                        break;

                    default:
                        return false;

                }

                return true;
            }
        });


    }

    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    public void signIn() {
        //Init providers
        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        showSignInOptions();
    }

    private void showSignInOptions() {
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.loggin)
                .setPhoneButtonId(R.id.phone_button)
                .setGoogleButtonId(R.id.google_button)
                // ...
                //.setTosAndPrivacyPolicyId(R.id.privacyTextView7)
                .build();

        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAuthMethodPickerLayout(customLayout)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .setTheme(R.style.MyTheme)
                        .build(),MY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) ;
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                //Get user
                user = FirebaseAuth.getInstance().getCurrentUser();
                userID=user.getUid();
                //Show user email on Toast
                if(user.getEmail() == null){
                    Toast.makeText(this, ""+user.getPhoneNumber(), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "" + user.getEmail(), Toast.LENGTH_SHORT).show();
                }
            } else {
                //
            }
        }
    }


}