package com.greensocratic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocraticRuleEngine {

    private static final Pattern QUESTION_PATTERN = Pattern.compile("(?im)^\\s*\\d+[.)]\\s*(.+?)(?:\\?+|$)\\s*$");

    public static List<String> extractQuestions(String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) {
            return List.of();
        }

        List<String> questions = new ArrayList<>();
        Matcher matcher = QUESTION_PATTERN.matcher(rawOutput);
        while (matcher.find()) {
            String question = matcher.group(1).trim();
            if (!question.isEmpty() && !question.endsWith("?")) {
               question += "?";
            }
            questions.add(question);
        }
        return questions;
    }

    public static String enforce(String rawOutput) {
        List<String> questions = extractQuestions(rawOutput);

        if (questions.size() >= 3) {
            return String.format("1. %s\n2. %s\n3. %s", questions.get(0), questions.get(1), questions.get(2));
        }

        return "Reflect on these core dimensions:\n" +
               "1. What is the fundamental assumption behind your reasoning?\n" +
               "2. How would an edge case or counterexample challenge this view?\n" +
               "3. What empirical evidence would change your mind?";
    }
}