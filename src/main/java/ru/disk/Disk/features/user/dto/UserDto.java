package ru.disk.Disk.features.user.dto;

import lombok.Getter;
import lombok.Setter;
import ru.disk.Disk.features.user.entity.UserEntity;
import ru.disk.Disk.features.user.entity.UserRole;

import java.util.Set;

@Getter
@Setter
public class UserDto {

    private Long id;
    private String email;
    private Set<UserRole> roles;

    public UserDto(UserEntity entity) {
        this.id = entity.getId();
        this.email = entity.getEmail();
        this.roles = entity.getRoles();
    }
}
