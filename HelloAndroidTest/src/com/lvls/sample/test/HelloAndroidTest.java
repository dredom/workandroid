package com.lvls.sample.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.lvls.sample.HelloAndroidActivity;

public class HelloAndroidTest extends
        ActivityInstrumentationTestCase2<HelloAndroidActivity> {

    public HelloAndroidTest() {
        super("com.lvls.sample", HelloAndroidActivity.class);
    }

    private HelloAndroidActivity mActivity;
    private TextView mView;
    private String resourceString;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
        mView = (TextView) mActivity.findViewById(com.lvls.sample.R.id.textview);
        resourceString = mActivity.getString(com.lvls.sample.R.string.hello);
    }

    public void testPreconditions() {
        assertNotNull(mView);
    }

    /**
     * Compares the expected value, read directly from the hellostring resource,
     * to the text displayed by the TextView, obtained from the TextView's getText() method
     */
    public void testText() {
        assertEquals(resourceString, (String) mView.getText());
    }
}
