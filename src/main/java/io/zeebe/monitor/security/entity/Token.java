package io.zeebe.monitor.security.entity;

import java.time.Instant;
import java.util.List;

public record Token(String subject,
                    Instant createdAt,
                    Instant expiresAt,
                    List<String> authorities
) {
}
