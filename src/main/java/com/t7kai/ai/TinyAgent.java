package com.t7kai.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.t7kai.ai.agent.Agent;
import com.t7kai.ai.agent.tools.Compare;
import com.t7kai.ai.agent.tools.GetTime;

import java.util.List;
import java.util.Scanner;

public class TinyAgent {

    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();
        // 创建一个Agent实例，使用Qwen3-32B模型
        Agent agent = new Agent(client, "Qwen/Qwen3-32B", List.of(Compare.class, GetTime.class), true);
        try (Scanner scanner = new Scanner(System.in)) {
            printUserMessage();
            while (true) {
                String userInput = scanner.nextLine();
                // 检查用户输入是否为退出命令
                if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                    System.out.println("\033[91mExiting the agent. Goodbye!\033[0m");
                    break;
                }
                if (userInput.isBlank()) {
                    printUserMessage();
                    continue;
                }
                // 处理用户输入并获取助手回复
                printAssistantMessage(agent.complete(userInput));
                printUserMessage();
            }
        }
    }

    private static void printAssistantMessage(String message) {
        // 使用ANSI转义码打印绿色文本 助手消息
        System.out.println("\033[92mAssistant: \033[0m" + message.strip());
    }

    private static void printUserMessage() {
        // 使用ANSI转义码打印蓝色文本 用户消息
        System.out.print("\033[94mUser: \033[0m");
    }
}
