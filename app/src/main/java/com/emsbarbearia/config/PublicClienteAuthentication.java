package com.emsbarbearia.config;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class PublicClienteAuthentication implements Authentication {

    private final Long clienteId;
    private boolean authenticated = true;

    public PublicClienteAuthentication(Long clienteId) {
        this.clienteId = clienteId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return clienteId;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return String.valueOf(clienteId);
    }

    public Long getClienteId() {
        return clienteId;
    }
}
