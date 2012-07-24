/**
 *
 */
package com.lvls.sample.service;

import android.os.Binder;

/**
 * @author auntiedt
 *
 */
public class MyBinder extends Binder {

    private MyService myService;

    public MyService getMyService() {
        return myService;
    }

    public void setMyService(MyService myService) {
        this.myService = myService;
    }

}
