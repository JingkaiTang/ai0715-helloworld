package com.t7kai.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import java.io.InputStream;
import java.util.Properties;

public class Env {

    static {
        load();
    }

    private static void load() {
        Properties properties = new Properties();
        try (InputStream input = Env.class.getClassLoader().getResourceAsStream(".env")) {
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                System.setProperty(key, value);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load .env file", ex);
        }
    }

    public static OpenAIClient getClient() {
        return OpenAIOkHttpClient.builder()
                .apiKey(System.getProperty("OPENAI_API_KEY"))
                .baseUrl(System.getProperty("OPENAI_BASE_URL", "https://api.openai.com/v1"))
                .build();
    }

}
