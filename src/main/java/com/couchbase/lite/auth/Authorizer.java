package com.couchbase.lite.auth;

import com.couchbase.lite.support.HttpClientFactory;

import java.net.URL;
import java.util.Map;

/**
 * Authorizers should extend from this class
 *
 * @exclude
 */
public class Authorizer extends AuthenticatorImpl {

    public boolean usesCookieBasedLogin() {
        return false;
    }

    public Map<String, String> loginParametersForSite(URL site) {
        return null;
    }

    public String loginPathForSite(URL site) {
        return null;
    }

    /**
     * If the client needs a custom HttpClientFactory (to handle TLS for example) then it can return it here, otherwise
     * it will return null.
     * https://github.com/couchbase/couchbase-lite-java-core/issues/41
     * @return
     */
    public HttpClientFactory getHttpClientFactory() {
		return null;
	}

    /**
     * If the authorizer will be handling the replication in another way, it may be worth overriding this method.
     */
    public boolean isWorkNeeded() { return true; }
}