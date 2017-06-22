package com.example.harald.runwithme2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Button btnNext;
    private Model model;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;

            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            this.model = new Model();

            mTextMessage = (TextView) findViewById(R.id.message);
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            btnNext = (Button) findViewById(R.id.buttonNext);
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.startMapsActivity(Model.ACTION.SELECT_STARTPOINT);
                }
            });
        }
        catch (Exception ex)
        {
            this.showMessage(ex.toString());
        }
    }

    private void startMapsActivity(Model.ACTION action) {

        try
        {
            this.model.setCurrentAction(action);
            Intent menuIntent = new Intent(this, MapsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("model", model);
            menuIntent.putExtras(bundle);
            startActivity(menuIntent);
        }
        catch (Exception ex)
        {
           this.showMessage(ex.toString());
        }
    }

    private void showMessage(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

}
