package com.addon.rateLimit.user;

import java.util.Optional;

public interface UserIdentifier <T>{
    Optional<T> getUserIdentifier();

}
