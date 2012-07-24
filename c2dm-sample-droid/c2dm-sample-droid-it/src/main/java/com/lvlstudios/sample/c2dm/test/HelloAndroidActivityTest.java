package com.lvlstudios.sample.c2dm.test;

import android.test.ActivityInstrumentationTestCase2;

import com.lvlstudios.sample.c2dm.*;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<SampleC2dmActivity> {

    public HelloAndroidActivityTest() {
        super("com.lvlstudios.sample.c2dm", SampleC2dmActivity.class);
    }

    public void testActivity() {
        SampleC2dmActivity activity = getActivity();
        assertNotNull(activity);
    }
}

