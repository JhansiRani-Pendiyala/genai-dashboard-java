package com.example.service;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
@Slf4j
public class LLMService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final JdbcTemplate jdbcTemplate;
    
    @Value("${openai.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public LLMService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public String generateSQL(String userPrompt) throws Exception {
    	String schemaContext = EntitySchemaScanner.scanSchema("com.example.entity");

    	String systemPrompt = "You are a helpful assistant that generates exactly one valid PostgreSQL SQL query based on the schema and user request. " + schemaContext;

    	String fullPrompt = "Generate a single SQL query for this request: " + userPrompt;

    	String json = mapper.writeValueAsString(Map.of(
    	    "model", "gpt-4",
    	    "messages", List.of(
    	        Map.of("role", "system", "content", systemPrompt),
    	        Map.of("role", "user", "content", fullPrompt)
    	    ),
    	    "temperature", 0,
    	    "n", 1,
    	    "max_tokens", 500
    	));

    	Request request = new Request.Builder()
    	    .url("https://api.openai.com/v1/chat/completions")
    	    .header("Authorization", "Bearer " + apiKey)
    	    .post(RequestBody.create(json, okhttp3.MediaType.parse("application/json")))
    	    .build();

    	try (Response response = client.newCall(request).execute()) {
    	    if (!response.isSuccessful()) {
    	        throw new RuntimeException("OpenAI error: " + response.body().string());
    	    }

    	    JsonNode body = mapper.readTree(response.body().string());
    	    return extractSqlQuery(body.get("choices").get(0).get("message").get("content").asText().trim());
    	}
    }

    private String extractSqlQuery(String llmResponse) {
        if (llmResponse == null) {
            return "";
        }

        int start = llmResponse.indexOf("```sql");
        if (start != -1) {
            int end = llmResponse.indexOf("```", start + 6);
            if (end != -1) {
                return llmResponse.substring(start + 6, end).trim();
            }
        }
        return llmResponse.trim();
    }

	public List<Map<String, Object>> executeSQL(String sql) {
		
		
		
		System.out.println("*********** {}" +sql );
        List<Map<String, Object>> rawList = jdbcTemplate.queryForList(sql);

		
		List<Map<String, Object>> resultList = new ArrayList<>();

        for (Map<String, Object> row : rawList) {
            Map<String, Object> processedRow = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                Object value = entry.getValue();

                if (value instanceof Array) {
                    try {
                        Array sqlArray = (Array) value;
                        Object arrayObj = sqlArray.getArray();

                        if (arrayObj instanceof Object[]) {
                            Object[] arr = (Object[]) arrayObj;
                            processedRow.put(entry.getKey(), Arrays.asList(arr));
                        } else {
                            processedRow.put(entry.getKey(), arrayObj);
                        }
                    } catch (Exception e) {
                        processedRow.put(entry.getKey(), null);
                    }
                } else {
                    processedRow.put(entry.getKey(), value);
                }
            }
            resultList.add(processedRow);
        }

        return resultList;
    }
}
