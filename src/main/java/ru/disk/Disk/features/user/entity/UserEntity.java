package ru.disk.Disk.features.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.disk.Disk.features.file.entity.FileEntity;
import ru.disk.Disk.features.folder.entity.FolderEntity;
import ru.disk.Disk.features.user.dto.RegisterDto;
import ru.disk.Disk.features.user.dto.UserDto;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity(name = "users")
@Schema
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(unique = true, nullable = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public Set<FolderEntity> folders;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public Set<FileEntity> files;

    public UserEntity() {}

    public UserEntity(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public UserEntity(RegisterDto dto) {
        this.email = dto.getEmail();
        this.password = dto.getPassword();
    }

    public UserEntity(UserDto dto) {
        this.email = dto.getEmail();
        this.roles = dto.getRoles();
        this.id = dto.getId();
    }
}
