package com.couchbase.lite.auth;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.ReplicatorArguments;

/**
 * https://github.com/couchbase/couchbase-lite-java-core/issues/41
 */
public interface AuthorizerFactory {
    /**
     * Examines authMap (which encodes the contents of the auth field in a replication request). If it determines
     * that the authMap doesn't encode arguments for an Authorizer supported by the factory then NULL MUST be
     * returned. If the arguments are supported by an Authorizer in the factory then either the Authorizer MUST
     * be created and returned or a couchbaseLiteException MUST be thrown so a proper error can be returned to the
     * caller.
     * @param replicatorArguments
     * @return
     */
    public Authorizer getAuthorizer(ReplicatorArguments replicatorArguments) throws CouchbaseLiteException;
}
