package com.kayque.compensa.goal.repository;

import com.kayque.compensa.goal.model.SavingsGoalContribution;

import java.math.BigDecimal;
import java.util.List;

public interface SavingsGoalContributionRepository {

    void add(BigDecimal amount);

    void remove(long contributionId);

    List<SavingsGoalContribution> findAll();
}