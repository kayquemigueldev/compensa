package com.kayque.compensa.onboarding.service;

import com.kayque.compensa.onboarding.repository.AppPreferenceRepository;

import java.util.Objects;

public class OnboardingService {

    private static final String
            ONBOARDING_COMPLETED_KEY =
            "onboarding_completed";

    private final AppPreferenceRepository repository;

    public OnboardingService(
            AppPreferenceRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "O repositório de preferências é obrigatório."
        );
    }

    public boolean shouldShowOnboarding() {
        return repository
                .findValue(ONBOARDING_COMPLETED_KEY)
                .map(Boolean::parseBoolean)
                .map(completed -> !completed)
                .orElse(true);
    }

    public void completeOnboarding() {
        repository.save(
                ONBOARDING_COMPLETED_KEY,
                Boolean.TRUE.toString()
        );
    }

    public void resetOnboarding() {
        repository.save(
                ONBOARDING_COMPLETED_KEY,
                Boolean.FALSE.toString()
        );
    }
}