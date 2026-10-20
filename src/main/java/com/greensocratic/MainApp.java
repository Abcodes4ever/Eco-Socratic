package com.greensocratic;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private final LLMService llmService = new LLMService();

    @Override
    public void start(Stage stage) {
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(320);
        chatArea.setText("Socrates: What topic shall we examine today?\n\n");

        TextField inputField = new TextField();
        inputField.setPromptText("Type your thought or programming question...");

        Button sendBtn = new Button("Ask Socrates");
        sendBtn.setOnAction(e -> {
            String userText = inputField.getText().trim();
            if (userText.isEmpty()) {
                return;
            }

            chatArea.appendText("You: " + userText + "\n\n");
            inputField.clear();

            llmService.askSocratesAsync(userText)
                    .thenAccept(response -> {
                        chatArea.appendText("Socrates:\n" + response + "\n\n");
                    })
                    .exceptionally(ex -> {
                        chatArea.appendText("Socrates:\n" + SocraticRuleEngine.enforce(
                                "1. What assumptions are driving your current view?\n" +
                                "2. What counterexample or alternative perspective could challenge your thinking?\n" +
                                "3. What evidence would cause you to revise your conclusion?\n") + "\n\n");
                        return null;
                    });
        });

        VBox root = new VBox(10, chatArea, inputField, sendBtn);
        root.setPadding(new Insets(15));

        stage.setTitle("Green Socratic Desktop");
        stage.setScene(new Scene(root, 500, 450));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
