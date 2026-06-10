package com.firstclub.membership;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Placeholder retained for project structure compatibility.
 * Testcontainers removed: the test suite uses the H2 in-memory database
 * configured via application.properties.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    // no beans — H2 auto-configured from application.properties
}
