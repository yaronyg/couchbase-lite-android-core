package com.couchbase.lite;

import com.couchbase.lite.auth.AuthorizerFactory;
import com.couchbase.lite.auth.AuthorizerFactoryManager;
import com.couchbase.lite.auth.BuiltInAuthorizerFactory;

import java.util.ArrayList;

public class ManagerOptions {

    /**
     *  No modifications to databases are allowed.
     */
    private boolean readOnly;

    /**
     * Persistent replications will not run (until/unless startPersistentReplications is called.)
     */
    private boolean noReplicator;

    private AuthorizerFactoryManager authorizerFactoryManager;

    public ManagerOptions(boolean readOnly, boolean noReplicator) {
        this(readOnly, noReplicator, new AuthorizerFactoryManager(new ArrayList<AuthorizerFactory>() {{ add(new BuiltInAuthorizerFactory()); }}));
    }

    public ManagerOptions(boolean readOnly, boolean noReplicator, AuthorizerFactoryManager authorizerFactoryManager) {
        this.readOnly = readOnly;
        this.noReplicator = noReplicator;
        this.authorizerFactoryManager = authorizerFactoryManager;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isNoReplicator() {
        return noReplicator;
    }

    public void setNoReplicator(boolean noReplicator) {
        this.noReplicator = noReplicator;
    }

    public AuthorizerFactoryManager getAuthorizerFactoryManager() { return authorizerFactoryManager; }

    public void setAuthorizerFactoryManager(AuthorizerFactoryManager authorizerFactoryManager) { this.authorizerFactoryManager = authorizerFactoryManager; }
}
