package com.pinyougou.shop.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

public class UserDetailsServiceImpl implements UserDetailsService {
	private SellerService sellerService;

	public SellerService getSellerService() {
		return sellerService;
	}

	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		TbSeller tbSeller = sellerService.findOne(username);
		if (tbSeller != null) {
			if (tbSeller.getStatus().equals("1") ) {
				return new User(username, tbSeller.getPassword(), grantedAuthorities);
			}
			return null;
		}
		return null;
	}
}
