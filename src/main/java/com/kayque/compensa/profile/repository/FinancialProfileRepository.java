package com.kayque.compensa.profile.repository;

import com.kayque.compensa.profile.model.FinancialProfile;

import java.util.Optional;

public interface FinancialProfileRepository {

    Optional<FinancialProfile> find();

    void save(FinancialProfile profile);
}