package com.lvlstudios.android.gtmessage.test;

import android.test.ActivityInstrumentationTestCase2;
import com.lvlstudios.android.gtmessage.*;

public class HelpActivityTest extends ActivityInstrumentationTestCase2<HelpActivity> {

    public HelpActivityTest() {
        super("com.lvlstudios.android.gtmessage", HelpActivity.class);
    }

    public void testActivity() {
        HelpActivity activity = getActivity();
        assertNotNull(activity);
    }
}

