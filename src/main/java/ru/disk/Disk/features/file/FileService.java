package ru.disk.Disk.features.file;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.disk.Disk.features.file.dto.FileDto;
import ru.disk.Disk.features.file.entity.FileEntity;
import ru.disk.Disk.features.folder.FolderRepository;
import ru.disk.Disk.features.folder.entity.FolderEntity;
import ru.disk.Disk.features.user.UserRepository;
import ru.disk.Disk.features.user.entity.UserEntity;
import ru.disk.Disk.utils.exceptions.NotFoundException;
import ru.disk.Disk.utils.repository.FileManager;

import javax.security.auth.message.AuthException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileManager fileManager;

    public Page<FileDto> getAll(
            Long folderId,
            Long userId,
            Boolean inBasket,
            @Min(0) int pageNumber,
            @Min(1) @Max(100) int pageSize
    ) {
        Page<FileEntity> entities;

        if(folderId == null){
            entities = fileRepository.findByFolderNull(
                    userId,
                    inBasket,
                    PageRequest.of(pageNumber, pageSize)
            );
        }else {
            entities = fileRepository.findByFolderId(
                    folderId,
                    userId,
                    inBasket,
                    PageRequest.of(pageNumber, pageSize)
            );
        }

        return entities.map(FileDto::new);
    }

    @SneakyThrows
    public FileDto add(
            Long folderId,
            Long userId,
            MultipartFile file
    ){
        FolderEntity folder = null;
        String folderName;

        if(folderId != null){
            Optional<FolderEntity> optionalFolder = folderRepository.findById(folderId);

            if(optionalFolder.isPresent())
                folder = optionalFolder.get();
            else
                throw new NotFoundException("folder not found");

            folderName = folder.getName() + "_" + folder.getId();
        }else {
            folderName = "main";
        }

        Optional<UserEntity> userOptional = userRepository.findById(userId);

        if(userOptional.isEmpty()) throw new NotFoundException("user not found");

        UserEntity userEntity = userOptional.get();

        String patch = fileManager.upload(file, "/resources/users/" + userEntity.getEmail() + "/" + folderName + "/");

        FileEntity fileEntity = new FileEntity(patch, userEntity, folder);

        return new FileDto(fileRepository.save(fileEntity));
    }

    @SneakyThrows
    @Transient
    public FileDto rename(Long fileId, String name, Long userId) {
        Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);

        if(optionalFileEntity.isEmpty())
            throw new NotFoundException("file not found");

        FileEntity fileEntity = optionalFileEntity.get();

        if(!Objects.equals(fileEntity.getUser().getId(), userId)) throw new AuthException();

        String oldPatch = fileEntity.getPatch();

        fileEntity.setName(name);
        fileEntity.setDateUpdate(new Date());

        FileDto fileDto =  new FileDto(fileRepository.save(fileEntity));

        fileManager.rename(
                oldPatch,
                fileEntity.getPatch()
        );

        return fileDto;
    }

    @SneakyThrows
    @Transient
    public FileDto updateFolder(Long fileId, Long folderId, Long userId) {
        Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);

        if(optionalFileEntity.isEmpty()) throw new NotFoundException("file not found");

        FileEntity fileEntity = optionalFileEntity.get();

        FolderEntity folderEntity;

        if(folderId == null) {
            folderEntity = null;

            if(!Objects.equals(fileEntity.getUser().getId(), userId))
                throw new AuthException();
        }else {
            Optional<FolderEntity> optionalFolderEntity = folderRepository.findById(folderId);

            if(optionalFolderEntity.isEmpty()) throw new NotFoundException("file not found");

            folderEntity = optionalFolderEntity.get();

            if(!Objects.equals(fileEntity.getUser().getId(), userId) || !Objects.equals(folderEntity.getUser().getId(), userId))
                throw new AuthException();
        }

        String oldPatch = fileEntity.getPatch();

        fileEntity.setFolder(folderEntity);
        fileEntity.setDateUpdate(new Date());

        Boolean renameSuccess = fileManager.moveFolder(
                oldPatch,
                fileEntity.getPatch()
        );

        if(!renameSuccess)
            throw new Exception();

        return new FileDto(fileRepository.save(fileEntity));
    }

    @SneakyThrows
    public FileDto updatePublic(Long fileId, Long userId) {
        Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);

        if(optionalFileEntity.isEmpty()) throw new NotFoundException("file not found");

        FileEntity fileEntity = optionalFileEntity.get();

        if(!Objects.equals(fileEntity.getUser().getId(), userId)) throw new AuthException();

        fileEntity.setIsPublic(!fileEntity.getIsPublic());
        fileEntity.setDateUpdate(new Date());

        return new FileDto(fileRepository.save(fileEntity));
    }

    @SneakyThrows
    @Transient
    public void delete(Long fileId, Long userId) {
        Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);

        if(optionalFileEntity.isEmpty())
            throw new NotFoundException("file not found");

        FileEntity fileEntity = optionalFileEntity.get();

        if(!Objects.equals(fileEntity.getUser().getId(), userId)) throw new AuthException();

        fileEntity.setInBasket(true);

        fileRepository.save(fileEntity);
    }

    @SneakyThrows
    public ResponseEntity<Resource> getFileResource(Long fileId, Long userId) {
        Optional<FileEntity> fileEntityOptional = fileRepository.findById(fileId);

        if(fileEntityOptional.isEmpty())
            throw new NotFoundException("not found file");

        FileEntity fileEntity = fileEntityOptional.get();

        if(userId == null && !fileEntity.isPublic)
            throw new AuthException();
        else if(userId != null && !fileEntity.isPublic && !Objects.equals(fileEntity.getUser().getId(), userId))
            throw new AuthException();

        return fileManager.get(fileEntity.getPatch());
    }

    @SneakyThrows
    public FileDto inBasketToFalse(Long fileId, Long userId) {
        Optional<FileEntity> fileEntityOptional = fileRepository.findById(fileId);

        if(fileEntityOptional.isEmpty()) throw new NotFoundException("not found file");

        FileEntity fileEntity = fileEntityOptional.get();

        if(!Objects.equals(fileEntity.getUser().getId(), userId)) throw new AuthException();

        fileEntity.setInBasket(false);

        return new FileDto(fileRepository.save(fileEntity));
    }

    public void deleteAllInBasket(Long userId) {
        List<FileEntity> folderEntities = fileRepository.findAllByUserId(userId, true);

        folderEntities.forEach(file -> {
            fileRepository.delete(file);
            fileManager.delete(file.getPatch());
        });
    }
}
