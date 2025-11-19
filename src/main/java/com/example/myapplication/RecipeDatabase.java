package com.example.myapplication;

import java.util.ArrayList;
import java.util.Arrays;

public class RecipeDatabase {
    
    private static ArrayList<Recipe> aiGeneratedRecipes = new ArrayList<>();
    
    public static ArrayList<Recipe> getRecipes() {
        ArrayList<Recipe> recipes = new ArrayList<>();
        
        // 1. Spaghetti Carbonara
        recipes.add(new Recipe(
            1,
            "Spaghetti Carbonara",
            "Italian",
            "Medium",
            25,
            4,
            new ArrayList<>(Arrays.asList(
                "400g spaghetti",
                "200g pancetta or guanciale",
                "4 large eggs",
                "100g Pecorino Romano cheese",
                "Black pepper",
                "Salt"
            )),
            new ArrayList<>(Arrays.asList(
                "Cook spaghetti in salted boiling water until al dente",
                "Meanwhile, cut pancetta into small cubes and fry until crispy",
                "Beat eggs with grated Pecorino and black pepper",
                "Drain pasta, reserving 1 cup of pasta water",
                "Remove pan from heat, add pasta to pancetta",
                "Quickly stir in egg mixture, adding pasta water to create creamy sauce",
                "Serve immediately with extra cheese and pepper"
            )),
            "🍝"
        ));
        
        // 2. Chicken Tikka Masala
        recipes.add(new Recipe(
            2,
            "Chicken Tikka Masala",
            "Indian",
            "Hard",
            45,
            6,
            new ArrayList<>(Arrays.asList(
                "800g boneless chicken thighs",
                "1 cup yogurt",
                "2 tbsp tikka masala spice",
                "400ml tomato puree",
                "200ml heavy cream",
                "3 cloves garlic, minced",
                "1 inch ginger, grated",
                "2 onions, diced",
                "2 tbsp vegetable oil",
                "Fresh cilantro",
                "Salt to taste"
            )),
            new ArrayList<>(Arrays.asList(
                "Marinate chicken in yogurt and half the tikka masala spice for 30 minutes",
                "Grill or pan-fry chicken pieces until charred",
                "In a large pan, sauté onions until golden",
                "Add garlic, ginger, and remaining spices",
                "Pour in tomato puree and simmer for 10 minutes",
                "Add chicken pieces and cream",
                "Simmer for 15 minutes until chicken is cooked through",
                "Garnish with cilantro and serve with rice or naan"
            )),
            "🍛"
        ));
        
        // 3. Caesar Salad
        recipes.add(new Recipe(
            3,
            "Caesar Salad",
            "Salad",
            "Easy",
            15,
            4,
            new ArrayList<>(Arrays.asList(
                "1 large romaine lettuce",
                "1 cup croutons",
                "1/2 cup Parmesan cheese, shaved",
                "2 cloves garlic",
                "2 anchovy fillets",
                "1 egg yolk",
                "2 tbsp lemon juice",
                "1 tsp Dijon mustard",
                "1/2 cup olive oil",
                "Salt and pepper"
            )),
            new ArrayList<>(Arrays.asList(
                "Wash and chop romaine lettuce into bite-sized pieces",
                "For dressing: blend garlic, anchovies, egg yolk, lemon juice, and mustard",
                "Slowly drizzle in olive oil while blending",
                "Season with salt and pepper",
                "Toss lettuce with dressing",
                "Top with croutons and Parmesan shavings",
                "Serve immediately"
            )),
            "🥗"
        ));
        
        // 4. Chocolate Chip Cookies
        recipes.add(new Recipe(
            4,
            "Chocolate Chip Cookies",
            "Dessert",
            "Easy",
            30,
            24,
            new ArrayList<>(Arrays.asList(
                "2 1/4 cups all-purpose flour",
                "1 tsp baking soda",
                "1 tsp salt",
                "1 cup butter, softened",
                "3/4 cup granulated sugar",
                "3/4 cup brown sugar",
                "2 large eggs",
                "2 tsp vanilla extract",
                "2 cups chocolate chips"
            )),
            new ArrayList<>(Arrays.asList(
                "Preheat oven to 375°F (190°C)",
                "Mix flour, baking soda, and salt in a bowl",
                "Beat butter and sugars until creamy",
                "Add eggs and vanilla, beat well",
                "Gradually stir in flour mixture",
                "Fold in chocolate chips",
                "Drop rounded tablespoons onto ungreased cookie sheets",
                "Bake 9-11 minutes until golden brown",
                "Cool on baking sheet for 2 minutes, then transfer to wire rack"
            )),
            "🍪"
        ));
        
        // 5. Tacos al Pastor
        recipes.add(new Recipe(
            5,
            "Tacos al Pastor",
            "Mexican",
            "Medium",
            35,
            6,
            new ArrayList<>(Arrays.asList(
                "1kg pork shoulder, thinly sliced",
                "3 dried guajillo chiles",
                "2 dried ancho chiles",
                "1/2 cup pineapple juice",
                "3 cloves garlic",
                "1 tbsp achiote paste",
                "1 tsp cumin",
                "1/2 cup white vinegar",
                "Fresh pineapple chunks",
                "Corn tortillas",
                "Cilantro and onion for topping",
                "Lime wedges"
            )),
            new ArrayList<>(Arrays.asList(
                "Remove seeds from dried chiles and soak in hot water for 15 minutes",
                "Blend chiles with pineapple juice, garlic, achiote, cumin, and vinegar",
                "Marinate pork in the chile mixture for at least 2 hours",
                "Grill or pan-fry pork until slightly charred",
                "Grill pineapple chunks until caramelized",
                "Warm tortillas on the grill",
                "Assemble tacos with pork, pineapple, cilantro, and onion",
                "Serve with lime wedges"
            )),
            "🌮"
        ));
        
        // 6. Greek Moussaka
        recipes.add(new Recipe(
            6,
            "Greek Moussaka",
            "Greek",
            "Hard",
            90,
            8,
            new ArrayList<>(Arrays.asList(
                "3 large eggplants, sliced",
                "500g ground lamb or beef",
                "1 onion, diced",
                "3 cloves garlic, minced",
                "400g canned tomatoes",
                "2 tbsp tomato paste",
                "1 tsp cinnamon",
                "1/2 cup red wine",
                "4 tbsp butter",
                "4 tbsp flour",
                "2 cups milk",
                "2 eggs",
                "1 cup grated cheese",
                "Olive oil",
                "Salt and pepper"
            )),
            new ArrayList<>(Arrays.asList(
                "Salt eggplant slices and let drain for 30 minutes, then rinse and pat dry",
                "Brush eggplant with oil and bake at 400°F until golden",
                "Sauté onion and garlic, add meat and brown",
                "Add tomatoes, tomato paste, wine, cinnamon, salt, and pepper. Simmer 30 minutes",
                "Make béchamel: melt butter, add flour, gradually whisk in milk",
                "Cook until thick, remove from heat, stir in beaten eggs",
                "Layer: meat sauce, eggplant, repeat, top with béchamel and cheese",
                "Bake at 350°F for 45 minutes until golden",
                "Let rest 15 minutes before serving"
            )),
            "🍆"
        ));
        
        // 7. Pad Thai
        recipes.add(new Recipe(
            7,
            "Pad Thai",
            "Thai",
            "Medium",
            30,
            4,
            new ArrayList<>(Arrays.asList(
                "200g rice noodles",
                "200g shrimp or chicken",
                "2 eggs",
                "3 tbsp fish sauce",
                "2 tbsp tamarind paste",
                "2 tbsp palm sugar",
                "2 cloves garlic, minced",
                "1 cup bean sprouts",
                "3 green onions, chopped",
                "1/4 cup roasted peanuts, crushed",
                "Lime wedges",
                "Vegetable oil"
            )),
            new ArrayList<>(Arrays.asList(
                "Soak rice noodles in warm water for 30 minutes, then drain",
                "Mix fish sauce, tamarind paste, and palm sugar for the sauce",
                "Heat oil in a wok over high heat",
                "Stir-fry garlic briefly, add shrimp/chicken and cook through",
                "Push ingredients to the side, scramble eggs",
                "Add noodles and sauce, toss well",
                "Add bean sprouts and green onions, stir-fry for 2 minutes",
                "Serve topped with crushed peanuts and lime wedges"
            )),
            "🍜"
        ));
        
        // 8. Margherita Pizza
        recipes.add(new Recipe(
            8,
            "Margherita Pizza",
            "Italian",
            "Medium",
            90,
            2,
            new ArrayList<>(Arrays.asList(
                "2 cups all-purpose flour",
                "1 tsp instant yeast",
                "1 tsp salt",
                "3/4 cup warm water",
                "1 tbsp olive oil",
                "1 cup tomato sauce",
                "200g fresh mozzarella",
                "Fresh basil leaves",
                "Extra virgin olive oil"
            )),
            new ArrayList<>(Arrays.asList(
                "Mix flour, yeast, and salt in a bowl",
                "Add warm water and olive oil, knead for 10 minutes",
                "Let dough rise for 1 hour until doubled",
                "Preheat oven to 475°F (245°C) with pizza stone",
                "Roll out dough into a circle",
                "Spread tomato sauce, leaving a border",
                "Top with torn mozzarella and basil",
                "Drizzle with olive oil",
                "Bake for 10-12 minutes until crust is golden and cheese is bubbly"
            )),
            "🍕"
        ));
        
        // 9. Beef Stir Fry
        recipes.add(new Recipe(
            9,
            "Beef Stir Fry",
            "Chinese",
            "Easy",
            20,
            4,
            new ArrayList<>(Arrays.asList(
                "500g beef sirloin, thinly sliced",
                "2 bell peppers, sliced",
                "1 onion, sliced",
                "2 cups broccoli florets",
                "3 cloves garlic, minced",
                "1 inch ginger, grated",
                "3 tbsp soy sauce",
                "2 tbsp oyster sauce",
                "1 tbsp cornstarch",
                "2 tbsp vegetable oil",
                "Sesame seeds for garnish"
            )),
            new ArrayList<>(Arrays.asList(
                "Marinate beef in soy sauce and cornstarch for 15 minutes",
                "Heat oil in a wok over high heat",
                "Stir-fry beef in batches until browned, set aside",
                "Stir-fry garlic and ginger until fragrant",
                "Add vegetables and stir-fry for 3-4 minutes",
                "Return beef to wok",
                "Add oyster sauce and toss everything together",
                "Garnish with sesame seeds and serve with rice"
            )),
            "🥘"
        ));
        
        // 10. French Onion Soup
        recipes.add(new Recipe(
            10,
            "French Onion Soup",
            "French",
            "Medium",
            60,
            6,
            new ArrayList<>(Arrays.asList(
                "6 large onions, thinly sliced",
                "4 tbsp butter",
                "2 cloves garlic, minced",
                "2 tbsp flour",
                "1/2 cup dry white wine",
                "8 cups beef stock",
                "2 bay leaves",
                "Fresh thyme",
                "French baguette, sliced",
                "2 cups Gruyère cheese, grated",
                "Salt and pepper"
            )),
            new ArrayList<>(Arrays.asList(
                "Melt butter in a large pot over medium heat",
                "Add onions and cook slowly for 40 minutes until deeply caramelized",
                "Add garlic and flour, stir for 1 minute",
                "Pour in wine and scrape bottom of pot",
                "Add stock, bay leaves, and thyme. Simmer 20 minutes",
                "Season with salt and pepper",
                "Toast baguette slices",
                "Ladle soup into oven-safe bowls",
                "Top with bread and cheese, broil until cheese is melted and golden"
            )),
            "🍲"
        ));
        
        // 11. Sushi Rolls
        recipes.add(new Recipe(
            11,
            "California Sushi Rolls",
            "Japanese",
            "Hard",
            45,
            4,
            new ArrayList<>(Arrays.asList(
                "2 cups sushi rice",
                "3 tbsp rice vinegar",
                "1 tbsp sugar",
                "4 nori sheets",
                "1 avocado, sliced",
                "1 cucumber, julienned",
                "200g imitation crab",
                "2 tbsp sesame seeds",
                "Soy sauce for serving",
                "Wasabi and pickled ginger"
            )),
            new ArrayList<>(Arrays.asList(
                "Cook sushi rice according to package directions",
                "Mix rice vinegar and sugar, fold into cooked rice",
                "Let rice cool to room temperature",
                "Place nori on bamboo mat, spread rice leaving 1 inch at top",
                "Arrange cucumber, avocado, and crab in a line",
                "Roll tightly using the bamboo mat",
                "Sprinkle with sesame seeds",
                "Cut into 8 pieces with a sharp wet knife",
                "Serve with soy sauce, wasabi, and pickled ginger"
            )),
            "🍣"
        ));
        
        // 12. Banana Bread
        recipes.add(new Recipe(
            12,
            "Banana Bread",
            "Dessert",
            "Easy",
            70,
            10,
            new ArrayList<>(Arrays.asList(
                "3 ripe bananas, mashed",
                "1/3 cup melted butter",
                "3/4 cup sugar",
                "1 egg, beaten",
                "1 tsp vanilla extract",
                "1 tsp baking soda",
                "Pinch of salt",
                "1 1/2 cups all-purpose flour",
                "Optional: 1/2 cup chopped walnuts"
            )),
            new ArrayList<>(Arrays.asList(
                "Preheat oven to 350°F (175°C)",
                "Mix melted butter with mashed bananas",
                "Stir in sugar, egg, and vanilla",
                "Add baking soda and salt",
                "Mix in flour until just combined",
                "Fold in walnuts if using",
                "Pour into greased 9x5 inch loaf pan",
                "Bake for 60-65 minutes until toothpick comes out clean",
                "Cool in pan for 10 minutes, then turn out onto wire rack"
            )),
            "🍌"
        ));
        
        // Add AI-generated recipes
        recipes.addAll(aiGeneratedRecipes);
        
        return recipes;
    }
    
    public static void addAiRecipe(Recipe recipe) {
        aiGeneratedRecipes.add(recipe);
    }
    
    public static void clearAiRecipes() {
        aiGeneratedRecipes.clear();
    }
}
