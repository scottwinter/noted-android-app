package com.fourheronsstudios.noted.utils;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Scott on 9/19/2017.
 */

public class DatabaseUtil {

    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }

        return mDatabase;
    }
}
