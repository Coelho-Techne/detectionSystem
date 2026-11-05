package com.coelhotechne.detection_system.globalClass.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessKeyTest {

    private final AccessKeyGenerator generator = new AccessKeyGenerator();
    private final AccessKeyHasher hasher = new AccessKeyHasher();

    @Test
    void generatedKeyIsUrlSafeAndUnique(){
        String a = generator.generate();
        String b = generator.generate();
        assertThat(a).hasSize(43).matches("^[A-Za-z0-9_-]+$").isNotEqualTo(b);
    }
    @Test
    void hasMatchesOnlyTheOriginalKey(){
        String key = generator.generate();
        String hash = hasher.hash(key);
        assertThat(hash).isNotEqualTo(key);
        assertThat(hasher.matches(key,hash)).isTrue();
        assertThat(hasher.matches(key+"x",hash)).isFalse();
        assertThat(hasher.matches(null,hash)).isFalse();
        assertThat(hasher.matches(key,null)).isFalse();
    }
}
