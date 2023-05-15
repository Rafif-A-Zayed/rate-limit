package com.addon.rateLimit.identifiers;

import java.util.Optional;

public interface ClientIdentifier {

    Optional<String> getClientIdentifier();
}
