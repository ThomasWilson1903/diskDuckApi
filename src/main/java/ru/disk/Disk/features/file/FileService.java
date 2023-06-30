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

import javax.annotation.Resources;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;
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
            @Min(0) int pageNumber,
            @Min(1) @Max(100) int pageSize
    ) {
        Page<FileEntity> entities;

        if(folderId == null){
            entities = fileRepository.findByFolderNull(
                    PageRequest.of(pageNumber, pageSize)
            );
        }else {
            entities = fileRepository.findByFolderId(
                    folderId,
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
    public FileDto rename(Long fileId, String name) {
        Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);

        if(optionalFileEntity.isEmpty())
            throw new NotFoundException("file not found");

        FileEntity fileEntity = optionalFileEntity.get();
        String folderName;

        if(fileEntity.getFolder() == null)
            folderName = "main";
        else
            folderName = fileEntity.getFolder().getName() + "_" + fileEntity.getFolder().getId();

        String newPatch = "/resources/users/" +
                fileEntity.getUser().getEmail() +
                "/" + folderName + "/" + name + "." +
                fileEntity.getExpansion();

        fileEntity.setName(name);
        fileEntity.setDateUpdate(new Date());
        fileEntity.setPath(newPatch);

        FileDto fileDto =  new FileDto(fileRepository.save(fileEntity));

        fileManager.rename(
                fileEntity.path,
                newPatch
        );

        return fileDto;
    }

    @SneakyThrows
    @Transient
    public FileDto updateFolder(Long fileId, Long folderId) {
        Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);

        if(optionalFileEntity.isEmpty()) throw new NotFoundException("file not found");

        FileEntity fileEntity = optionalFileEntity.get();

        Optional<FolderEntity> optionalFolderEntity = folderRepository.findById(folderId);

        if(optionalFolderEntity.isEmpty()) throw new NotFoundException("file not found");

        FolderEntity folderEntity = optionalFolderEntity.get();

        String newPath = "/resources/users/" +
                fileEntity.getUser().getEmail() +
                "/" + folderEntity.getName() + "_" + folderEntity.getId() + "/" + fileEntity.getName() + "." +
                fileEntity.getExpansion();

        fileEntity.setFolder(folderEntity);
        fileEntity.setDateUpdate(new Date());
        fileEntity.setPath(newPath);

        FileDto fileDto = new FileDto(fileRepository.save(fileEntity));

        fileManager.rename(
                fileEntity.path,
                newPath
        );

        return fileDto;
    }

    @SneakyThrows
    public FileDto updatePublic(Long fileId) {
        Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);

        if(optionalFileEntity.isEmpty()) throw new NotFoundException("file not found");

        FileEntity fileEntity = optionalFileEntity.get();

        fileEntity.setIsPublic(!fileEntity.getIsPublic());
        fileEntity.setDateUpdate(new Date());

        return new FileDto(fileRepository.save(fileEntity));
    }

    @SneakyThrows
    @Transient
    public void delete(Long fileId) {
        Optional<FileEntity> optionalFileEntity = fileRepository.findById(fileId);

        if(optionalFileEntity.isEmpty())
            throw new NotFoundException("file not found");

        FileEntity fileEntity = optionalFileEntity.get();

        fileRepository.delete(fileEntity);

        fileManager.delete(fileEntity.getPath());
    }

    @SneakyThrows
    public ResponseEntity<Resource> getFileResource(String filePatch) {
        return fileManager.get(filePatch);
    }
}
