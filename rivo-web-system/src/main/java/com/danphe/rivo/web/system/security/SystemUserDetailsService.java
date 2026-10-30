package com.danphe.rivo.web.system.security;

import com.danphe.rivo.service.api.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SystemUserDetailsService implements UserDetailsService {

  private final UserService userService;

  public SystemUserDetailsService(@org.springframework.beans.factory.annotation.Qualifier("systemUserService") UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userService.findByEmail(email)
        .map(SystemUserPrincipal::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
  }
}
