package com.couchbase.lite.support;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.auth.AuthenticatorImpl;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.util.Log;
import com.couchbase.lite.util.URIUtils;
import com.couchbase.lite.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.ClientParamsStack;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class RemoteRequest implements Runnable {

    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY_MS = 10 * 1000;

    protected ScheduledExecutorService workExecutor;
    protected final HttpClientFactory clientFactory;
    protected String method;
    protected URL url;
    protected Object body;
    protected Authenticator authenticator;
    protected RemoteRequestCompletionBlock onPreCompletion;
    protected RemoteRequestCompletionBlock onCompletion;
    protected RemoteRequestCompletionBlock onPostCompletion;
    private int retryCount;
    private Database db;
    protected HttpUriRequest request;

    protected Map<String, Object> requestHeaders;

    public RemoteRequest(ScheduledExecutorService workExecutor,
                         HttpClientFactory clientFactory, String method, URL url,
                         Object body, Database db, Map<String, Object> requestHeaders, RemoteRequestCompletionBlock onCompletion) {
        this.clientFactory = clientFactory;
        this.method = method;
        this.url = url;
        this.body = body;
        this.onCompletion = onCompletion;
        this.workExecutor = workExecutor;
        this.requestHeaders = requestHeaders;
        this.db = db;
        this.request = createConcreteRequest();
        Log.v(Log.TAG_SYNC, "%s: RemoteRequest created, url: %s", this, url);

    }

    @Override
    public void run() {

        Log.v(Log.TAG_SYNC, "%s: RemoteRequest run() called, url: %s", this, url);

        HttpClient httpClient = clientFactory.getHttpClient();

        ClientConnectionManager manager = httpClient.getConnectionManager();

        preemptivelySetAuthCredentials(httpClient);

        request.addHeader("Accept", "multipart/related, application/json");

        addRequestHeaders(request);

        setBody(request);

        executeRequest(httpClient, request);

        Log.v(Log.TAG_SYNC, "%s: RemoteRequest run() finished, url: %s", this, url);


    }

    public void abort() {
        if (request != null) {
            request.abort();
        } else {
            Log.w(Log.TAG_REMOTE_REQUEST, "%s: Unable to abort request since underlying request is null", this);
        }
    }

    public HttpUriRequest getRequest() {
        return request;
    }

    protected void addRequestHeaders(HttpUriRequest request) {
        for (String requestHeaderKey : requestHeaders.keySet()) {
            request.addHeader(requestHeaderKey, requestHeaders.get(requestHeaderKey).toString());
        }
    }

    public void setOnPostCompletion(RemoteRequestCompletionBlock onPostCompletion) {
        this.onPostCompletion = onPostCompletion;
    }

    public void setOnPreCompletion(RemoteRequestCompletionBlock onPreCompletion) {
        this.onPreCompletion = onPreCompletion;
    }

    protected HttpUriRequest createConcreteRequest() {
        HttpUriRequest request = null;
        if (method.equalsIgnoreCase("GET")) {
            request = new HttpGet(url.toExternalForm());
        } else if (method.equalsIgnoreCase("PUT")) {
            request = new HttpPut(url.toExternalForm());
        } else if (method.equalsIgnoreCase("POST")) {
            request = new HttpPost(url.toExternalForm());
        }
        return request;
    }

    protected void setBody(HttpUriRequest request) {
        // set body if appropriate
        if (body != null && request instanceof HttpEntityEnclosingRequestBase) {
            byte[] bodyBytes = null;
            try {
                bodyBytes = Manager.getObjectMapper().writeValueAsBytes(body);
            } catch (Exception e) {
                Log.e(Log.TAG_REMOTE_REQUEST, "Error serializing body of request", e);
            }
            ByteArrayEntity entity = new ByteArrayEntity(bodyBytes);
            entity.setContentType("application/json");
            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
        }
    }

    /**
     *  Set Authenticator for BASIC Authentication
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Retry this remote request, unless we've already retried MAX_RETRIES times
     *
     * NOTE: This assumes all requests are idempotent, since even though we got an error back, the
     * request might have succeeded on the remote server, and by retrying we'd be issuing it again.
     * PUT and POST requests aren't generally idempotent, but the ones sent by the replicator are.
     *
     * @return true if going to retry the request, false otherwise
     */
    protected boolean retryRequest() {
        if (retryCount >= MAX_RETRIES) {
            return false;
        }
        workExecutor.schedule(this, RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
        retryCount += 1;
        Log.d(Log.TAG_REMOTE_REQUEST, "Will retry in %d ms", RETRY_DELAY_MS);
        return true;
    }

    protected void executeRequest(HttpClient httpClient, HttpUriRequest request) {
        Object fullBody = null;
        Throwable error = null;
        HttpResponse response = null;

        try {

            Log.v(Log.TAG_SYNC, "%s: RemoteRequest executeRequest() called, url: %s", this, url);

            if (request.isAborted()) {
                Log.v(Log.TAG_SYNC, "%s: RemoteRequest has already been aborted", this);
                respondWithResult(fullBody, new Exception(String.format("%s: Request %s has been aborted", this, request)), response);
                return;
            }

            Log.v(Log.TAG_SYNC, "%s: RemoteRequest calling httpClient.execute", this);
            response = httpClient.execute(request);
            Log.v(Log.TAG_SYNC, "%s: RemoteRequest called httpClient.execute", this);

            // add in cookies to global store
            try {
                if (httpClient instanceof DefaultHttpClient) {
                    DefaultHttpClient defaultHttpClient = (DefaultHttpClient)httpClient;
                    this.clientFactory.addCookies(defaultHttpClient.getCookieStore().getCookies());
                }
            } catch (Exception e) {
                Log.e(Log.TAG_REMOTE_REQUEST, "Unable to add in cookies to global store", e);
            }

            StatusLine status = response.getStatusLine();
            if (Utils.isTransientError(status) && retryRequest()) {
                return;
            }

            if (status.getStatusCode() >= 300) {
                Log.e(Log.TAG_REMOTE_REQUEST, "Got error status: %d for %s.  Reason: %s", status.getStatusCode(), request, status.getReasonPhrase());
                error = new HttpResponseException(status.getStatusCode(),
                        status.getReasonPhrase());
            } else {
                HttpEntity temp = response.getEntity();
                if (temp != null) {
                    InputStream stream = null;
                    try {
                        stream = temp.getContent();
                        fullBody = Manager.getObjectMapper().readValue(stream,
                                Object.class);
                    } finally {
                        try {
                            stream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(Log.TAG_REMOTE_REQUEST, "io exception", e);
            error = e;
            // Treat all IOExceptions as transient, per:
            // http://hc.apache.org/httpclient-3.x/exception-handling.html
            Log.v(Log.TAG_SYNC, "%s: RemoteRequest calling retryRequest()", this);
            if (retryRequest()) {
                return;
            } else {
                Log.e(Log.TAG_SYNC, "%s: RemoteRequest failed all retries, giving up.", this);
            }
        } catch (Exception e) {
            Log.e(Log.TAG_REMOTE_REQUEST, "%s: executeRequest() Exception: ", e, this);
            error = e;
        }
        Log.v(Log.TAG_SYNC, "%s: RemoteRequest calling respondWithResult.  error: %s", this, error);
        respondWithResult(fullBody, error, response);

    }

    protected void preemptivelySetAuthCredentials(HttpClient httpClient) {
        boolean isUrlBasedUserInfo = false;

        String userInfo = url.getUserInfo();
        if (userInfo != null) {
            isUrlBasedUserInfo = true;
        } else {
            if (authenticator != null) {
                AuthenticatorImpl auth = (AuthenticatorImpl) authenticator;
                userInfo = auth.authUserInfo();
            }
        }

        if (userInfo != null) {
            if (userInfo.contains(":") && !userInfo.trim().equals(":")) {
                String[] userInfoElements = userInfo.split(":");
                String username = isUrlBasedUserInfo ? URIUtils.decode(userInfoElements[0]): userInfoElements[0];
                String password = isUrlBasedUserInfo ? URIUtils.decode(userInfoElements[1]): userInfoElements[1];
                final Credentials credentials = new UsernamePasswordCredentials(username, password);

                if (httpClient instanceof DefaultHttpClient) {
                    DefaultHttpClient dhc = (DefaultHttpClient) httpClient;
                    HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
                        @Override
                        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
                            if (authState.getAuthScheme() == null) {
                                authState.setAuthScheme(new BasicScheme());
                                authState.setCredentials(credentials);
                            }
                        }
                    };
                    dhc.addRequestInterceptor(preemptiveAuth, 0);
                }
            } else {
                Log.w(Log.TAG_REMOTE_REQUEST, "RemoteRequest Unable to parse user info, not setting credentials");
            }
        }
    }

    public void respondWithResult(final Object result, final Throwable error, final HttpResponse response) {

        if (workExecutor != null) {
            workExecutor.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (onPreCompletion != null) {
                            onPreCompletion.onCompletion(response, error);
                        }
                        onCompletion.onCompletion(result, error);
                        if (onPostCompletion != null) {
                            onPostCompletion.onCompletion(response, error);
                        }
                    } catch (Exception e) {
                        // don't let this crash the thread
                        Log.e(Log.TAG_REMOTE_REQUEST,
                                "RemoteRequestCompletionBlock throw Exception",
                                e);
                    }
                }
            });
        } else {
            Log.e(Log.TAG_REMOTE_REQUEST, "Work executor was null!");
        }
    }

}
