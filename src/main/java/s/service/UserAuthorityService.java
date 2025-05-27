package s.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import s.repository.UserAuthorityRepository;

@Service
public class UserAuthorityService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserAuthorityRepository userAuthorityRepository;

    public UserAuthorityService(UserAuthorityRepository userAuthorityRepository) {
        this.userAuthorityRepository = userAuthorityRepository;
    }


    public Mono<Void> grantActivatedAuthority(Long id) {
        return userAuthorityRepository.grantActivatedAuthority(id);
    }

}
