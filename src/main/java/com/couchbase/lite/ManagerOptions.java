package com.couchbase.lite;

import com.couchbase.lite.auth.AuthorizerFactory;
import com.couchbase.lite.auth.AuthorizerFactoryManager; // https://github.com/couchbase/couchbase-lite-java-core/issues/41
import com.couchbase.lite.auth.BuiltInAuthorizerFactory;

import java.util.ArrayList;

/**
 * Option flags for Manager initialization.
 */
public class ManagerOptions {

    /**
     *  No modifications to databases are allowed.
     */
    private boolean readOnly;

    private AuthorizerFactoryManager authorizerFactoryManager; // https://github.com/couchbase/couchbase-lite-java-core/issues/41

    // https://github.com/couchbase/couchbase-lite-java-core/issues/41
    public ManagerOptions() {
        this(new AuthorizerFactoryManager(new ArrayList<AuthorizerFactory>() {{ add(new BuiltInAuthorizerFactory()); }}));
    }

    // https://github.com/couchbase/couchbase-lite-java-core/issues/41
    public ManagerOptions(AuthorizerFactoryManager authorizerFactoryManager) {
        this.authorizerFactoryManager = authorizerFactoryManager;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    // https://github.com/couchbase/couchbase-lite-java-core/issues/41
    public AuthorizerFactoryManager getAuthorizerFactoryManager() { return authorizerFactoryManager; }

    // https://github.com/couchbase/couchbase-lite-java-core/issues/41
    public void setAuthorizerFactoryManager(AuthorizerFactoryManager authorizerFactoryManager) { this.authorizerFactoryManager = authorizerFactoryManager; }
}
