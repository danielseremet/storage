package s.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import s.domain.UserReservations;
import s.repository.UserRepository;
import s.repository.UserReservationsRepository;

@Service
public class UserReservationsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserReservationsService.class);

    private final UserReservationsRepository userReservationsRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final UserAuthorityService userAuthorityService;

    public UserReservationsService(UserReservationsRepository userReservationsRepository, UserRepository userRepository, MailService mailService, UserAuthorityService userAuthorityService) {
        this.userReservationsRepository = userReservationsRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.userAuthorityService = userAuthorityService;
    }

    public Flux<UserReservations> getAwaitingApprovalReservations() {
        return userReservationsRepository.getWaitingForApproval();
    }

    public Mono<Void> approveUserReservations(long id) {

        LOG.info("Approving user reservations for user {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    LOG.warn("User with id {} not found", id);
                    return Mono.empty();
                }))
                .flatMap(user -> {
                    LOG.debug("Sending mail and approving reservations for user: {}", user.getId());
                    return userReservationsRepository.approveUsersReservation(id)
                        .doOnSuccess(res -> mailService.sendStorageReservationApprovedMail(user))
                        .then(userAuthorityService.grantActivatedAuthority(id));
                });
    }




    public Mono<Void> saveUserReservations(int storage) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .flatMap(userRepository::findOneByLogin)
            .flatMap(user ->
                userReservationsRepository.findById(user.getId())
                    .map(userReservations -> userReservations.getActivated())
                    .defaultIfEmpty(false)
                    .flatMap(activated -> {
                        if (!activated) {
                            return userReservationsRepository.saveUserReservation(user.getLogin(), storage)
                                .doOnSuccess(res -> mailService.sendStorageReservationSentMail(user));
                        } else {
                            return Mono.error(new IllegalStateException("Storage already activated"));

                        }
                    })
            );
    }






}
