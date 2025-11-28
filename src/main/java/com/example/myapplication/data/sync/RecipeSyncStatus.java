package com.example.myapplication.data.sync;

import androidx.annotation.NonNull;

/**
 * Represents the current state of a recipe synchronization job.
 */
public final class RecipeSyncStatus {

    public enum State {
        IDLE,
        RUNNING,
        SUCCEEDED,
        FAILED
    }

    private final State state;
    private final int progress;
    private final int goal;
    private final int inserted;
    private final String message;

    private RecipeSyncStatus(State state, int progress, int goal, int inserted, String message) {
        this.state = state;
        this.progress = progress;
        this.goal = goal;
        this.inserted = inserted;
        this.message = message;
    }

    public static RecipeSyncStatus idle() {
        return new RecipeSyncStatus(State.IDLE, 0, 0, 0, null);
    }

    public static RecipeSyncStatus running(int progress, int goal) {
        return new RecipeSyncStatus(State.RUNNING, progress, goal, 0, null);
    }

    public static RecipeSyncStatus success(int inserted) {
        return new RecipeSyncStatus(State.SUCCEEDED, inserted, inserted, inserted, null);
    }

    public static RecipeSyncStatus failure(@NonNull String message) {
        return new RecipeSyncStatus(State.FAILED, 0, 0, 0, message);
    }

    public State getState() {
        return state;
    }

    public int getProgress() {
        return progress;
    }

    public int getGoal() {
        return goal;
    }

    public int getInserted() {
        return inserted;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public boolean isTerminal() {
        return state == State.SUCCEEDED || state == State.FAILED;
    }
}
