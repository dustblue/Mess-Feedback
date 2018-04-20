package com.madhan.mess;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    FirebaseFirestore mDB;
    List<User> userList;
    BarChart barChart;
    TextView reviews;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        userList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();

        mDB.collection("feedback")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap<String, Double> ratings
                                        = (HashMap<String, Double>) document.getData().get("ratings");
                                String remarks = "";
                                if (document.getData().get("remarks") != null) {
                                    remarks = document.getData().get("remarks").toString();
                                }
                                User user = new User();
                                user.setRatings(ratings);
                                user.setRemarks(remarks);

                                userList.add(user);
                            }
                            setUpChart();
                        } else {
                            Toast.makeText(AdminActivity.this, "Error getting documents:"
                                    + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        barChart = findViewById(R.id.bar_chart);
        reviews = findViewById(R.id.reviews);

        barChart.setDrawBarShadow(true);
        barChart.setDrawValueAboveBar(true);

    }

    void setUpChart() {
        reviews.append(getAllReviews());

        IAxisValueFormatter xAxisFormatter = new IAxisValueFormatter() {
            String[] labels = new String[]{
                    "Quality", "Quantity", "Punctuality", "Behaviour", "Hygiene"
            };

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                try {
                    int index = (int) value;
                    return labels[index];
                } catch (Exception e) {
                    return "";
                }
            }
        };

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setLabelCount(5);
        xAxis.setValueFormatter(xAxisFormatter);

        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setLabelCount(5, false);
//        yAxis.setValueFormatter(yAxisFormatter);
        yAxis.setSpaceTop(15f);
        yAxis.setAxisMinimum(0f);

        BarData barData = new BarData(getDataSet());
        barChart.getDescription().setEnabled(false);
        barChart.setData(barData);
        barChart.animateXY(2000, 2000);


    }

    private BarDataSet getDataSet() {

        float[] ratings = getRatings();

        ArrayList<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0, ratings[0]));
        entries.add(new BarEntry(1, ratings[1]));
        entries.add(new BarEntry(2, ratings[2]));
        entries.add(new BarEntry(3, ratings[3]));
        entries.add(new BarEntry(4, ratings[4]));

        BarDataSet set = new BarDataSet(entries, "Feedback");
        set.setColors(ColorTemplate.MATERIAL_COLORS);

        return set;
    }

    private float[] getRatings() {

        float[] ratings = new float[]{
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f
        };
        int n = userList.size();
        for (User user : userList) {
            ratings[0] += (user.getRatings().get("q1") / n);
            ratings[1] += (user.getRatings().get("q2") / n);
            ratings[2] += (user.getRatings().get("q3") / n);
            ratings[3] += (user.getRatings().get("q4") / n);
            ratings[4] += (user.getRatings().get("q5") / n);
        }

        return ratings;
    }

    private String getAllReviews() {
        StringBuilder allReviews = new StringBuilder("\n\n");

        for (User user : userList) {
            if (!user.getRemarks().trim().equals(""))
                allReviews.append(user.getRemarks()).append("\n");
        }

        return allReviews.toString();
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

    @Override
    public void onBackPressed() {

    }
}
