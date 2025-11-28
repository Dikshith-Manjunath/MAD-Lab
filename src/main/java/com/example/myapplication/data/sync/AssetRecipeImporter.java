package com.example.myapplication.data.sync;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.myapplication.data.RecipeDao;
import com.example.myapplication.data.RecipeEntity;
import com.example.myapplication.data.RecipeIngredient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Imports real recipes from an on-device JSON asset and persists them into Room.
 */
public final class AssetRecipeImporter {

    private static final String TAG = "AssetRecipeImporter";
    private static final String ASSET_FILE_NAME = "recipes.json";
    private static final int MIN_INGREDIENTS = 3;
    private static final int MIN_STEPS = 3;

    private final Gson gson = new Gson();

    public interface ProgressListener {
        void onProgress(int completed, int target);
    }

    public int importRecipes(Context context,
                             RecipeDao recipeDao,
                             int desiredCount,
                             ProgressListener listener) throws IOException {
        AssetManager assetManager = context.getAssets();
        List<RecipeEntity> selectedRecipes = new ArrayList<>();
        Set<String> seenTitles = new HashSet<>();
        boolean limitResults = desiredCount > 0;

        try (InputStream stream = assetManager.open(ASSET_FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                JsonObject json;
                try {
                    json = gson.fromJson(line, JsonObject.class);
                } catch (Exception parsingException) {
                    Log.w(TAG, "Skipping malformed recipe entry", parsingException);
                    continue;
                }

                RecipeEntity entity = convert(json);
                if (entity == null) {
                    continue;
                }

                String dedupeKey = entity.getName().toLowerCase(Locale.ROOT);
                if (!seenTitles.add(dedupeKey)) {
                    continue;
                }

                selectedRecipes.add(entity);

                if (limitResults && selectedRecipes.size() >= desiredCount) {
                    break;
                }
            }
        }

        recipeDao.deleteAll();

        int total = selectedRecipes.size();

        int inserted = 0;
        if (listener != null) {
            listener.onProgress(0, total);
        }
        for (RecipeEntity entity : selectedRecipes) {
            recipeDao.insert(entity);
            inserted++;
            if (listener != null) {
                listener.onProgress(inserted, total);
            }
        }
        return inserted;
    }

    private RecipeEntity convert(JsonObject json) {
        String name = optString(json, "recipe_title");
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        List<String> rawIngredients = toStringList(json.getAsJsonArray("ingredients"));
        List<String> instructions = toStringList(json.getAsJsonArray("directions"));

        if (rawIngredients.size() < MIN_INGREDIENTS || instructions.size() < MIN_STEPS) {
            return null;
        }

        List<RecipeIngredient> ingredients = new ArrayList<>(rawIngredients.size());
        for (String ingredientText : rawIngredients) {
            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setName(ingredientText.trim());
            ingredient.setQuantity(0);
            ingredient.setUnit("");
            ingredients.add(ingredient);
        }

        int stepCount = json.has("num_steps") ? json.get("num_steps").getAsInt() : instructions.size();
        int ingredientCount = json.has("num_ingredients") ? json.get("num_ingredients").getAsInt() : rawIngredients.size();

        String cuisine = resolveCuisine(json, rawIngredients);
        int calories = estimateCalories(rawIngredients);
        int cookingTime = estimateCookTime(stepCount, ingredientCount);
        String allergens = detectAllergens(rawIngredients);
        int servings = estimateServings(rawIngredients);
        String imageUrl = buildImageUrl(name, cuisine);

        return new RecipeEntity(
                name,
                cuisine,
                calories,
                cookingTime,
                allergens,
                ingredients,
                instructions,
                servings,
                "ic_recipe_placeholder",
                imageUrl
        );
    }

    private List<String> toStringList(JsonArray array) {
        if (array == null) {
            return new ArrayList<>();
        }
        List<String> values = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            if (element == null || element.isJsonNull()) {
                continue;
            }
            values.add(element.getAsString());
        }
        return values;
    }

    private String resolveCuisine(JsonObject json, List<String> ingredients) {
        StringBuilder builder = new StringBuilder();
        append(builder, optString(json, "recipe_title"));
        append(builder, optString(json, "subcategory"));
        append(builder, optString(json, "category"));
        append(builder, optString(json, "description"));

        String combined = builder.toString();
        String detected = detectCuisine(combined);
        if (detected == null) {
            for (String ingredient : ingredients) {
                detected = detectCuisine(ingredient);
                if (detected != null) {
                    break;
                }
            }
        }

        if (detected != null) {
            return detected;
        }

        String lower = combined.toLowerCase(Locale.ROOT);
        if (lower.contains("air fryer") || lower.contains("slow cooker") || lower.contains("instant pot")) {
            return "American";
        }
        if (lower.contains("bbq") || lower.contains("barbecue")) {
            return "American";
        }
        if (lower.contains("dessert") || lower.contains("cookie") || lower.contains("brownie")) {
            return "American";
        }

        String fallback = normalizeCuisine(optString(json, "subcategory"));
        if (!TextUtils.isEmpty(fallback)) {
            return fallback;
        }
        fallback = normalizeCuisine(optString(json, "category"));
        if (!TextUtils.isEmpty(fallback)) {
            return fallback;
        }
        return "International";
    }

    private String normalizeCuisine(String value) {
        if (TextUtils.isEmpty(value)) {
            return "International";
        }
        String cleaned = value.replace("Recipes", "").replace("Recipe", "").trim();
        if (cleaned.isEmpty()) {
            return "International";
        }
        return cleaned;
    }

    private void append(StringBuilder builder, String value) {
        if (!TextUtils.isEmpty(value)) {
            builder.append(' ').append(value);
        }
    }

    private String detectCuisine(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        for (CuisineProfile profile : CUISINE_PROFILES) {
            if (profile.matches(lower)) {
                return profile.label;
            }
        }
        return null;
    }

    private int estimateCalories(List<String> ingredients) {
        int base = 40 * ingredients.size();
        for (String ingredient : ingredients) {
            String lower = ingredient.toLowerCase(Locale.ROOT);
            if (containsAny(lower, "butter", "cheese", "cream", "bacon", "sausage", "oil")) {
                base += 80;
            }
            if (containsAny(lower, "sugar", "honey", "syrup", "molasses", "chocolate")) {
                base += 60;
            }
            if (containsAny(lower, "rice", "pasta", "noodle", "potato", "bread")) {
                base += 50;
            }
        }
        return clamp(base, 180, 950);
    }

    private int estimateCookTime(int stepCount, int ingredientCount) {
        int time = stepCount * 7 + ingredientCount * 2;
        return clamp(time, 15, 180);
    }

    private int estimateServings(List<String> ingredients) {
        int size = ingredients.size();
        if (size <= 6) {
            return 2;
        }
        if (size <= 12) {
            return 4;
        }
        if (size <= 20) {
            return 6;
        }
        return 8;
    }

    private String detectAllergens(List<String> ingredients) {
        Set<String> allergens = new LinkedHashSet<>();
        for (String ingredient : ingredients) {
            String lower = ingredient.toLowerCase(Locale.ROOT);
            if (containsAny(lower, "milk", "butter", "cream", "cheese", "yogurt")) {
                allergens.add("Dairy");
            }
            if (containsAny(lower, "egg")) {
                allergens.add("Eggs");
            }
            if (containsAny(lower, "peanut", "almond", "walnut", "cashew", "pecan", "pistachio", "hazelnut")) {
                allergens.add("Tree Nuts");
            }
            if (containsAny(lower, "shrimp", "prawn", "crab", "lobster", "clam", "mussel", "oyster")) {
                allergens.add("Shellfish");
            }
            if (containsAny(lower, "soy", "tofu", "edamame")) {
                allergens.add("Soy");
            }
            if (containsAny(lower, "wheat", "flour", "barley", "rye")) {
                allergens.add("Gluten");
            }
        }
        if (allergens.isEmpty()) {
            return "None";
        }
        return TextUtils.join(", ", allergens);
    }

    private String buildImageUrl(String name, String cuisine) {
        String query = name + " " + cuisine + " food";
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return "https://source.unsplash.com/600x400/?" + encoded;
    }

    private boolean containsAny(String source, String... needles) {
        for (String needle : needles) {
            if (source.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String optString(JsonObject json, String key) {
        if (json == null || key == null || !json.has(key)) {
            return null;
        }
        JsonElement element = json.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }

    private static final CuisineProfile[] CUISINE_PROFILES = new CuisineProfile[] {
            new CuisineProfile("Mexican", "mexican", "quesadilla", "taco", "enchilada", "salsa", "fajita", "guacamole", "pozole", "tamale"),
            new CuisineProfile("Italian", "italian", "parmesan", "risotto", "ricotta", "pasta", "gnocchi", "lasagna", "mozzarella", "pesto"),
            new CuisineProfile("Indian", "indian", "masala", "curry", "paneer", "tikka", "dal", "garam", "biriyani", "vindaloo"),
            new CuisineProfile("Chinese", "chinese", "stir-fry", "lo mein", "hoisin", "five-spice", "dumpling", "kung pao", "sesame oil"),
            new CuisineProfile("Japanese", "japanese", "teriyaki", "sushi", "ramen", "miso", "tonkatsu", "udon", "yakitori"),
            new CuisineProfile("Thai", "thai", "lemongrass", "tom yum", "curry paste", "galangal", "pad thai"),
            new CuisineProfile("Greek", "greek", "feta", "tzatziki", "gyro", "souvlaki", "baklava"),
            new CuisineProfile("Spanish", "spanish", "paella", "chorizo", "tapas", "gazpacho"),
            new CuisineProfile("French", "french", "ratatouille", "bechamel", "crepe", "bouillabaisse", "proven\u00e7al", "coq au vin"),
            new CuisineProfile("Mediterranean", "mediterranean", "falafel", "hummus", "tabbouleh", "shawarma", "sumac", "za'atar"),
            new CuisineProfile("Middle Eastern", "middle eastern", "persian", "turkish", "tagine", "harissa"),
            new CuisineProfile("Korean", "korean", "gochujang", "kimchi", "bulgogi", "bibimbap"),
            new CuisineProfile("Vietnamese", "vietnamese", "pho", "banh", "lemongrass", "nuoc mam"),
            new CuisineProfile("Caribbean", "caribbean", "jerk", "plantain", "mojo"),
            new CuisineProfile("Brazilian", "brazilian", "feijoada", "picanha", "brigadeiro"),
            new CuisineProfile("German", "german", "sauerkraut", "bratwurst", "spaetzle"),
            new CuisineProfile("Turkish", "turkish", "doner", "baklava", "pide"),
            new CuisineProfile("American", "american", "barbecue", "bbq", "cornbread", "cajun", "southern", "gumbo", "burger", "hot dog"),
            new CuisineProfile("African", "african", "ethiopian", "moroccan", "berbere", "jollof", "peri-peri")
    };

    private static final class CuisineProfile {
        private final String label;
        private final String[] keywords;

        private CuisineProfile(String label, String... keywords) {
            this.label = label;
            this.keywords = keywords;
        }

        private boolean matches(String value) {
            for (String keyword : keywords) {
                if (value.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }
    }
}
