package com.danphe.rivo.web.client.security;

import com.danphe.rivo.service.api.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClientUserDetailsService implements UserDetailsService {

  private final UserService userService;

  public ClientUserDetailsService(@org.springframework.beans.factory.annotation.Qualifier("clientUserService") UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userService.findByEmail(email)
        .map(ClientUserPrincipal::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
  }
}
