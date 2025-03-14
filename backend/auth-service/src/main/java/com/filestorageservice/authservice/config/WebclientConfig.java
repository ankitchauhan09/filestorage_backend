package com.filestorageservice.authservice.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
public class WebclientConfig {
    @Bean
    public WebClient webClient() throws KeyStoreException, IOException, NoSuchAlgorithmException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream trustStoreStream = getClass().getClassLoader().getResourceAsStream("auth_truststore.jks")) {
            trustStore.load(trustStoreStream, "filestorage_auth".toCharArray());
        } catch (CertificateException e) {  
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(trustManagerFactory)
                .build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
//        return WebClient.builder().build();
    }

}
