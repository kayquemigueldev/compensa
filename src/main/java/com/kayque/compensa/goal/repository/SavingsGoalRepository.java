package com.kayque.compensa.goal.repository;

import com.kayque.compensa.goal.model.SavingsGoal;

import java.util.Optional;

public interface SavingsGoalRepository {

    Optional<SavingsGoal> find();

    void save(SavingsGoal goal);
}