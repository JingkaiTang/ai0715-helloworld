package com.t7kai.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.Map;
import java.util.Scanner;

/**
 * 多轮对话示例
 */
public class MultiTurnConversation {

    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();
        ChatCompletionCreateParams.Builder createParamsBuilder = ChatCompletionCreateParams.builder()
                .model("Qwen/Qwen3-8B")
                .addSystemMessage("你是一个友好的助手。")
                .additionalBodyProperties(Map.of("enable_thinking", JsonValue.from(false)));
        String initialAssistantMessage = "你好！请问有什么我可以帮助你的吗？";
        createParamsBuilder.addAssistantMessage(initialAssistantMessage);
        printAssistantMessage(initialAssistantMessage);
        printUserMessage();
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNext()) {
                String userMessage = scanner.nextLine();
                createParamsBuilder.addUserMessage(userMessage);
                client.chat()
                        .completions()
                        .create(createParamsBuilder.build())
                        .choices()
                        .stream()
                        .findFirst()
                        .ifPresent(choice -> {
                            String assistantResponse = choice.message().content().orElse("No response");
                            printAssistantMessage(assistantResponse);
                            createParamsBuilder.addAssistantMessage(assistantResponse);
                            printUserMessage();
                        });
            }
        }
    }

    private static void printAssistantMessage(String message) {
        System.out.println("\033[92mAssistant: \033[0m" + message.strip());
    }

    private static void printUserMessage() {
        System.out.print("\033[94mUser: \033[0m");
    }

}
