package com.kayque.compensa.goal.repository;

public interface SavingsGoalLifecycleRepository {

    void archiveCompletedGoalAndPrepareNew();
}