package com.couchbase.lite;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses the arguments in a replication request so they can be seen and processed identically by both the
 * functional and security code.
 */
public class ReplicatorArguments {
    public static final String sourceFieldName = "source";
    public static final String targetFieldName = "target";
    public static final String urlFieldName = "url";
    public static final String authFieldName = "auth";
    public static final String queryParamsFieldName = "query_params";
    public static final String createTargetFieldName = "create_target";
    public static final String continuousFieldName = "continuous";
    public static final String cancelFieldName = "cancel";
    public static final String filterFieldName = "filter";

    private final Map<String, Object> rawProperties;
    private final Map<String, Object> queryParams;
    private final Map<String, Object> headers;
    private String source;
    private String target;
    private final Map<String, Object> sourceAuth;
    private final Map<String, Object> targetAuth;
    private final boolean createTarget;
    private final boolean continuous;
    private final boolean cancel;
    private final boolean push;
    private final String filter;
    private final Principal principal;

    public ReplicatorArguments(Map<String, Object> properties, Manager manager, Principal principal) throws CouchbaseLiteException {
        rawProperties = properties;
        queryParams = (Map<String,Object>)properties.get(queryParamsFieldName);
        Map<String, Object> sourceMap = parseSourceOrTarget(properties, sourceFieldName);
        Map<String, Object> targetMap = parseSourceOrTarget(properties, targetFieldName);
        headers = (Map<String, Object>)properties.get("headers");
        sourceAuth = (Map<String, Object>) sourceMap.get(authFieldName);
        targetAuth = (Map<String, Object>) targetMap.get(authFieldName);

        source = (String)sourceMap.get(urlFieldName);
        target = (String)targetMap.get(urlFieldName);

        Boolean createTargetBoolean = (Boolean)properties.get(createTargetFieldName);
        createTarget = (createTargetBoolean != null && createTargetBoolean);

        Boolean continuousBoolean = (Boolean)properties.get(continuousFieldName);
        continuous = (continuousBoolean != null && continuousBoolean);

        Boolean cancelBoolean = (Boolean)properties.get(cancelFieldName);
        cancel = (cancelBoolean != null && cancelBoolean);

        if(source == null || target == null) {
            throw new CouchbaseLiteException("source and target are both null", new Status(Status.BAD_REQUEST));
        }

        if (Manager.isValidDatabaseName(getSource()) == false && Manager.isValidDatabaseName(getTarget()) == false) {
            throw new CouchbaseLiteException("source and target are both not valid local database names, we don't support having CouchBase Lite act as the middle man between replications of two database it doesn't own.",
                    new Status(Status.BAD_REQUEST));
        }

        push =  Manager.isValidDatabaseName(getSource());

        filter = (String)properties.get(filterFieldName);

        this.principal = principal;
    }

    public Map<String, Object> getRawProperties() { return rawProperties; }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public Map<String, Object> getHeaders() { return headers; }

    public String getSource() {
        return source;
    }

    public void setSource(String source) { this.source = source; }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) { this.target = target; }

    public boolean getCreateTarget() {
        return createTarget;
    }

    public boolean getContinuous() {
        return continuous;
    }

    public boolean getCancel() {
        return cancel;
    }

    public Map<String, Object> getSourceAuth() {
        return sourceAuth;
    }

    public Map<String, Object> getTargetAuth() {
        return targetAuth;
    }

    /**
     * If true then data from a local database (source) is being pushed to a remote database (target). If false then data from
     * a remote database (source) is being copied to a local database (target).
     * @return
     */
    public boolean getPush() { return push; }

    public String getFilterName() { return filter; }

    public Principal getPrincipal() { return principal; }

    private Map<String, Object> parseSourceOrTarget(Map<String,Object> properties, String key) {
        Map<String, Object> result = new HashMap<String, Object>();

        Object value = properties.get(key);

        if (value instanceof String) {
            result.put(urlFieldName, (String)value);
        }
        else if (value instanceof Map) {
            result = (Map<String, Object>) value;
        }
        return result;
    }
}
