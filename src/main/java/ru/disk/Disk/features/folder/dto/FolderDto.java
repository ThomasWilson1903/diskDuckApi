package ru.disk.Disk.features.folder.dto;

import lombok.Getter;
import lombok.Setter;
import ru.disk.Disk.features.folder.entity.FolderEntity;
import ru.disk.Disk.features.user.dto.UserDto;

import java.util.Date;

@Getter
@Setter
public class FolderDto {

    public Long id;
    public String name;
    public Date dateUpdate;
    public Date dateCreate;
    public Boolean isPublic;
    public UserDto user;

    public FolderDto(FolderEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.dateCreate = entity.getDateCreate();
        this.dateUpdate = entity.getDateUpdate();
        this.isPublic = entity.getIsPublic();

        this.user = new UserDto(entity.getUser());
    }
}
