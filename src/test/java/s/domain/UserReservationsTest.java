package s.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static s.domain.UserReservationsTestSamples.*;

import org.junit.jupiter.api.Test;
import s.web.rest.TestUtil;

class UserReservationsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserReservations.class);
        UserReservations userReservations1 = getUserReservationsSample1();
        UserReservations userReservations2 = new UserReservations();
        assertThat(userReservations1).isNotEqualTo(userReservations2);

        userReservations2.setId(userReservations1.getId());
        assertThat(userReservations1).isEqualTo(userReservations2);

        userReservations2 = getUserReservationsSample2();
        assertThat(userReservations1).isNotEqualTo(userReservations2);
    }
}
