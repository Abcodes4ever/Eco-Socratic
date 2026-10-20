package com.greensocratic;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class LLMService {

    private final ChatLanguageModel model;

    public LLMService() {
        ChatLanguageModel builtModel = null;
        try {
            builtModel = OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("phi3:mini")
                    .timeout(Duration.ofSeconds(60))
                    .temperature(0.3)
                    .build();
        } catch (RuntimeException e) {
            builtModel = null;
        }
        this.model = builtModel;
    }

    public CompletableFuture<String> askSocratesAsync(String userPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            if (userPrompt == null || userPrompt.isBlank()) {
                return SocraticRuleEngine.enforce("");
            }

            String systemInstruction =
                    "You are Socrates. You must NEVER give answers or direct advice. " +
                            "Respond ONLY with exactly 3 probing questions, numbered 1 to 3, to guide the user's thinking.";

            String prompt = systemInstruction + "\n\nUser: " + userPrompt + "\nSocrates:";
            if (model == null) {
                return SocraticRuleEngine.enforce("1. What assumptions are driving your current view?\n2. What counterexample or alternative perspective could challenge your thinking?\n3. What evidence would cause you to revise your conclusion?");
            }

            try {
                String response = model.generate(prompt);
                return SocraticRuleEngine.enforce(response);
            } catch (RuntimeException e) {
                return SocraticRuleEngine.enforce("1. What assumptions are driving your current view?\n2. What counterexample or alternative perspective could challenge your thinking?\n3. What evidence would cause you to revise your conclusion?");
            }
        });
    }
}