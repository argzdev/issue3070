package com.argz.issue3070;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SnapshotMetadata;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /***
     * Each device/emulator has it's own limit when it comes to hitting RESOUCE_EXHAUSTED limit when querying.
     * Upon testing, the RESOUCE_EXHAUSTED is encountered when retrieving around 30 items (300MB) worth of data on an emulator.
     * Test varies between devices, testing on physical device reach up to 40 items (400MB) worth of data before hitting RESOUCE_EXHAUSTED.
     */
    public void queryDb(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> docRef = db.collection("users")
            .limit(40)
            .get()
            .addOnCompleteListener(task -> {
                if(!task.isSuccessful()) {
                    Log.d(TAG, "queryDb: " + task.getException().getMessage());
                    return;
                }

                // Just output total byte size = total size of list + extra zero
                Log.d(TAG, "Total data size: " + task.getResult().size() + "0MB");
            });
    }

    /***
     * Insert 10 users in Firestore Database.
     * Each user has 10 MB of data computed via https://firebase.google.com/docs/firestore/storage-size
     * A total of 100 MB is inserted into the Database
     *
     * The loop will insert 1 MB to the Db for 10 times -> 100 MB
     */
    public void addDataToDb(View view) {
        for(int i = 0; i < 10; i++) {
            try {
                // Add a delay so the stream wont hit resource exhausted on write.
                Thread.sleep(2000);
            } catch (InterruptedException e) {}

            FirebaseFirestore.getInstance().collection("user").add(create1MBData()).addOnCompleteListener(task -> {});
        }
    }

    /***
     * Creates 1 MB worth of data
     * A string contains 10 KB generated via https://www.lipsum.com/
     * The string is amplified using HashMap to form 1 MB
     */
    private Map<String, Object> create1MBData(){
        Map<String, Object> one_hundred_kilobyte = new HashMap<>();

        for(int i = 0; i < 10; i++)
            one_hundred_kilobyte.put("ten_kilobytes_" + i, getString(R.string.ten_kilobytes));

        Map<String, Object> one_megabyte = new HashMap<>();
        for(int i = 0; i < 10; i++)
            one_megabyte.put("megabyte_" + i, one_hundred_kilobyte);

        return one_megabyte;
    }
}