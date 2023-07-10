package ru.disk.Disk.features.folder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.disk.Disk.features.folder.entity.FolderEntity;

import java.util.List;

@Repository
@Transactional
public interface FolderRepository extends JpaRepository<FolderEntity, Long> {

    @Query("SELECT u FROM folders u WHERE folder_id = ?1 AND user_id = ?2 AND in_basket = ?3")
    Page<FolderEntity> findByFolderId(Long folderId, Long userId, Boolean inBasket, Pageable pageable);

    @Query("SELECT u FROM folders u WHERE folder_id = NULL AND user_id = ?1 AND in_basket = ?2")
    Page<FolderEntity> findByFolderNull(Long userId, Boolean inBasket, Pageable pageable);

    @Modifying
    @Query("DELETE folders WHERE user_id = :userId AND in_basket = true")
    void deleteAllInBasket(@Param("userId") Long userId);

    @Query("SELECT u FROM folders u WHERE user_id = ?1 AND in_basket = ?3")
    List<FolderEntity> findAllByUserId(Long userId, Boolean inBasket);
}
