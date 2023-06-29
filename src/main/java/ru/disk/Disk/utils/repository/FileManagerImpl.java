package ru.disk.Disk.utils.repository;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class FileManagerImpl implements FileManager {

    @Override
    public ResponseEntity<Resource> get(String filePath) throws IOException {
        File file = new File(filePath);

        Path path = Paths.get(filePath);

        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType(Files.probeContentType(path)))
                .body(resource);
    }

    @Override
    public String upload(MultipartFile file, String folderName) throws IOException {
        return upload(List.of(file), folderName).get(0);
    }

    @Override
    public List<String> upload(List<MultipartFile> files, String folderName) throws IOException {
        File folder = new File(folderName);
        ArrayList<String> filesPath = new ArrayList<>();

        if (!folder.exists()) {
            folder.mkdirs();
        }

        for (MultipartFile file: files) {
            if (file.isEmpty()) {
                continue;
            }

            byte[] bytes = file.getBytes();
            Path path = Paths.get(folderName + file.getOriginalFilename());
            Files.write(path, bytes);

            filesPath.add(folderName + file.getOriginalFilename());
        }

        return filesPath;
    }

    @Override
    public Boolean delete(String filePath) {
        try {
            File file = new File(filePath);

            if(!file.exists()) return false;

            return file.delete();
        }catch (Exception ex){
            return false;
        }
    }

    @Override
    public Boolean rename(String oldFilePath, String newFilePath) {
        try {
            File oldFile = new File(oldFilePath);
            File newFile = new File(newFilePath);

            if (newFile.exists())
                throw new IOException("file exists");

            return oldFile.renameTo(newFile);
        }catch (Exception ex){
            return false;
        }
    }

    @Override
    public Long getSizeFolder(String path) {
        long length = 0;
        File directory = new File(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile())
                length += file.length();
            else
                length += getSizeFolder(file.getPath());
        }
        return length;
    }

    @SneakyThrows
    @Override
    public void deleteFolder(String path) {
        FileUtils.deleteDirectory(new File(path));
    }
}
