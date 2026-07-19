package com.kayque.compensa.goal.repository;

public interface SavingsGoalMilestoneRepository {

    int findLastCelebratedMilestone();

    void saveLastCelebratedMilestone(int milestone);
}