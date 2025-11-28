package com.example.myapplication.data;

import java.io.Serializable;

public class RecipeIngredient implements Serializable {
    private String name;
    private double quantity;
    private String unit;

    public RecipeIngredient() {
    }

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDisplayText() {
        StringBuilder builder = new StringBuilder();

        if (quantity > 0) {
            String value = quantity == (long) quantity ? String.valueOf((long) quantity) : trimTrailingZeros(quantity);
            builder.append(value);
        }

        if (unit != null && !unit.trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(unit.trim());
        }

        if (name != null && !name.trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(name.trim());
        }

        return builder.length() > 0 ? builder.toString() : "Ingredient";
    }

    private String trimTrailingZeros(double number) {
        String text = String.valueOf(number);
        if (!text.contains(".")) {
            return text;
        }
        while (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
