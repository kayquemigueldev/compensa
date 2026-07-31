package com.kayque.compensa.goal.repository;

import com.kayque.compensa.goal.model.SavingsGoalAchievement;

import java.util.List;

public interface SavingsGoalAchievementRepository {

    List<SavingsGoalAchievement> findAll();
}