package com.couchbase.lite.auth;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.ReplicatorArguments;

import java.util.Map;

/**
 * Certain Authorizers come with CouchBase Lite, this class provides an AuthorizerFactory that can handle requests for
 * any of them.
 */
public class BuiltInAuthorizerFactory implements AuthorizerFactory {
    protected static final String facebook = "facebook";
    protected static final String persona = "persona";
    protected static final String email = "email";

    @Override
    public Authorizer getAuthorizer(ReplicatorArguments replicatorArguments) throws CouchbaseLiteException {
        assert replicatorArguments != null;

        Map<String, Object> authMap = replicatorArguments.getPush() ? replicatorArguments.getTargetAuth() : replicatorArguments.getSourceAuth();

        Boolean containsFacebook = authMap.containsKey(facebook);
        Boolean containsPersona = authMap.containsKey(persona);

        if (containsFacebook == false && containsPersona == false) {
            // We don't have logic to handle this request
            return null;
        }

        if (authMap.size() != 1 || (containsFacebook && containsPersona)) {
            throw new CouchbaseLiteException("Auth field value contains extraneous elements, " + facebook + " & " + persona + " are to be used alone", 400);
        }

        String email = getEmail(containsFacebook ? authMap.get(facebook) : authMap.get(persona));

        if (email == null) {
            throw new CouchbaseLiteException("There must be a child field of " + facebook + " or " + persona + " called " + email, 400);
        }

        if (containsFacebook) {
            return new FacebookAuthorizer(email);
        }

        if (containsPersona) {
            return new PersonaAuthorizer(email);
        }

        throw new RuntimeException("This shouldn't even be theoretically possible.");
    }

    protected String getEmail(Object childOfFacebookOrPersona) {
        if (childOfFacebookOrPersona == null) {
            return null;
        }

        try {
            return (String)(((Map<String, Object>) childOfFacebookOrPersona).get(email));
        } catch (ClassCastException e) {
            return null;
        }
    }
}
