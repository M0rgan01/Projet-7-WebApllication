package com.bibliotheque.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.bibliotheque.service.BibliothequeException_Exception;
import com.bibliotheque.service.BibliothequeServiceService;
import com.bibliotheque.service.BibliothequeWS;
import com.bibliotheque.service.Roles;
import com.bibliotheque.service.Utilisateur;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider, UserDetailsService {

	private Utilisateur utilisateur;
	private List<GrantedAuthority> grantedAuths;
		
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		BibliothequeWS ws = new BibliothequeServiceService().getBibliothequeWSPort();

		try {
			utilisateur = ws.doConnection(authentication.getName(), authentication.getCredentials().toString());
		} catch (BibliothequeException_Exception e) {
			throw new BadCredentialsException(e.getFaultInfo().getInfo().getFaultString());
		}

		grantedAuths = new ArrayList<>();

		for (Roles role : ws.getListRoles(utilisateur.getPseudo())) {
			grantedAuths.add(new SimpleGrantedAuthority(role.getRole()));
		}
				
		return new UsernamePasswordAuthenticationToken(utilisateur.getPseudo(), utilisateur.getPassWord(),
				grantedAuths);

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {	
		
		System.out.println(username);
		
		return new UserService(utilisateur, grantedAuths);
	}

}
