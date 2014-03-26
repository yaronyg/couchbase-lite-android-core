package com.couchbase.lite.router;

import com.couchbase.lite.Manager;

// https://github.com/couchbase/couchbase-lite-java-core/issues/44
public interface RequestAuthorization {
    /**
     * Called by Router it determines if a request is allowed to proceed or not. If false is
     * returned then it is assumed that the Authorize code has set up the urlConnection object
     * with the proper error code, headers, response body (if any), etc. Router's only
     * responsibility if it detects a 'false' is to send the connection immediately and exit.
     *
     * Note that the code can also call setPrincipal on urlConnection if it wishes to change the principal
     * based on available data.
     * @param manager
     * @param urlConnection
     * @return
     */
    public boolean Authorize(Manager manager, URLConnection urlConnection);
}
