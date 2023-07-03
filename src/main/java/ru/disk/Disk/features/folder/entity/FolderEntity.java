package ru.disk.Disk.features.folder.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.disk.Disk.features.file.entity.FileEntity;
import ru.disk.Disk.features.folder.dto.FolderDto;
import ru.disk.Disk.features.user.entity.UserEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Schema
@Entity(name = "folders")
public class FolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public Date dateUpdate = new Date();

    @Column(nullable = false)
    public Date dateCreate = new Date();

    @Column(nullable = false)
    public Boolean isPublic = false;

    @ManyToOne(fetch = FetchType.EAGER)
    public UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    public FolderEntity folder = null;

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY)
    public Set<FolderEntity> folders;

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY)
    public Set<FileEntity> files;

    public FolderEntity() {

    }

    public FolderEntity(String name, UserEntity user, FolderEntity folder) {
        this.name = name;
        this.user = user;
        this.folder = folder;
    }

    public FolderEntity(FolderDto dto) {
        this.id = dto.getId();
        this.name = dto.getName();
        this.dateCreate = dto.getDateCreate();
        this.dateUpdate = dto.getDateUpdate();
        this.isPublic = dto.getIsPublic();
        this.user = new UserEntity(dto.getUser());
    }

    public String getPatch() {
        return "/resources/users/" +
               user.getEmail() +
                "/" + name + "_" + id;
    }
}
