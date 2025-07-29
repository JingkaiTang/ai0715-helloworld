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
        // 构建客户端
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();
        // 构建多轮对话的参数，后续一直使用该builder构建调用参数，以此保留对话上下文
        ChatCompletionCreateParams.Builder createParamsBuilder = ChatCompletionCreateParams.builder()
                .model("Qwen/Qwen3-8B")
                .addSystemMessage("你是一个友好的助手。")
                // 关闭思考模式
                .additionalBodyProperties(Map.of("enable_thinking", JsonValue.from(false)));
        // 添加初始助手消息
        String initialAssistantMessage = "你好！请问有什么我可以帮助你的吗？";
        createParamsBuilder.addAssistantMessage(initialAssistantMessage);
        printAssistantMessage(initialAssistantMessage);
        printUserMessage();
        // 使用stdin作为交互入口
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNext()) {
                String userMessage = scanner.nextLine();
                // 将用户消息添加到对话上下文中
                createParamsBuilder.addUserMessage(userMessage);
                // 调用OpenAI API获取助手回复
                client.chat()
                        .completions()
                        .create(createParamsBuilder.build())
                        .choices()
                        .stream()
                        .findFirst()
                        .ifPresent(choice -> {
                            // 打印助手的回复
                            String assistantResponse = choice.message().content().orElse("No response");
                            printAssistantMessage(assistantResponse);
                            // 将助手的回复添加到对话上下文中
                            createParamsBuilder.addAssistantMessage(assistantResponse);
                            printUserMessage();
                        });
            }
        }
    }

    private static void printAssistantMessage(String message) {
        // 使用ANSI转义码打印绿色文本 区分助手和用户消息
        System.out.println("\033[92mAssistant: \033[0m" + message.strip());
    }

    private static void printUserMessage() {
        // 使用ANSI转义码打印蓝色文本 区分助手和用户消息
        System.out.print("\033[94mUser: \033[0m");
    }

}
