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
        Agent agent = new Agent(client, "Qwen/Qwen3-32B", List.of(Compare.class, GetTime.class), true);
        try (Scanner scanner = new Scanner(System.in)) {
            printUserMessage();
            while (true) {
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                    System.out.println("\033[91mExiting the agent. Goodbye!\033[0m");
                    break;
                }
                if (userInput.isBlank()) {
                    printUserMessage();
                    continue;
                }
                printAssistantMessage(agent.complete(userInput));
                printUserMessage();
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
