package ru.disk.Disk.features.file.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import ru.disk.Disk.features.folder.entity.FolderEntity;
import ru.disk.Disk.features.user.entity.UserEntity;
import ru.disk.Disk.utils.BaseConstance;

import javax.persistence.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

@Getter
@Setter
@Schema
@Entity(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String expansion;

    @Column(nullable = false)
    public Integer size;

    @Column(nullable = false)
    public Date dateUpdate;

    @Column(nullable = false)
    public Date dateCreate;

    @Column(nullable = false)
    public Boolean isPublic;

    @Column(nullable = false)
    public Boolean inBasket = false;

    @ManyToOne(fetch = FetchType.EAGER)
    public UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    public FolderEntity folder = null;

    public FileEntity() {

    }

    public FileEntity(
            String path,
            UserEntity user,
            FolderEntity folder
    ) throws IOException {
        this.name = FilenameUtils.getName(path);
        this.expansion = FilenameUtils.getExtension(path);
        this.dateCreate = new Date();
        this.dateUpdate = new Date();
        this.isPublic = false;
        this.inBasket = false;
        this.user = user;
        this.folder = folder;

        this.size = Math.toIntExact(Files.size(Path.of(path)));
    }

    public String getPatch() {
        String folderName;

        if(folder == null)
            folderName = "main";
        else
            folderName = folder.name + "_" + folder.getId();

        String fileName = name;

        if(!fileName.contains("."))
            fileName += "." + expansion;

        return "/resources/users/" + user.getEmail() + "/" + folderName + "/" + fileName;
    }
}
