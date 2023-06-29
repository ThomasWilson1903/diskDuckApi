package ru.disk.Disk.features.folder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.disk.Disk.features.folder.entity.FolderEntity;

public interface FolderRepository extends JpaRepository<FolderEntity, Long> {

    @Query("SELECT u FROM folders u WHERE folder_id = ?1")
    Page<FolderEntity> findByFolderId(Long folderId, Pageable pageable);

    @Query("SELECT u FROM folders u WHERE folder_id = NULL")
    Page<FolderEntity> findByFolderNull(Pageable pageable);
}
