import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal, deterministic smoke test that guards the JUnit test infrastructure:
 * if junit-platform-launcher and the jupiter engine ever drift again, this fails
 * to discover instead of silently leaving the suite with nothing to run.
 */
public class SmokeTest {
    @Test
    public void testInfraRuns() {
        assertThat(1 + 1).isEqualTo(2);
    }
}
