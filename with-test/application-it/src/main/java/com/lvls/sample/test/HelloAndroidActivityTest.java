package com.lvls.sample.test;

import android.test.ActivityInstrumentationTestCase2;
import com.lvls.sample.*;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<HelloAndroidActivity> {

    public HelloAndroidActivityTest() {
        super(HelloAndroidActivity.class);
    }

    public void testActivity() {
        HelloAndroidActivity activity = getActivity();
        assertNotNull(activity);
    }
}

