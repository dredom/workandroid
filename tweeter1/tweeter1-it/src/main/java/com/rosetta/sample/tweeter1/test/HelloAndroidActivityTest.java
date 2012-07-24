package com.rosetta.sample.tweeter1.test;

import android.test.ActivityInstrumentationTestCase2;
import com.rosetta.sample.tweeter1.*;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<TwitteringListActivity> {

    public HelloAndroidActivityTest() {
        super("com.rosetta.sample.tweeter1", TwitteringListActivity.class);
    }

    public void testActivity() {
        TwitteringListActivity activity = getActivity();
        assertNotNull(activity);
    }
}

