/**
 *
 */
package com.lvls.sample.test;

import com.lvls.sample.Hell2Activity;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

/**
 * @author auntiedt
 *
 */
public class Hell2Test extends ActivityInstrumentationTestCase2<Hell2Activity> {

    private Hell2Activity mActivity;
    private TextView mView;
    private String resourceString;

    public Hell2Test() {
        super("com.lvls.sample", Hell2Activity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
//        mView = (TextView) mActivity.findViewById(com.lvls.sample.R.id.textview);
        resourceString = mActivity.getString(com.lvls.sample.R.string.hello);
    }

}
