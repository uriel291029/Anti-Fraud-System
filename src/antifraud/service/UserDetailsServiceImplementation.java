package antifraud.service;

import antifraud.domain.security.UserDetailsImplementation;
import antifraud.model.User;
import antifraud.repository.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImplementation implements UserDetailsService {


  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<User> user = userRepository.findByUsername(username);
    if(user.isEmpty()){
      throw new UsernameNotFoundException("Not found");
    }
    Collection<? extends GrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority("ROLE_"+user.get().getRole()));
    return UserDetailsImplementation.builder()
        .username(user.get().getUsername())
        .unlock(user.get().isUnlock())
        .authorities(authorities)
        .password(user.get().getPassword()).build();
  }
}
