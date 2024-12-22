package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

@Service
public class HomeService {

    public String getResponseFromGeminiModel(String managerName, String leaveDate, String excuseLevel) {
        String response = "";

        // Gemini API details
        String apiKey = "AIzaSyBB30ZfjPi3dG9YMdmy7s2lqGFvpxf7yBs";
        String modelId = "gemini-1.5-flash-latest";
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + modelId + ":generateContent?key=" + apiKey;

        // Define the system prompt
        String systemPrompt = """
            Your objective is to create an exciting and convincing leave excuse for my manager that does not include any sickness reasons. 
            The excuse should be:
            1. Creative , funny and unique.
            2. Don't give any health related excuse .
            3. Short and professional.
            4. Relevant to the specified excuse level (1, 2, or 3). Higher levels indicate more serious excuses.
            The response must be directly addressed to the manager and include a clear explanation for the leave.
            """;

        // Define the user prompt
        String userPrompt = "Manager name is: " + managerName + ", leave date: " + leaveDate + ", and excuse level: " + excuseLevel + ".";

        // Combine system and user prompts
        String combinedPrompt = systemPrompt + "\n\n" + userPrompt;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            response = sendRequestToGemini(httpClient, apiUrl, combinedPrompt);
            System.out.println("Generated Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private String sendRequestToGemini(CloseableHttpClient httpClient, String apiUrl, String prompt) throws Exception {
        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Content-Type", "application/json");

        // Create JSON payload using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        ArrayNode contentsArray = objectMapper.createArrayNode();
        ObjectNode partsObject = objectMapper.createObjectNode();
        partsObject.put("text", prompt);

        ObjectNode contentsObject = objectMapper.createObjectNode();
        contentsObject.set("parts", objectMapper.createArrayNode().add(partsObject));
        contentsArray.add(contentsObject);

        payload.set("contents", contentsArray);

        request.setEntity(new StringEntity(objectMapper.writeValueAsString(payload)));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());

            // Debugging: Log raw response
            System.out.println("Raw Response: " + responseBody);

            // Parse response using Jackson
            ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(responseBody);

            if (jsonResponse.has("candidates")) {
                ArrayNode candidates = (ArrayNode) jsonResponse.get("candidates");
                if (!candidates.isEmpty()) {
                    ObjectNode candidate = (ObjectNode) candidates.get(0);
                    ObjectNode content = (ObjectNode) candidate.get("content");
                    ArrayNode parts = (ArrayNode) content.get("parts");

                    if (!parts.isEmpty()) {
                        return parts.get(0).get("text").asText().trim();
                    }
                }
            } else {
                System.err.println("Unexpected response format: " + responseBody);
            }
        }

        return "Failed to generate response from the Gemini model.";
    }
}
