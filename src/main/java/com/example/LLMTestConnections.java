package com.example;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class LLMTestConnections {

    private static final String API_KEY = "s";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public static void main(String[] args) throws IOException {
        String prompt = "Generate an SQL query to find the top 5 employees with the highest sales last month.";

        OkHttpClient client = new OkHttpClient();

        // Create the request body
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.put(message);

        JSONObject json = new JSONObject();
        json.put("model", "gpt-4"); // Or use "gpt-3.5-turbo"
        json.put("messages", messages);
        json.put("temperature", 0.2);

        RequestBody body = RequestBody.create(
            json.toString(),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONObject responseJson = new JSONObject(responseBody);
                String sql = responseJson
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                System.out.println("Generated SQL Query:\n" + extractSqlQuery(sql));
            } else {
                System.err.println("Request failed: " + response);
            }
        }
    }
    public static String extractSqlQuery(String llmResponse) {
        int start = llmResponse.indexOf("```sql");
        int end = llmResponse.indexOf("```", start + 6);

        if (start != -1 && end != -1) {
            return llmResponse.substring(start + 6, end).trim();
        }
        return llmResponse.trim();
    }
}
