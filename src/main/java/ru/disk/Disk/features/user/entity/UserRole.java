package ru.disk.Disk.features.user.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum UserRole implements GrantedAuthority {
    BASE_USER,
    ADMIN;

    @Override
    public String getAuthority() {
        return this.name();
    }
}
