package com.couchbase.lite.auth;

import com.couchbase.lite.support.HttpClientFactory;

import java.net.URL;
import java.util.Map;

/**
 * I turned this from a class to an interface because it more accurately described its behavior.
 */
public interface Authorizer {
    public boolean usesCookieBasedLogin();

    public Map<String, String> loginParametersForSite(URL site);

    public String loginPathForSite(URL site);

    /**
     * If the client needs a custom HttpClientFactory (to handle TLS for example) then it can return it here, otherwise
     * it will return null.
     * https://github.com/couchbase/couchbase-lite-java-core/issues/41
     * @return
     */
    public HttpClientFactory getHttpClientFactory();
}