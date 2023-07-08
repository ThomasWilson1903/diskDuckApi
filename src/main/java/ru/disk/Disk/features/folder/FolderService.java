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

import javax.security.auth.message.AuthException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.Objects;
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
            Long userId,
            @Min(0) int pageNumber,
            @Min(1) @Max(100) int pageSize
    ) {
        Page<FolderEntity> entities;

        if(folderId == null){
            entities = folderRepository.findByFolderNull(
                    userId,
                    PageRequest.of(pageNumber, pageSize)
            );
        }else {
            entities = folderRepository.findByFolderId(
                    folderId,
                    userId,
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

        return fileManager.getSizeFolder(folderEntity.getPatch());
    }

    @SneakyThrows
    @Transient
    public void delete(Long folderId, Long userId) {
        Optional<FolderEntity> optionalFolderEntity = folderRepository.findById(folderId);

        if(optionalFolderEntity.isEmpty()) throw new NotFoundException("folder not found");

        FolderEntity folderEntity = optionalFolderEntity.get();

        if(!Objects.equals(folderEntity.getUser().getId(), userId)) throw new AuthException();

        folderEntity.setInBasket(true);

        folderRepository.save(folderEntity);
    }

    @SneakyThrows
    public FolderDto updatePublic(Long folderId, Long userId) {
        Optional<FolderEntity> optionalFolderEntity = folderRepository.findById(folderId);

        if(optionalFolderEntity.isEmpty()) throw new NotFoundException("folder not found");

        FolderEntity folderEntity = optionalFolderEntity.get();

        if(!Objects.equals(folderEntity.getUser().getId(), userId)) throw new AuthException();

        folderEntity.setIsPublic(!folderEntity.getIsPublic());
        folderEntity.setDateUpdate(new Date());

        return new FolderDto(folderRepository.save(folderEntity));
    }

    @SneakyThrows
    public FolderDto rename(Long userId, Long folderId, String name) {
        Optional<FolderEntity> optionalFolderEntity = folderRepository.findById(folderId);

        if(optionalFolderEntity.isEmpty()) throw new NotFoundException("folder not found");

        FolderEntity folderEntity = optionalFolderEntity.get();

        if(!Objects.equals(folderEntity.getUser().getId(), userId)) throw new AuthException();

        String oldFolderPatch = folderEntity.getPatch();

        folderEntity.setName(name);

        String newFolderPatch = folderEntity.getPatch();

        Boolean renameSuccess = fileManager.rename(oldFolderPatch, newFolderPatch);

        if(!renameSuccess) throw new Exception();

        return new FolderDto(folderRepository.save(folderEntity));
    }

    @SneakyThrows
    public FolderDto updateFolder(Long userId, Long folderId, Long newFolderId) {
        Optional<FolderEntity> optionalFolderEntity = folderRepository.findById(folderId);

        if(optionalFolderEntity.isEmpty()) throw new NotFoundException("folder not found");

        FolderEntity folderEntity = optionalFolderEntity.get();

        FolderEntity newFolderEntity;

        if(newFolderId == null) {
            newFolderEntity = null;

            if(!Objects.equals(folderEntity.getUser().getId(), userId))
                throw new AuthException();
        }else {
            Optional<FolderEntity> optionalNewFolderEntity = folderRepository.findById(newFolderId);

            if(optionalNewFolderEntity.isEmpty()) throw new NotFoundException("file not found");

            newFolderEntity = optionalNewFolderEntity.get();

            if(!Objects.equals(folderEntity.getUser().getId(), userId) || !Objects.equals(newFolderEntity.getUser().getId(), userId))
                throw new AuthException();
        }

        String oldPatch = folderEntity.getPatch();

        folderEntity.setFolder(newFolderEntity);
        folderEntity.setDateUpdate(new Date());

        Boolean renameSuccess = fileManager.rename(
                oldPatch,
                folderEntity.getPatch()
        );

        if(!renameSuccess)
            throw new Exception();

        return new FolderDto(folderEntity);
    }

    public void deleteAllInBasket(Long userId) {
        folderRepository.deleteAllInBasket(userId);
    }
}
