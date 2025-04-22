package org.wildfly.capabilities;

import io.quarkiverse.roq.testing.RoqAndRoll;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RoqAndRoll
public class SiteTest {
    @Test
    public void testGen() {
        // All pages will be generated/validated during test setup
    }
}
