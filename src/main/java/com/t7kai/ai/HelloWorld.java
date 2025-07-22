package com.t7kai.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.Map;

public class HelloWorld {

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tH:%1$tM:%1$tS => %5$s%6$s%n");
    }

    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();
        ChatCompletion chatCompletion = client.chat()
                .completions()
                .create(ChatCompletionCreateParams.builder()
                        .model("Qwen/Qwen3-8B")
                        .addUserMessage("你好，世界！")
                        .additionalBodyProperties(Map.of("enable_thinking", JsonValue.from(false)))
                        .build());
        System.out.println(chatCompletion.choices().getFirst().message().content().orElse("No response"));
    }

}
