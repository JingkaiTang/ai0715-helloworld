package com.t7kai.ai;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class HelloWorld {

    public static void main(String[] args) {
        OpenAIClient client = Env.getClient();
        ChatCompletion chatCompletion = client.chat()
                .completions()
                .create(ChatCompletionCreateParams.builder()
                        .model("Qwen/Qwen3-8B")
                        .addUserMessage("你好，世界！")
                        .build());
        System.out.println(chatCompletion.choices().getFirst().message().content().orElse("No response"));
    }

}
