package com.couchbase.lite.auth;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.ReplicatorArguments;
import com.couchbase.lite.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Walks through the authorizerFactories it has been given in the order they were given trying to find the first
 * one that matches the request params.
 * https://github.com/couchbase/couchbase-lite-java-core/issues/41
 */
public class AuthorizerFactoryManager {
    ArrayList<AuthorizerFactory> authorizers = new ArrayList<AuthorizerFactory>();

    public AuthorizerFactoryManager(List<AuthorizerFactory> authorizerFactories) {
        authorizers.addAll(authorizerFactories);
    }

    /**
     * The code checks to see if an authorizer is needed for the request
     * @param replicatorArguments
     * @return null if there is no authorizer needed otherwise the appropriate authorizer, if any
     * @throws CouchbaseLiteException
     */
    public Authorizer findAuthorizer(ReplicatorArguments replicatorArguments) throws CouchbaseLiteException {
        for(AuthorizerFactory authorizerFactory : authorizers) {
            Authorizer authorizer = authorizerFactory.getAuthorizer(replicatorArguments);
            if (authorizer != null) {
                return authorizer;
            }
        }

        return null;
    }
}
