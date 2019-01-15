package com.mycorp;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mycorp.support.Ticket;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Realm;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.ning.http.client.uri.Uri;
import static com.mycorp.constants.ZendeskConstants.JSON;
import static com.mycorp.constants.ZendeskConstants.RESTRICTED_PATTERN;

public class Zendesk implements Closeable {
	
    /** The Constant LOG. */
    private static final Logger logger = LoggerFactory.getLogger( ZendeskService.class );
	
	//EXTRACT - it must relocate to a Constant class
    private final boolean closeClient;
    private final AsyncHttpClient client;
    private final Realm realm;
    private final String url;
    private final String oauthToken;
    //CREATE - create a specific Zendesk's mapper class and inject dependency
    private final ObjectMapper mapper;
    private boolean closed = false;


    //It must be refactorized. A constructor only must initialize properties
    private Zendesk(AsyncHttpClient client, String url, String username, String password) {
        this.closeClient = client == null;
        this.oauthToken = null;
        this.client = client == null ? new AsyncHttpClient() : client;
        this.url = url.endsWith("/") ? url + "api/v2" : url + "/api/v2";
        //This is not its job
        if (username != null) {
            this.realm = new Realm.RealmBuilder()
                    .setScheme(Realm.AuthScheme.BASIC)
                    .setPrincipal(username)
                    .setPassword(password)
                    .setUsePreemptiveAuth(true)
                    .build();
        } else {
            if (password != null) {
                throw new IllegalStateException("Cannot specify token or password without specifying username");
            }
            this.realm = null;
        }
    	//This is not its job
        this.mapper = createMapper();
    }

    public Ticket createTicket(Ticket ticket) {
        return complete(submit(req("POST", cnst("/tickets.json"),
                        JSON, json(Collections.singletonMap("ticket", ticket))),
                handle(Ticket.class, "ticket")));
    }

    //EXTRACT - it must be a specific method of Zendesk's mapper class
    private byte[] json(Object object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new ZendeskException(e.getMessage(), e);
        }
    }

    private Request req(String method, Uri template, String contentType, byte[] body) {
        RequestBuilder builder = new RequestBuilder(method);
        if (realm != null) {
            builder.setRealm(realm);
        } else {
            builder.addHeader("Authorization", "Bearer " + oauthToken);
        }
        builder.setUrl(RESTRICTED_PATTERN.matcher(template.toString()).replaceAll("+")); //replace out %2B with + due to API restriction
        builder.addHeader("Content-type", contentType);
        builder.setBody(body);
        return builder.build();
    }
    
    //EXTRACT - it must be a specific class
    public static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private Uri cnst(String template) {
        return Uri.create(url + template);
    }

    private boolean isStatus2xx(Response response) {
        return response.getStatusCode() / 100 == 2;
    }

    private <T> ListenableFuture<T> submit(Request request, ZendeskAsyncCompletionHandler<T> handler) {
        if (logger.isDebugEnabled()) {
            if (request.getStringData() != null) {
                logger.debug("Request {} {}\n{}", request.getMethod(), request.getUrl(), request.getStringData());
            } else if (request.getByteData() != null) {
                logger.debug("Request {} {} {} {} bytes", request.getMethod(), request.getUrl(),
                        request.getHeaders().getFirstValue("Content-type"), request.getByteData().length);
            } else {
                logger.debug("Request {} {}", request.getMethod(), request.getUrl());
            }
        }
        return client.executeRequest(request, handler);
    }

    //EXTRACT - it must be a specific method of Zendesk's logger class
    private void logResponse(Response response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Response HTTP/{} {}\n{}", response.getStatusCode(), response.getStatusText(),
                    response.getResponseBody());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Response headers {}", response.getHeaders());
        }
    }

    private boolean isRateLimitResponse(Response response) {
        return response.getStatusCode() == 429;
    }

    protected <T> ZendeskAsyncCompletionHandler<T> handle(final Class<T> clazz, final String name, final Class... typeParams) {
        return new BasicAsyncCompletionHandler<T>(clazz, name, typeParams);
    }

    private class BasicAsyncCompletionHandler<T> extends ZendeskAsyncCompletionHandler<T> {
        private final Class<T> clazz;
        private final String name;
        private final Class[] typeParams;

        public BasicAsyncCompletionHandler(Class clazz, String name, Class... typeParams) {
            this.clazz = clazz;
            this.name = name;
            this.typeParams = typeParams;
        }

        @Override
        public T onCompleted(Response response) throws Exception {
            logResponse(response);
            if (isStatus2xx(response)) {
                if (typeParams.length > 0) {
                    JavaType type = mapper.getTypeFactory().constructParametricType(clazz, typeParams);
                    return mapper.convertValue(mapper.readTree(response.getResponseBodyAsStream()).get(name), type);
                }
                return mapper.convertValue(mapper.readTree(response.getResponseBodyAsStream()).get(name), clazz);
            } else if (isRateLimitResponse(response)) {
                throw new ZendeskException(response.toString());
            }
            if (response.getStatusCode() == 404) {
                return null;
            }
            throw new ZendeskException(response.toString());
        }
    }


    private static abstract class ZendeskAsyncCompletionHandler<T> extends AsyncCompletionHandler<T> {
        @Override
        public void onThrowable(Throwable t) {
            if (t instanceof IOException) {
                throw new ZendeskException(t);
            } else {
                super.onThrowable(t);
            }
        }
    }


    //////////////////////////////////////////////////////////////////////
    // Closeable interface methods
    //////////////////////////////////////////////////////////////////////

    public boolean isClosed() {
        return closed || client.isClosed();
    }

    public void close() {
        if (closeClient && !client.isClosed()) {
            client.close();
        }
        closed = true;
    }

    //////////////////////////////////////////////////////////////////////
    // Static helper methods
    //////////////////////////////////////////////////////////////////////

    private static <T> T complete(ListenableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new ZendeskException(e.getMessage(), e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ZendeskException) {
                throw (ZendeskException) e.getCause();
            }
            throw new ZendeskException(e.getMessage(), e);
        }
    }

    public static class Builder {
        private AsyncHttpClient client = null;
        private final String url;
        private String username = null;
        private String password = null;
        private String token = null;
        private String oauthToken = null;

        public Builder(String url) {
            this.url = url;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            if (password != null) {
                this.token = null;
                this.oauthToken = null;
            }
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            if (token != null) {
                this.password = null;
                this.oauthToken = null;
            }
            return this;
        }

        public Zendesk build() {
            if (token != null) {
                return new Zendesk(client, url, username + "/token", token);
            }
            return new Zendesk(client, url, username, password);
        }
    }
}