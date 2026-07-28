package com.kayque.compensa.onboarding.repository;

import java.util.Optional;

public interface AppPreferenceRepository {

    Optional<String> findValue(String key);

    void save(String key, String value);
}