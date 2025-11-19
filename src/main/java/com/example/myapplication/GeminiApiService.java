package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApiService {
    
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
    private final String apiKey;
    private final OkHttpClient client;
    private final Gson gson;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    public interface RecipeCallback {
        void onSuccess(Recipe recipe);
        void onError(String error);
    }
    
    public GeminiApiService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void generateRecipe(String dishName, RecipeCallback callback) {
        executor.execute(() -> {
            try {
                String prompt = createPrompt(dishName);
                String response = callGeminiApi(prompt);
                Recipe recipe = parseRecipe(response, dishName);
                
                mainHandler.post(() -> callback.onSuccess(recipe));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
            }
        });
    }
    
    private String createPrompt(String dishName) {
        return "Generate a detailed recipe for " + dishName + ". " +
                "Respond ONLY with a valid JSON object in this exact format (no markdown, no extra text):\n" +
                "{\n" +
                "  \"name\": \"Recipe Name\",\n" +
                "  \"category\": \"Category (e.g., Italian, Indian, Dessert)\",\n" +
                "  \"difficulty\": \"Easy/Medium/Hard\",\n" +
                "  \"cookingTime\": 30,\n" +
                "  \"servings\": 4,\n" +
                "  \"ingredients\": [\"ingredient 1\", \"ingredient 2\"],\n" +
                "  \"instructions\": [\"step 1\", \"step 2\"],\n" +
                "  \"emoji\": \"🍝\"\n" +
                "}";
    }
    
    private String callGeminiApi(String prompt) throws IOException {
        GeminiRequest request = new GeminiRequest();
        request.contents = new ArrayList<>();
        
        GeminiContent content = new GeminiContent();
        content.parts = new ArrayList<>();
        
        GeminiPart part = new GeminiPart();
        part.text = prompt;
        content.parts.add(part);
        request.contents.add(content);
        
        String jsonRequest = gson.toJson(request);
        
        RequestBody body = RequestBody.create(
            jsonRequest,
            MediaType.parse("application/json")
        );
        
        Request httpRequest = new Request.Builder()
                .url(API_URL + "?key=" + apiKey)
                .post(body)
                .build();
        
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API call failed: " + response.code() + " - " + response.message());
            }
            
            String responseBody = response.body().string();
            GeminiResponse geminiResponse = gson.fromJson(responseBody, GeminiResponse.class);
            
            if (geminiResponse.candidates != null && !geminiResponse.candidates.isEmpty()) {
                GeminiCandidate candidate = geminiResponse.candidates.get(0);
                if (candidate.content != null && candidate.content.parts != null && !candidate.content.parts.isEmpty()) {
                    return candidate.content.parts.get(0).text;
                }
            }
            
            throw new IOException("No response from API");
        }
    }
    
    private Recipe parseRecipe(String jsonResponse, String dishName) {
        try {
            // Clean up the response - remove markdown code blocks if present
            String cleanJson = jsonResponse.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            
            RecipeJson recipeJson = gson.fromJson(cleanJson, RecipeJson.class);
            
            return new Recipe(
                (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
                recipeJson.name != null ? recipeJson.name : dishName,
                recipeJson.category != null ? recipeJson.category : "AI Generated",
                recipeJson.difficulty != null ? recipeJson.difficulty : "Medium",
                recipeJson.cookingTime > 0 ? recipeJson.cookingTime : 30,
                recipeJson.servings > 0 ? recipeJson.servings : 4,
                recipeJson.ingredients != null ? recipeJson.ingredients : new ArrayList<>(),
                recipeJson.instructions != null ? recipeJson.instructions : new ArrayList<>(),
                recipeJson.emoji != null ? recipeJson.emoji : "🤖"
            );
        } catch (Exception e) {
            // If parsing fails, create a basic recipe
            ArrayList<String> ingredients = new ArrayList<>();
            ingredients.add("See AI response for details");
            
            ArrayList<String> instructions = new ArrayList<>();
            instructions.add(jsonResponse);
            
            return new Recipe(
                (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
                dishName,
                "AI Generated",
                "Medium",
                30,
                4,
                ingredients,
                instructions,
                "🤖"
            );
        }
    }
    
    // Request/Response classes for Gemini API
    private static class GeminiRequest {
        List<GeminiContent> contents;
    }
    
    private static class GeminiContent {
        List<GeminiPart> parts;
    }
    
    private static class GeminiPart {
        String text;
    }
    
    private static class GeminiResponse {
        List<GeminiCandidate> candidates;
    }
    
    private static class GeminiCandidate {
        GeminiContent content;
    }
    
    private static class RecipeJson {
        String name;
        String category;
        String difficulty;
        @SerializedName("cookingTime")
        int cookingTime;
        int servings;
        ArrayList<String> ingredients;
        ArrayList<String> instructions;
        String emoji;
    }
}
