package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;

import com.example.myapplication.data.RecipeIngredient;
import com.example.myapplication.models.Recipe;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                "  \"cuisine\": \"Cuisine or origin\",\n" +
                "  \"calories\": 480,\n" +
                "  \"cookingTime\": 30,\n" +
                "  \"allergens\": \"Contains dairy, nuts\",\n" +
                "  \"servings\": 4,\n" +
                "  \"ingredients\": [\n" +
                "    {\n" +
                "      \"name\": \"ingredient name\",\n" +
                "      \"quantity\": 1.5,\n" +
                "      \"unit\": \"cups\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"instructions\": [\"step 1\", \"step 2\"]\n" +
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
            String cleanJson = sanitizeResponse(jsonResponse);
            JsonElement element = JsonParser.parseString(cleanJson);
            if (!element.isJsonObject()) {
                throw new IllegalStateException("Invalid JSON payload");
            }

            JsonObject json = element.getAsJsonObject();

            String name = getString(json, "name", dishName);
            String cuisine = getString(json, "cuisine", "AI Fusion");
            int calories = getInt(json, "calories", 400);
            int cookingTime = getInt(json, "cookingTime", 30);
            String allergens = getString(json, "allergens", "Check ingredients");
            int servings = getInt(json, "servings", 4);

            List<RecipeIngredient> ingredients = parseIngredients(json);
            List<String> instructions = parseInstructions(json);

            if (ingredients.isEmpty()) {
                ingredients = Collections.singletonList(new RecipeIngredient(dishName + " ingredient", 0, ""));
            }
            if (instructions.isEmpty()) {
                instructions = Collections.singletonList("Refer to AI notes for preparation steps.");
            }

            return new Recipe(
                    0,
                    name,
                    cuisine,
                    calories,
                    cookingTime,
                    allergens,
                    servings,
                    ingredients,
                    instructions,
                    null,
                    "ic_recipe_placeholder"
            );
        } catch (Exception e) {
            ArrayList<RecipeIngredient> ingredients = new ArrayList<>();
            ingredients.add(new RecipeIngredient("See AI response for details", 0, ""));

            ArrayList<String> instructions = new ArrayList<>();
            instructions.add(cleanMarkdown(jsonResponse));

            return new Recipe(
                0,
                dishName,
                "AI Fusion",
                400,
                30,
                "Check ingredients",
                4,
                ingredients,
                instructions,
                null,
                "ic_recipe_placeholder"
            );
        }
    }

    private String sanitizeResponse(String input) {
        String clean = cleanMarkdown(input == null ? "" : input).trim();
        if (clean.startsWith("{")) {
            return clean;
        }
        int firstBrace = clean.indexOf('{');
        int lastBrace = clean.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return clean.substring(firstBrace, lastBrace + 1);
        }
        return clean;
    }

    private String cleanMarkdown(String text) {
        if (text == null) {
            return "";
        }
        String clean = text.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        }
        if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        return clean.trim();
    }

    private String getString(JsonObject json, String key, String fallback) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsString();
        }
        return fallback;
    }

    private int getInt(JsonObject json, String key, int fallback) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            try {
                return json.get(key).getAsInt();
            } catch (NumberFormatException e) {
                return fallback;
            }
        }
        return fallback;
    }

    private List<RecipeIngredient> parseIngredients(JsonObject json) {
        if (!json.has("ingredients")) {
            return new ArrayList<>();
        }

        JsonElement element = json.get("ingredients");
        List<RecipeIngredient> results = new ArrayList<>();

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                if (item.isJsonObject()) {
                    JsonObject obj = item.getAsJsonObject();
                    String name = obj.has("name") ? obj.get("name").getAsString() : "";
                    double quantity = obj.has("quantity") && obj.get("quantity").isJsonPrimitive()
                            ? safeDouble(obj.get("quantity")) : 0;
                    String unit = obj.has("unit") ? obj.get("unit").getAsString() : "";
                    results.add(new RecipeIngredient(name, quantity, unit));
                } else if (item.isJsonPrimitive()) {
                    results.add(parseIngredientLine(item.getAsString()));
                }
            }
        } else if (element.isJsonPrimitive()) {
            results.add(parseIngredientLine(element.getAsString()));
        }

        return results;
    }

    private List<String> parseInstructions(JsonObject json) {
        if (!json.has("instructions")) {
            return new ArrayList<>();
        }
        JsonElement element = json.get("instructions");
        List<String> instructions = new ArrayList<>();
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                instructions.add(item.getAsString());
            }
        } else if (element.isJsonPrimitive()) {
            instructions.add(element.getAsString());
        }
        return instructions;
    }

    private double safeDouble(JsonElement element) {
        try {
            return element.getAsDouble();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private RecipeIngredient parseIngredientLine(String line) {
        if (line == null) {
            return new RecipeIngredient("Ingredient", 0, "");
        }
        String cleaned = line.replace("•", "").replace("-", "").trim();
        Pattern pattern = Pattern.compile("^(\\d+(?:[\\./]\\d+)?)(?:\\s+([a-zA-Z]+))?\\s+(.*)$");
        Matcher matcher = pattern.matcher(cleaned);
        if (matcher.find()) {
            double quantity = parseQuantity(matcher.group(1));
            String unit = matcher.group(2) != null ? matcher.group(2) : "";
            String name = matcher.group(3) != null ? matcher.group(3) : cleaned;
            return new RecipeIngredient(name, quantity, unit);
        }
        return new RecipeIngredient(cleaned, 0, "");
    }

    private double parseQuantity(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        if (text.contains("/")) {
            String[] parts = text.split("/");
            if (parts.length == 2) {
                try {
                    double numerator = Double.parseDouble(parts[0]);
                    double denominator = Double.parseDouble(parts[1]);
                    if (denominator != 0) {
                        return numerator / denominator;
                    }
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
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
}
