package com.jifalops.toolbox.android.nsd;

import android.net.nsd.NsdServiceInfo;

/**
 * Created by jake on 8/6/15.
 */
public interface NsdServiceFilter {
    boolean isAcceptableService(NsdServiceInfo info);
}
