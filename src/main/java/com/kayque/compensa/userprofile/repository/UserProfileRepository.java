package com.kayque.compensa.userprofile.repository;

import com.kayque.compensa.userprofile.model.UserProfile;

import java.util.Optional;

public interface UserProfileRepository {

    Optional<UserProfile> find();

    void save(UserProfile profile);
}