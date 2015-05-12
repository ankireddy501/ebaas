package com.ebaas.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.generators.InputStreamBodyGenerator;
import com.ning.http.client.resumable.ResumableIOExceptionFilter;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Client to access a server with a RESTful interface. Is based on a HTTP client.
 */
public class RESTClient
{
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 21600000;
    private static final int IDLE_TIMEOUT_MS = 180000;
    private static final int MAX_CONNECTIONS_PER_HOST = 25;
    private static final int MAX_CONNECTIONS_TOTAL = 100;
    private static final int MAX_RETRY_COUNT = 5;

    private static final String URI_IS_NULL_ERROR = "The URI to send HTTP request to is null.";
    private static final String INVALID_BODY_ERROR = "The body to send with the HTTP request is invalid: ";
    private static final String MALFORMED_URL_ERROR = "The URL to send the HTTP request to is malformed: ";
    private static final String REQUEST_EXECUTION_ERROR = "Could not execute HTTP request: ";
    private static final String NO_RESPONSE_ERROR = "Could not get HTTP response: ";
    private static final String UNKNOWN_STATUS_CODE_ERROR = "Unknown status code in HTTP response: ";

    private enum HttpMethod
    {
        HEAD("HEAD"), PUT("PUT"), DELETE("DELETE"), GET("GET"), POST("POST");

        private String httpMethodString;

        private HttpMethod(String httpMethodString)
        {
            this.httpMethodString = httpMethodString;
        }

        public String getAsString()
        {
            return httpMethodString;
        }
    }

    public enum StatusCode
    {
        OK(200), CREATED(201), NOT_FOUND(404);

        private int statusCodeNumber;

        private StatusCode(int statusCodeNumber)
        {
            this.statusCodeNumber = statusCodeNumber;
        }
    }

    public class Response
    {
        private StatusCode statusCode;
        private String body;

        public Response(StatusCode statusCode, String body)
        {
            this.statusCode = statusCode;
            this.body = body;
        }

        public StatusCode getStatusCode()
        {
            return statusCode;
        }

        public String getBody()
        {
            return body;
        }

        public String getSource() {
            String responseBody = this.body;
            net.sf.json.JSONObject responseJSONObject = net.sf.json.JSONObject.fromObject(responseBody);
            return (String) responseJSONObject.get("_source");
        }

        public List<SearchHit> getSearchHits() {
            String responseBody = this.body;

            net.sf.json.JSONObject responseJSONObject = net.sf.json.JSONObject.fromObject(responseBody);
            net.sf.json.JSONObject hitsObject = (net.sf.json.JSONObject) responseJSONObject.get("hits");

            if (hitsObject == null)
            {
                return Collections.emptyList();
            }

            int totalNumberOfHits = hitsObject.getInt("total");

            if (totalNumberOfHits == 0)
            {
                return Collections.emptyList();
            }

            List<SearchHit> searchHits = new ArrayList<SearchHit>();

            net.sf.json.JSONArray hitsArray = hitsObject.getJSONArray("hits");

            Iterator<Object> hitsIterator = hitsArray.iterator();

            while (hitsIterator.hasNext())
            {
                net.sf.json.JSONObject hitObject = (net.sf.json.JSONObject) hitsIterator.next();

                Map<String, String> hitEntryMap = new HashMap<String, String>();
                Set<Map.Entry<String, String>> hitEntries = hitObject.entrySet();

                for (Map.Entry hitEntry : hitEntries)
                {
                    Object hitEntryValue = hitEntry.getValue();

                    if (!(hitEntryValue instanceof String))
                    {
                        hitEntryValue = hitEntryValue.toString();
                    }

                    hitEntryMap.put((String) hitEntry.getKey(), (String) hitEntryValue);
                }

                searchHits.add(new SearchHit(hitEntryMap));
            }

            return searchHits;
        }

        public boolean isCreated() {
            String responseBody = this.body;
            net.sf.json.JSONObject responseJSONObject = net.sf.json.JSONObject.fromObject(responseBody);
            return responseJSONObject.getString("created").equals("true") ? true : false;
        }
    }

    public class SearchHit
    {
        private Map<String, String> hitEntries;

        private SearchHit(Map<String, String> hitEntries)
        {
            this.hitEntries = hitEntries;
        }

        public String getEntry(String key)
        {
            return hitEntries.get(key);
        }

        public int getNumberOfEntries()
        {
            return hitEntries.size();
        }
    }


    private AsyncHttpClient asyncHttpClient;

    /**
     * c-tor
     */
    public RESTClient()
    {
        asyncHttpClient = createHttpClient();
    }

    /**
     * Method to be overridden by test classes, so that the HTTP client can be mocked.
     *
     * @return HTTP client
     */
    protected AsyncHttpClient createHttpClient()
    {
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setAllowPoolingConnection(true).setAllowSslConnectionPool(true).setCompressionEnabled(true)
                .setFollowRedirects(false).setMaximumConnectionsTotal(MAX_CONNECTIONS_TOTAL)
                .setMaximumConnectionsPerHost(MAX_CONNECTIONS_PER_HOST).setConnectionTimeoutInMs(CONNECT_TIMEOUT_MS)
                .setIdleConnectionTimeoutInMs(IDLE_TIMEOUT_MS).setRequestTimeoutInMs(READ_TIMEOUT_MS)
                .setMaxRequestRetry(MAX_RETRY_COUNT).addIOExceptionFilter(new ResumableIOExceptionFilter());

        ExecutorService callbackExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("EbaasAsyncCallback-%s").build());
        builder.setExecutorService(callbackExecutor);

        ScheduledExecutorService idleConnectionReaper = Executors.newScheduledThreadPool(Runtime.getRuntime()
                .availableProcessors(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat(
                "EbaasIdleConnectionReaper-%s").build());
        builder.setScheduledExecutorService(idleConnectionReaper);

        builder.setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());

        return new AsyncHttpClient(builder.build());
    }

    /**
     * Executes a request with the HEAD method for a specific URI
     *
     * @param uri URI where the HEAD request is sent to
     * @return Status code which indicates if the request has been successful
     */
    public StatusCode head(URI uri)
    {
        validateParameter(uri);

        return executeRequestAndReturnStatusCodeFromResponse(HttpMethod.HEAD, uri, null, null);
    }

    /**
     * Executes a request with the PUT method for a specific URI
     *
     * @param uri URI where the PUT request is sent to
     * @return Status code which indicates if the request has been successful
     */
    public StatusCode put(URI uri)
    {
        validateParameter(uri);

        return putInternal(uri, null);
    }

    /**
     * Executes a request with the PUT method for a specific URI and with a specific request body
     *
     * @param uri URI where the PUT request is sent to
     * @param body The body of the request
     * @return Status code which indicates if the request has been successful
     */
    public StatusCode put(URI uri, String body)
    {
        validateParameters(uri, body);

        return putInternal(uri, body);
    }

    private StatusCode putInternal(URI uri, String body)
    {
        return executeRequestAndReturnStatusCodeFromResponse(HttpMethod.PUT, uri, body, null);
    }

    /**
     * Executes a request with the DELETE method for a specific URI
     *
     * @param uri URI where the DELETE request is sent to
     * @return Status code which indicates if the request has been successful
     */
    public StatusCode delete(URI uri)
    {
        validateParameter(uri);

        return executeRequestAndReturnStatusCodeFromResponse(HttpMethod.DELETE, uri, null, null);
    }

    /**
     * Executes a request with the DELETE method for a specific URI
     *
     * @param uri URI where the DELETE request is sent to
     * @return Status code which indicates if the request has been successful
     */
    public StatusCode delete(URI uri, Cookie cookie)
    {
        validateParameter(uri);

        return executeRequestAndReturnStatusCodeFromResponse(HttpMethod.DELETE, uri, null, cookie);
    }

    /**
     * Executes a request with the GET method for a specific URI
     *
     * @param uri URI where the GET request is sent to
     * @return Status code which indicates if the request has been successful
     */
    public Response get(URI uri)
    {
        String body = null;
        validateParameter(uri);
        return executeRequestAndReturnResponse(HttpMethod.GET, uri, body, null);
    }

    /**
     * Executes a request with the GET method for a specific URI
     *
     * @param uri URI where the GET request is sent to
     * @param cookie for authentication
     * @return Status code which indicates if the request has been successful
     */
    public Response get(URI uri, Cookie cookie)
    {
        String body = null;
        validateParameter(uri);
        cookie.setDomain(uri.getHost());
        cookie.setPath("/");
        return executeRequestAndReturnResponse(HttpMethod.GET, uri, body, cookie);
    }

    public InputStream getAsStream(URI uri, Cookie cookie){
        validateParameter(uri);
        cookie.setDomain(uri.getHost());
        cookie.setPath("/");
        return executeRequestAndReturnInputStream(HttpMethod.GET, uri, null, cookie);
    }

    /**
     * Executes a request with the POST method for a specific URI and with a specific request body
     *
     * @param uri URI where the POST request is sent to
     * @param body The body of the request
     * @return The response body
     */
    public Response post(URI uri, String body)
    {
        validateParameters(uri, body);

        return executeRequestAndReturnResponse(HttpMethod.POST, uri, body, null);
    }

    public Response post(URI uri, String body,Cookie cookie)
    {
        validateParameters(uri, body);

        return executeRequestAndReturnResponse(HttpMethod.POST, uri, body, cookie);
    }

    public Response post(URI uri, InputStream body, Cookie cookie)
    {
        validateParameter(uri);

        return executeRequestAndReturnResponse(HttpMethod.POST, uri, body, cookie);
    }

    private StatusCode executeRequestAndReturnStatusCodeFromResponse(HttpMethod httpMethod, URI uri, String body,
            Cookie cookie)
    {
        com.ning.http.client.Response response = executeRequest(httpMethod, uri, body, cookie);

        return getStatusCodeFromNumber(response.getStatusCode());
    }

    private Response executeRequestAndReturnResponse(HttpMethod httpMethod, URI uri, String body, Cookie cookie)
    {
        com.ning.http.client.Response response = executeRequest(httpMethod, uri, body, cookie);

        String responseBody = null;

        try
        {
            responseBody = response.getResponseBody();
        }
        catch (IOException e)
        {
            handleStateError(e.toString());
        }

        return new Response(getStatusCodeFromNumber(response.getStatusCode()), responseBody);
    }

    private Response executeRequestAndReturnResponse(HttpMethod httpMethod, URI uri, InputStream body, Cookie cookie)
    {
        com.ning.http.client.Response response = executeRequest(httpMethod, uri, body, cookie);

        String responseBody = null;

        try
        {
            responseBody = response.getResponseBody();
        }
        catch (IOException e)
        {
            handleStateError(e.toString());
        }

        return new Response(getStatusCodeFromNumber(response.getStatusCode()), responseBody);
    }

    private InputStream executeRequestAndReturnInputStream(HttpMethod httpMethod, URI uri, String body, Cookie cookie)
    {
        com.ning.http.client.Response response = executeRequest(httpMethod, uri, body, cookie);

        InputStream responseBody = null;

        try
        {
            responseBody = response.getResponseBodyAsStream();
        }
        catch (IOException e)
        {
            handleStateError(e.toString());
        }

        return responseBody;
    }

    private String executeRequestAndReturnResponseBody(HttpMethod httpMethod, URI uri, String body, Cookie cookie)
    {
        com.ning.http.client.Response response = executeRequest(httpMethod, uri, body, cookie);

        String responseBody = null;

        try
        {
            responseBody = response.getResponseBody();
        }
        catch (IOException e)
        {
            handleStateError(e.toString());
        }

        return responseBody;
    }

    private com.ning.http.client.Response executeRequest(HttpMethod httpMethod, URI uri, String body, Cookie cookie)
    {
        Request request = createRequest(httpMethod, uri, body, cookie);

        return executeRequest(request);
    }

    private com.ning.http.client.Response executeRequest(HttpMethod httpMethod, URI uri, InputStream body, Cookie cookie)
    {
        Request request = createRequest(httpMethod, uri, body, cookie);

        return executeRequest(request);
    }

    private Request createRequest(HttpMethod httpMethod, URI uri, String body, Cookie cookie)
    {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setMethod(httpMethod.getAsString());
        requestBuilder.setUrl(getURLString(uri));

        if (body != null)
        {
            requestBuilder.setBodyEncoding("UTF-8");
            requestBuilder.setBody(body);
        }

        if (cookie != null)
        {
            requestBuilder.addCookie(new com.ning.http.client.Cookie(cookie.getDomain(), cookie.getName(), cookie
                    .getValue(), cookie.getPath(), cookie.getMaxAge(), cookie.getSecure()));

        }

        return requestBuilder.build();
    }

    private Request createRequest(HttpMethod httpMethod, URI uri, InputStream body, Cookie cookie)
    {
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setMethod(httpMethod.getAsString());
        requestBuilder.setUrl(getURLString(uri));

        if (body != null)
        {
            requestBuilder.setBody(new InputStreamBodyGenerator(body));
        }
        if (cookie != null)
        {
            requestBuilder.addCookie(new com.ning.http.client.Cookie(cookie.getDomain(), cookie.getName(), cookie
                    .getValue(), cookie.getPath(), cookie.getMaxAge(), cookie.getSecure()));

        }

        return requestBuilder.build();
    }

    private String getURLString(URI uri)
    {
        URL url = null;

        try
        {
            url = uri.toURL();
        }
        catch (MalformedURLException e)
        {
            handleArgumentError(MALFORMED_URL_ERROR + uri.toString());
        }

        return url.toString();
    }

    private com.ning.http.client.Response executeRequest(Request request)
    {
        ListenableFuture<com.ning.http.client.Response> listenableFuture = null;

        try
        {
            listenableFuture = asyncHttpClient.executeRequest(request);
        }
        catch (IOException e)
        {
            handleStateError(REQUEST_EXECUTION_ERROR + e.getMessage());
        }

        if (listenableFuture == null)
        {
            handleStateError(NO_RESPONSE_ERROR + listenableFuture);
        }

        com.ning.http.client.Response response = null;

        try
        {
            response = listenableFuture.get();
        }
        catch (InterruptedException e)
        {
            handleStateError(NO_RESPONSE_ERROR + e.getMessage());
        }
        catch (ExecutionException e)
        {
            handleStateError(NO_RESPONSE_ERROR + e.getMessage());
        }

        return response;
    }

    private StatusCode getStatusCodeFromNumber(int statusCodeNumber)
    {
        StatusCode statusCode = null;

        if (statusCodeNumber == StatusCode.OK.statusCodeNumber)
        {
            statusCode = StatusCode.OK;
        }
        else if (statusCodeNumber == StatusCode.CREATED.statusCodeNumber)
        {
            statusCode = StatusCode.CREATED;
        }
        else if (statusCodeNumber == StatusCode.NOT_FOUND.statusCodeNumber)
        {
            statusCode = StatusCode.NOT_FOUND;
        }
        else
        {
            handleStateError(UNKNOWN_STATUS_CODE_ERROR + statusCodeNumber);
        }

        return statusCode;
    }

    /**
     * Closes the internal http client
     */
    public void close()
    {
        asyncHttpClient.close();
    }

    private void validateParameters(URI uri, String body)
    {
        validateParameter(uri);

        if (body == null || body.trim().isEmpty())
        {
            handleArgumentError(INVALID_BODY_ERROR + body);
        }
    }

    private void validateParameter(URI uri)
    {
        if (uri == null)
        {
            handleArgumentError(URI_IS_NULL_ERROR);
        }
    }

    private void handleArgumentError(String errorMessage)
    {
        handleError(errorMessage, new IllegalArgumentException(errorMessage));
    }

    private void handleStateError(String errorMessage)
    {
        handleError(errorMessage, new IllegalStateException(errorMessage));
    }

    private void handleError(String errorMessage, RuntimeException runtimeException)
    {
        throw runtimeException;
    }
}
