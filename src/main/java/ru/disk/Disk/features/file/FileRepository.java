package ru.disk.Disk.features.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.disk.Disk.features.file.entity.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query("SELECT u FROM files u WHERE folder_id = ?1")
    Page<FileEntity> findByFolderId(Long folderId, Pageable pageable);
}
