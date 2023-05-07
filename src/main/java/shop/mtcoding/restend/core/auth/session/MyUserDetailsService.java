package shop.mtcoding.restend.core.auth.session;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import shop.mtcoding.restend.model.user.User;
import shop.mtcoding.restend.model.user.UserRepository;

@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * login 시 username, password -> email, password 로 바뀜에 따른 코드 수정
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User userPS = userRepository.findByEmail(email).orElseThrow(
                () -> new InternalAuthenticationServiceException("인증 실패")); // 나중에 테스트할 때 설명해드림.
        return new MyUserDetails(userPS);
    }
}
