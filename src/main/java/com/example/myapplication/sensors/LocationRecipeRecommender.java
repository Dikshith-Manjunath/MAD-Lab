package com.example.myapplication.sensors;

import android.location.Address;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class LocationRecipeRecommender {

    private static final Map<String, String> COUNTRY_TO_CUISINE = new HashMap<>();

    static {
        COUNTRY_TO_CUISINE.put("india", "Indian");
        COUNTRY_TO_CUISINE.put("united states", "American");
        COUNTRY_TO_CUISINE.put("usa", "American");
        COUNTRY_TO_CUISINE.put("united kingdom", "British");
        COUNTRY_TO_CUISINE.put("mexico", "Mexican");
        COUNTRY_TO_CUISINE.put("italy", "Italian");
        COUNTRY_TO_CUISINE.put("france", "French");
        COUNTRY_TO_CUISINE.put("china", "Chinese");
        COUNTRY_TO_CUISINE.put("japan", "Japanese");
        COUNTRY_TO_CUISINE.put("thailand", "Thai");
        COUNTRY_TO_CUISINE.put("spain", "Spanish");
        COUNTRY_TO_CUISINE.put("germany", "German");
        COUNTRY_TO_CUISINE.put("canada", "Canadian");
        COUNTRY_TO_CUISINE.put("australia", "Australian");
        COUNTRY_TO_CUISINE.put("brazil", "Brazilian");
        COUNTRY_TO_CUISINE.put("greece", "Greek");
        COUNTRY_TO_CUISINE.put("turkey", "Turkish");
        COUNTRY_TO_CUISINE.put("morocco", "Moroccan");
        COUNTRY_TO_CUISINE.put("vietnam", "Vietnamese");
        COUNTRY_TO_CUISINE.put("korea", "Korean");
        COUNTRY_TO_CUISINE.put("south korea", "Korean");
        COUNTRY_TO_CUISINE.put("indonesia", "Indonesian");
        COUNTRY_TO_CUISINE.put("philippines", "Filipino");
        COUNTRY_TO_CUISINE.put("malaysia", "Malaysian");
        COUNTRY_TO_CUISINE.put("singapore", "Peranakan");
        COUNTRY_TO_CUISINE.put("pakistan", "Pakistani");
        COUNTRY_TO_CUISINE.put("bangladesh", "Bangladeshi");
        COUNTRY_TO_CUISINE.put("sri lanka", "Sri Lankan");
        COUNTRY_TO_CUISINE.put("nepal", "Nepalese");
        COUNTRY_TO_CUISINE.put("russia", "Russian");
        COUNTRY_TO_CUISINE.put("ukraine", "Ukrainian");
        COUNTRY_TO_CUISINE.put("poland", "Polish");
        COUNTRY_TO_CUISINE.put("sweden", "Swedish");
        COUNTRY_TO_CUISINE.put("norway", "Norwegian");
        COUNTRY_TO_CUISINE.put("finland", "Finnish");
        COUNTRY_TO_CUISINE.put("denmark", "Danish");
        COUNTRY_TO_CUISINE.put("argentina", "Argentinian");
        COUNTRY_TO_CUISINE.put("peru", "Peruvian");
        COUNTRY_TO_CUISINE.put("chile", "Chilean");
        COUNTRY_TO_CUISINE.put("colombia", "Colombian");
        COUNTRY_TO_CUISINE.put("venezuela", "Venezuelan");
        COUNTRY_TO_CUISINE.put("egypt", "Egyptian");
        COUNTRY_TO_CUISINE.put("mauritania", "North African");
        COUNTRY_TO_CUISINE.put("tunisia", "Tunisian");
        COUNTRY_TO_CUISINE.put("algeria", "Algerian");
        COUNTRY_TO_CUISINE.put("ethiopia", "Ethiopian");
        COUNTRY_TO_CUISINE.put("kenya", "Kenyan");
        COUNTRY_TO_CUISINE.put("nigeria", "Nigerian");
        COUNTRY_TO_CUISINE.put("ghana", "Ghanaian");
        COUNTRY_TO_CUISINE.put("south africa", "South African");
        COUNTRY_TO_CUISINE.put("lebanon", "Lebanese");
        COUNTRY_TO_CUISINE.put("israel", "Israeli");
        COUNTRY_TO_CUISINE.put("saudi arabia", "Middle Eastern");
        COUNTRY_TO_CUISINE.put("iran", "Persian");
        COUNTRY_TO_CUISINE.put("iraq", "Iraqi");
        COUNTRY_TO_CUISINE.put("portugal", "Portuguese");
        COUNTRY_TO_CUISINE.put("belgium", "Belgian");
        COUNTRY_TO_CUISINE.put("netherlands", "Dutch");
        COUNTRY_TO_CUISINE.put("switzerland", "Swiss");
    }

    private LocationRecipeRecommender() {
    }

    public static String recommendCuisine(Address address) {
        if (address == null) {
            return "";
        }
        String locality = safeLower(address.getSubAdminArea());
        String city = safeLower(address.getLocality());
        String country = safeLower(address.getCountryName());

        String direct = COUNTRY_TO_CUISINE.get(country);
        if (direct != null) {
            return direct;
        }

        if (city.contains("rome") || locality.contains("rome")) {
            return "Italian";
        }
        if (city.contains("paris") || locality.contains("paris")) {
            return "French";
        }
        if (city.contains("bangkok") || locality.contains("bangkok")) {
            return "Thai";
        }
        if (city.contains("tokyo") || locality.contains("tokyo")) {
            return "Japanese";
        }
        if (city.contains("barcelona") || locality.contains("barcelona")) {
            return "Spanish";
        }
        if (city.contains("mumbai") || locality.contains("mumbai") || city.contains("delhi")) {
            return "Indian";
        }
        if (city.contains("mexico") || locality.contains("mexico")) {
            return "Mexican";
        }
        if (city.contains("athens") || locality.contains("athens")) {
            return "Greek";
        }
        if (city.contains("istanbul") || locality.contains("istanbul")) {
            return "Turkish";
        }

        return "";
    }

    private static String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT);
    }
}
