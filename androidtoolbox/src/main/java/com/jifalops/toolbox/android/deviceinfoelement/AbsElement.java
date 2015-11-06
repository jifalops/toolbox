package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;
import android.os.Build;


public abstract class AbsElement {
//	private static final String LOG_TAG = AbsElement.class.getSimpleName();

    protected static final int API = Build.VERSION.SDK_INT;

	protected Context mContext;
	
	public AbsElement(Context context) {
		mContext = context;
	}
}
