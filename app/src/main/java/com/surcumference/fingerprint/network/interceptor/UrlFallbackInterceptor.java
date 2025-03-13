package com.surcumference.fingerprint.network.interceptor;

import com.surcumference.fingerprint.util.log.L;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import okhttp3.*;

/**
 * An OkHttp interceptor that tries fallback URLs when the original request fails.
 * It will try each URL in sequence until one succeeds or all fail.
 */
public class UrlFallbackInterceptor implements Interceptor {
    private final List<String> fallbackUrls;

    /**
     * Creates a new UrlFallbackInterceptor with the specified fallback URLs.
     * 
     * @param fallbackUrls The URLs to try if the original request fails
     */
    public UrlFallbackInterceptor(String... fallbackUrls) {
        this.fallbackUrls = Arrays.asList(fallbackUrls);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        IOException lastException = null;
        
        // Try the original request
        try {
            Response response = chain.proceed(originalRequest);
            if (response.isSuccessful()) {
                return response;
            }
            String body = response.body().string();
            response.close();
            throw new IOException("Error: request fail code: " + response.code() + " body: " + body);
        } catch (IOException e) {
            // Save the exception
            lastException = e;
            L.e("Error: request fail on url: " + originalRequest.url().toString() + " try next..", lastException);
        }
        
        // Try each fallback URL
        for (String fallbackUrl : fallbackUrls) {
            HttpUrl newUrl = HttpUrl.parse(fallbackUrl);
            if (newUrl == null) continue;
            
            Request newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .build();
            
            try {
                Response response = chain.proceed(newRequest);
                if (response.isSuccessful()) {
                    return response;
                }
                String body = response.body().string();
                response.close();
                throw new IOException("Error: request fail code: " + response.code() + " body: " + body);
            } catch (IOException e) {
                // Update the last exception
                lastException = e;
                L.e("Error: request fail on fallback url: " + fallbackUrl + " try next..", e);
            }
        }
        
        // All URLs failed, throw the last exception
        if (lastException != null) {
            throw lastException;
        } else {
            // In case no exception was caught but all responses were unsuccessful
            throw new IOException("All URL requests failed without specific exceptions");
        }
    }
}