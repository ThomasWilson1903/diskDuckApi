package ru.disk.Disk.features.file.dto;

import lombok.Getter;
import lombok.Setter;
import ru.disk.Disk.features.file.entity.FileEntity;
import ru.disk.Disk.features.user.dto.UserDto;
import ru.disk.Disk.utils.BaseConstance;

import java.util.Date;

@Getter
@Setter
public class FileDto {

    public Long id;
    public String name;
    public String url;
    public String expansion;
    public Integer size;
    public Date dateUpdate;
    public Date dateCreate;
    public Boolean inBasket;
    public Boolean isPublic;
    public UserDto user;

    public FileDto(FileEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.dateCreate = entity.getDateCreate();
        this.dateUpdate = entity.getDateUpdate();
        this.isPublic = entity.getIsPublic();
        this.expansion = entity.getExpansion();
        this.size = entity.getSize();
        this.inBasket = entity.getInBasket();

        this.user = new UserDto(entity.getUser());

        this.url = BaseConstance.BASE_URL + "/files/resource?file_id=" + entity.getId();
    }
}
