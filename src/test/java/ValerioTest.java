import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValerioTest {
    @Test
    public void aLogicianNightmare() {
        assertThat(a()).isFalse();
    }

    @Test
    public void aLogicianNightmare1() {
        assertThat(!a()).isFalse();
    }

    private boolean a() {
        String valerioTest = System.getProperty("ValerioTest");
        if (valerioTest == null || valerioTest.equals("")) {
            System.setProperty("ValerioTest", "-");
            return true;
        } else {
            System.clearProperty("ValerioTest");
            return false;
        }
    }
}
