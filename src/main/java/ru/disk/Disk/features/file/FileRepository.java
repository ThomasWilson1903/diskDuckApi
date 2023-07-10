package ru.disk.Disk.features.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.disk.Disk.features.file.entity.FileEntity;

import java.util.List;

@Repository
@Transactional
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query("SELECT u FROM files u WHERE folder_id = ?1 AND user_id = ?2 AND in_basket = ?3")
    Page<FileEntity> findByFolderId(Long folderId, Long userId, Boolean inBasket, Pageable pageable);

    @Query("SELECT u FROM files u WHERE folder_id = NULL AND user_id = ?1 AND in_basket = ?2")
    Page<FileEntity> findByFolderNull(Long userId, Boolean inBasket, Pageable pageable);

    @Modifying
    @Query("DELETE files WHERE user_id = :userId AND in_basket = true")
    void deleteAllInBasket(@Param("userId") Long userId);

    @Query("SELECT u FROM files u WHERE user_id = ?1 AND in_basket = ?3")
    List<FileEntity> findAllByUserId(Long userId, Boolean inBasket);
}
