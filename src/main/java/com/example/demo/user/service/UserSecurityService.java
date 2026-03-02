package com.example.demo.user.service;

import com.example.demo.user.model.SiteUser;
import com.example.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SiteUser> _siteUser = this.userRepository.findByUsername(username);
        if (_siteUser.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을수 없습니다.");
        }
        SiteUser siteUser = _siteUser.get();
        
        // 정지 여부 확인
        if (siteUser.getSuspendedUntil() != null && siteUser.getSuspendedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("정지된 계정입니다. 정지 기한: " + siteUser.getSuspendedUntil());
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if ("ROLE_ADMIN".equals(siteUser.getRole())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
    }
}
