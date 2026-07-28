package com.kayque.compensa.onboarding.service;

import com.kayque.compensa.onboarding.repository.AppPreferenceRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OnboardingServiceTest {

    @Test
    void shouldShowOnboardingWhenPreferenceDoesNotExist() {
        InMemoryAppPreferenceRepository repository =
                new InMemoryAppPreferenceRepository();

        OnboardingService service =
                new OnboardingService(repository);

        assertTrue(service.shouldShowOnboarding());
    }

    @Test
    void shouldNotShowOnboardingAfterCompletion() {
        InMemoryAppPreferenceRepository repository =
                new InMemoryAppPreferenceRepository();

        OnboardingService service =
                new OnboardingService(repository);

        service.completeOnboarding();

        assertFalse(service.shouldShowOnboarding());
    }

    @Test
    void shouldShowOnboardingAgainAfterReset() {
        InMemoryAppPreferenceRepository repository =
                new InMemoryAppPreferenceRepository();

        OnboardingService service =
                new OnboardingService(repository);

        service.completeOnboarding();
        service.resetOnboarding();

        assertTrue(service.shouldShowOnboarding());
    }

    @Test
    void shouldRejectNullRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new OnboardingService(null)
        );
    }

    private static class InMemoryAppPreferenceRepository
            implements AppPreferenceRepository {

        private final Map<String, String> preferences =
                new HashMap<>();

        @Override
        public Optional<String> findValue(String key) {
            return Optional.ofNullable(
                    preferences.get(key)
            );
        }

        @Override
        public void save(String key, String value) {
            preferences.put(key, value);
        }
    }
}