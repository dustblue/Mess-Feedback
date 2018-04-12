package com.madhan.mess;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText remarks;
    private RatingBar ratingBars[];
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private Button submit;
    private ScrollView scrollView;
    FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    //continue
                } else {
                    finish();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        };

        submit = (Button) findViewById(R.id.submit);
        remarks = (EditText) findViewById(R.id.editText);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        ratingBars = new RatingBar[]{
                (RatingBar) findViewById(R.id.q1_rating), (RatingBar) findViewById(R.id.q2_rating),
                (RatingBar) findViewById(R.id.q3_rating), (RatingBar) findViewById(R.id.q4_rating),
                (RatingBar) findViewById(R.id.q5_rating)
        };

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidated()) {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Sending Feedback to Server...");
                    progressDialog.show();

                    sendToFirebase();
                }
            }
        });
    }

    private void sendToFirebase() {
        Map<String, Object> feedback = new HashMap<>();
        Map<String, Object> ratings = new HashMap<>();
        for (int i=0; i<5; i++) {
            ratings.put("q"+(i+1), ratingBars[i].getRating());
        }
        feedback.put("ratings", ratings);
        if(!TextUtils.isEmpty(remarks.getText())) {
            feedback.put("remarks", remarks.getText().toString());
        }

        // Add a new document with a generated ID
        mDB.collection("feedback").document(mAuth.getCurrentUser().getEmail())
                .set(feedback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        displayDialog("Added Successfully\nThank You", -1);
                        //Move to Main menu
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Error adding document" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidated() {
        if (ratingBars[0].getRating() != 0) {
            if (ratingBars[1].getRating() != 0) {
                if (ratingBars[2].getRating() != 0) {
                    if (ratingBars[3].getRating() != 0) {
                        if (ratingBars[4].getRating() != 0) {
                            return true;
                        } else {
                            displayDialog("Please rate all questions", 5);
                            return false;
                        }
                    } else {
                        displayDialog("Please rate all questions", 4);
                        return false;
                    }
                } else {
                    displayDialog("Please rate all questions", 3);
                    return false;
                }
            } else {
                displayDialog("Please rate all questions", 2);
                return false;
            }
        } else {
            displayDialog("Please rate all questions", 1);
            return false;
        }
    }

    private void resetRatings(ViewGroup group) {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof RatingBar) {
                ((RatingBar) view).setRating(0);
            }

            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                resetRatings((ViewGroup) view);
        }
    }

    private void displayDialog(String text, final int id) {
        if (alertDialog != null) {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
        alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle("Mess Feedback");
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (id == -1) {
                            mAuth.signOut();
                            finish();
                        } else if (id == -2) {
                            resetRatings(scrollView);
                        } else {
//                            scrollView.post(new Runnable() {
//                                public void run() {
//                                    scrollView.scrollTo(id, scrollView.getBottom());
//                                }
//                            });
                        }
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                displayDialog("Do you want to logout?", -1);
                break;
            case R.id.action_reset:
                displayDialog("Reset all ratings?", -2);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
