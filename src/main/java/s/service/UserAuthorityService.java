package s.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import s.domain.Authority;
import s.repository.AuthorityRepository;
import s.repository.StorageFileRepository;
import s.repository.UserAuthorityRepository;
import s.repository.UserRepository;

@Service
public class UserAuthorityService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserAuthorityRepository userAuthorityRepository;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    public UserAuthorityService(UserAuthorityRepository userAuthorityRepository, UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userAuthorityRepository = userAuthorityRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }


    public Mono<Void> grantActivatedAuthority(Long id) {
        return userAuthorityRepository.grantActivatedAuthority(id);
    }

}
