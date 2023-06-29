package ru.disk.Disk.features.folder;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.disk.Disk.features.folder.dto.FolderDto;
import ru.disk.Disk.features.folder.entity.FolderEntity;
import ru.disk.Disk.features.user.UserRepository;
import ru.disk.Disk.features.user.entity.UserEntity;
import ru.disk.Disk.utils.exceptions.NotFoundException;
import ru.disk.Disk.utils.repository.FileManager;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.Optional;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileManager fileManager;

    public Page<FolderDto> getAll(
            Long folderId,
            @Min(0) int pageNumber,
            @Min(1) @Max(100) int pageSize
    ) {
        Page<FolderEntity> entities;

        if(folderId == null){
            entities = folderRepository.findByFolderNull(
                    PageRequest.of(pageNumber, pageSize)
            );
        }else {
            entities = folderRepository.findByFolderId(
                    folderId,
                    PageRequest.of(pageNumber, pageSize)
            );
        }

        return entities.map(FolderDto::new);
    }

    @SneakyThrows
    public FolderDto add(String name, Long folderId, Long userId) {
        FolderEntity folder = null;

        if(folderId != null){
            Optional<FolderEntity> optionalFolder = folderRepository.findById(folderId);

            if(optionalFolder.isPresent())
                folder = optionalFolder.get();
        }

        Optional<UserEntity> user = userRepository.findById(userId);

        if(user.isEmpty()) throw new NotFoundException("user not found");

        FolderEntity folderEntity = new FolderEntity(name, user.get(), folder);

        return new FolderDto(folderRepository.save(folderEntity));
    }

    @SneakyThrows
    public Long getSize(Long folderId) {
        Optional<FolderEntity> optionalFolderEntity = folderRepository.findById(folderId);

        if(optionalFolderEntity.isEmpty())
            throw new NotFoundException("folder not found");

        FolderEntity folderEntity = optionalFolderEntity.get();

        return fileManager.getSizeFolder(
                "/resources/users/" +
                        folderEntity.getUser().getEmail() +
                        "/" + folderEntity.getName() + "_" + folderEntity.getId()
        );
    }

    @SneakyThrows
    @Transient
    public void delete(Long folderId) {
        Optional<FolderEntity> optionalFolderEntity = folderRepository.findById(folderId);

        if(optionalFolderEntity.isEmpty()) throw new NotFoundException("folder not found");

        FolderEntity folderEntity = optionalFolderEntity.get();

        folderRepository.delete(folderEntity);

        fileManager.deleteFolder(
                "/resources/users/" +
                        folderEntity.getUser().getEmail() +
                        "/" + folderEntity.getName() + "_" + folderEntity.getId()
        );
    }

    @SneakyThrows
    public FolderDto updatePublic(Long folderId) {
        Optional<FolderEntity> optionalFolderEntity = folderRepository.findById(folderId);

        if(optionalFolderEntity.isEmpty()) throw new NotFoundException("folder not found");

        FolderEntity folderEntity = optionalFolderEntity.get();

        folderEntity.setIsPublic(!folderEntity.getIsPublic());
        folderEntity.setDateUpdate(new Date());

        return new FolderDto(folderRepository.save(folderEntity));
    }
}
