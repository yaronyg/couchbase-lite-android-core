package com.couchbase.lite.auth;

import com.couchbase.lite.support.HttpClientFactory;

import java.net.URL;
import java.util.Map;

public interface Authorizer {
    public boolean usesCookieBasedLogin();

    public Map<String, String> loginParametersForSite(URL site);

    public String loginPathForSite(URL site);

    /**
     * If the client needs a custom HttpClientFactory (to handle TLS for example) then it can return it here, otherwise
     * it will return null.
     * @return
     */
    public HttpClientFactory getHttpClientFactory();
}