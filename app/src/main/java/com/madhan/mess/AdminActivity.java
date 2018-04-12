package com.madhan.mess;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mAuth = FirebaseAuth.getInstance();

        RadarChart radarChart = findViewById(R.id.radar_chart);
        TextView reviews = findViewById(R.id.reviews);

        ArrayList<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(1f, "Quality"));
        entries.add(new RadarEntry(2f, "Quantity"));
        entries.add(new RadarEntry(3f, "Punctuality"));
        entries.add(new RadarEntry(4f, "Behaviour"));
        entries.add(new RadarEntry(5f, "Cleanliness"));

        RadarDataSet dataSet = new RadarDataSet(entries, "Feedback");

        RadarData radarData = new RadarData(dataSet);
        radarChart.setData(radarData);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                mAuth.signOut();
                finish();
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
