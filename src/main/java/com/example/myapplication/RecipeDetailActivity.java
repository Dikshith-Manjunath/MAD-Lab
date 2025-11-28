package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.myapplication.data.RecipeIngredient;
import com.example.myapplication.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView detailImage;
    private TextView detailName;
    private TextView detailCuisine;
    private TextView detailCalories;
    private TextView detailCookTime;
    private TextView detailServings;
    private TextView detailAllergens;
    private TextView detailIngredients;
    private TextView detailInstructions;
    private ImageButton speakInstructionsButton;
    private TextToSpeech textToSpeech;
    private List<String> instructionSteps = new ArrayList<>();
    private int currentInstructionIndex = 0;
    private boolean isSpeakingInstructions = false;
    private boolean isTtsReady = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailCuisine = findViewById(R.id.detailCuisine);
        detailCalories = findViewById(R.id.detailCalories);
        detailCookTime = findViewById(R.id.detailCookTime);
        detailServings = findViewById(R.id.detailServings);
        detailAllergens = findViewById(R.id.detailAllergens);
        detailIngredients = findViewById(R.id.detailIngredients);
        detailInstructions = findViewById(R.id.detailInstructions);
        speakInstructionsButton = findViewById(R.id.speakInstructionsButton);
        updateSpeakButton(false);
        speakInstructionsButton.setEnabled(false);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());
                boolean supported = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
                isTtsReady = supported;
                if (!supported) {
                    Toast.makeText(this, R.string.voice_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                isTtsReady = false;
                Toast.makeText(this, R.string.voice_error, Toast.LENGTH_SHORT).show();
            }
            updateSpeakInstructionsAvailability();
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // no-op
            }

            @Override
            public void onDone(String utteranceId) {
                mainHandler.post(() -> {
                    if (!isSpeakingInstructions) {
                        return;
                    }
                    currentInstructionIndex++;
                    speakCurrentInstruction();
                });
            }

            @Override
            public void onError(String utteranceId) {
                mainHandler.post(RecipeDetailActivity.this::stopInstructionPlayback);
            }
        });

        speakInstructionsButton.setOnClickListener(v -> {
            if (!isSpeakingInstructions) {
                startInstructionPlayback();
            } else {
                stopInstructionPlayback();
            }
        });

        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        if (recipe != null) {
            displayRecipe(recipe);
        }
    }

    private void displayRecipe(Recipe recipe) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipe.getName());
        }

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(recipe.getImageUrl())
                    .placeholder(recipe.resolveImageResId(this))
                    .error(recipe.resolveImageResId(this))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(detailImage);
        } else {
            detailImage.setImageResource(recipe.resolveImageResId(this));
        }
        detailName.setText(recipe.getName());
        detailCuisine.setText(recipe.getCuisine());
        detailCalories.setText(getString(R.string.format_calories, recipe.getCalories()));
        detailCookTime.setText(getString(R.string.format_minutes, recipe.getCookingTime()));
        detailServings.setText(getString(R.string.format_servings, recipe.getServings()));
        detailAllergens.setText(recipe.getAllergens());

        StringBuilder ingredientsText = new StringBuilder();
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            RecipeIngredient ingredient = recipe.getIngredients().get(i);
            ingredientsText.append("• ").append(ingredient.getDisplayText());
            if (i < recipe.getIngredients().size() - 1) {
                ingredientsText.append("\n\n");
            }
        }
        detailIngredients.setText(ingredientsText.toString());

        StringBuilder instructionsText = new StringBuilder();
        for (int i = 0; i < recipe.getInstructions().size(); i++) {
            instructionsText.append(i + 1)
                    .append(". ")
                    .append(recipe.getInstructions().get(i));
            if (i < recipe.getInstructions().size() - 1) {
                instructionsText.append("\n\n");
            }
        }
        detailInstructions.setText(instructionsText.toString());

        instructionSteps = new ArrayList<>(recipe.getInstructions());
        updateSpeakInstructionsAvailability();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopInstructionPlayback();
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void startInstructionPlayback() {
        if (!isTtsReady || instructionSteps.isEmpty() || textToSpeech == null) {
            Toast.makeText(this, R.string.voice_error, Toast.LENGTH_SHORT).show();
            return;
        }
        isSpeakingInstructions = true;
        currentInstructionIndex = 0;
        updateSpeakButton(true);
        speakCurrentInstruction();
    }

    private void speakCurrentInstruction() {
        if (!isSpeakingInstructions || textToSpeech == null) {
            return;
        }
        if (currentInstructionIndex >= instructionSteps.size()) {
            stopInstructionPlayback();
            return;
        }
        String stepText = getString(R.string.tts_step_format, currentInstructionIndex + 1, instructionSteps.get(currentInstructionIndex));
        Bundle params = new Bundle();
        String utteranceId = "STEP_" + currentInstructionIndex;
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        textToSpeech.speak(stepText, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
    }

    private void stopInstructionPlayback() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        isSpeakingInstructions = false;
        currentInstructionIndex = 0;
        updateSpeakButton(false);
    }

    private void updateSpeakInstructionsAvailability() {
        speakInstructionsButton.setEnabled(isTtsReady && !instructionSteps.isEmpty());
    }

    private void updateSpeakButton(boolean speaking) {
        if (speaking) {
            speakInstructionsButton.setImageResource(R.drawable.ic_stop);
            speakInstructionsButton.setContentDescription(getString(R.string.stop_instruction_audio));
        } else {
            speakInstructionsButton.setImageResource(R.drawable.ic_mic);
            speakInstructionsButton.setContentDescription(getString(R.string.start_instruction_audio));
        }
    }
}
