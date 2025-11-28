package com.example.myapplication.utils;

import com.google.mlkit.vision.label.ImageLabel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class IngredientLabelMapper {

    private static final Map<String, String> DIRECT_MAPPINGS = Map.ofEntries(
            Map.entry("tomato", "tomato"),
            Map.entry("onion", "onion"),
            Map.entry("garlic", "garlic"),
            Map.entry("carrot", "carrot"),
            Map.entry("pepper", "bell pepper"),
            Map.entry("cucumber", "cucumber"),
            Map.entry("lettuce", "lettuce"),
            Map.entry("cabbage", "cabbage"),
            Map.entry("beef", "beef"),
            Map.entry("pork", "pork"),
            Map.entry("chicken", "chicken"),
            Map.entry("fish", "fish"),
            Map.entry("shrimp", "shrimp"),
            Map.entry("salmon", "salmon"),
            Map.entry("bread", "bread"),
            Map.entry("cheese", "cheese"),
            Map.entry("butter", "butter"),
            Map.entry("egg", "egg"),
            Map.entry("milk", "milk"),
            Map.entry("cream", "cream"),
            Map.entry("potato", "potato"),
            Map.entry("spinach", "spinach"),
            Map.entry("broccoli", "broccoli"),
            Map.entry("mushroom", "mushroom"),
            Map.entry("herb", "fresh herbs"),
            Map.entry("basil", "basil"),
            Map.entry("parsley", "parsley"),
            Map.entry("cilantro", "cilantro"),
            Map.entry("rice", "rice"),
            Map.entry("noodle", "noodles"),
            Map.entry("pasta", "pasta"),
            Map.entry("apple", "apple"),
            Map.entry("banana", "banana"),
            Map.entry("lemon", "lemon"),
            Map.entry("lime", "lime"),
            Map.entry("orange", "orange"),
            Map.entry("strawberry", "strawberry"),
            Map.entry("blueberry", "blueberries"),
            Map.entry("raspberry", "raspberries"),
            Map.entry("grape", "grapes"),
            Map.entry("pineapple", "pineapple"),
            Map.entry("corn", "corn"),
            Map.entry("bean", "beans"),
            Map.entry("pea", "peas"),
            Map.entry("almond", "almonds"),
            Map.entry("walnut", "walnuts"),
            Map.entry("peanut", "peanuts")
    );

    private IngredientLabelMapper() {
    }

    public static List<String> mapLabelsToIngredients(List<ImageLabel> labels) {
        Set<String> results = new LinkedHashSet<>();
        if (labels == null) {
            return new ArrayList<>();
        }
        for (ImageLabel label : labels) {
            String text = label.getText();
            if (text == null) {
                continue;
            }
            String lower = text.toLowerCase(Locale.ROOT);
            DIRECT_MAPPINGS.forEach((key, ingredient) -> {
                if (lower.contains(key) && results.size() < 12) {
                    results.add(ingredient);
                }
            });
            if (results.size() >= 12) {
                break;
            }
        }
        return new ArrayList<>(results);
    }
}
