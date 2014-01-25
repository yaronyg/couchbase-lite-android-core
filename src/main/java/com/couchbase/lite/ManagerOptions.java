package com.couchbase.lite;

import com.couchbase.lite.auth.AuthorizerFactoryManager;

/**
 * Option flags for Manager initialization.
 */
public class ManagerOptions {

    /**
     *  No modifications to databases are allowed.
     */
    private boolean readOnly;

    private AuthorizerFactoryManager authorizerFactoryManager;

    public ManagerOptions() {
		this(null);
    }

    public ManagerOptions(AuthorizerFactoryManager authorizerFactoryManager) {
        this.authorizerFactoryManager = authorizerFactoryManager;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public AuthorizerFactoryManager getAuthorizerFactoryManager() { return authorizerFactoryManager; }

    public void setAuthorizerFactoryManager(AuthorizerFactoryManager authorizerFactoryManager) { this.authorizerFactoryManager = authorizerFactoryManager; }
}
