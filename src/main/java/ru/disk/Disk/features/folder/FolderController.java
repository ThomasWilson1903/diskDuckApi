package ru.disk.Disk.features.folder;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.disk.Disk.features.file.FileService;
import ru.disk.Disk.features.file.dto.FileDto;
import ru.disk.Disk.features.folder.dto.FolderDto;
import ru.disk.Disk.features.folder.entity.FolderEntity;
import ru.disk.Disk.features.user.UserService;
import ru.disk.Disk.features.user.dto.JwtAuthentication;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("folders")
@Tag(name = "Folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('BASE_USER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<FolderDto>> getAll(
            @RequestParam(name = "folder_id", required = false) Long folderId,
            @RequestParam(name = "in_basket", defaultValue = "false") Boolean inBasket,
            @RequestParam(name = "page", defaultValue = "0") int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
            HttpServletRequest request
    ){
        JwtAuthentication user = userService.getAuthInfo(request);

        return ResponseEntity.ok(folderService.getAll(folderId, user.getId(), inBasket, pageNumber, pageSize));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BASE_USER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FolderDto> add(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "folder_id", required = false) Long folderId,
            HttpServletRequest request
    ){
        JwtAuthentication user = userService.getAuthInfo(request);

        return ResponseEntity.ok(folderService.add(name, folderId, user.getId()));
    }

    @RequestMapping(value = "/size", method = RequestMethod.HEAD)
    public ResponseEntity<String> getSize(
            @RequestParam(name = "folder_id") Long folderId
    ){
        HttpHeaders responseHeaders = new HttpHeaders();

        responseHeaders.set("folder_size", folderService.getSize(folderId).toString());

        return new ResponseEntity("success", responseHeaders, HttpStatus.OK);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('BASE_USER')")
    @SecurityRequirement(name = "bearerAuth")
    public void delete(
            @RequestParam(name = "folder_id") Long folderId,
            HttpServletRequest request
    ){
        JwtAuthentication user = userService.getAuthInfo(request);

        folderService.delete(folderId, user.getId());
    }

    @PatchMapping("/public")
    @PreAuthorize("hasAuthority('BASE_USER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FolderDto> updatePublic(
            @RequestParam(name = "folder_id") Long folderId,
            HttpServletRequest request
    ){
        JwtAuthentication user = userService.getAuthInfo(request);

        return ResponseEntity.ok(folderService.updatePublic(folderId, user.getId()));
    }

    @PatchMapping("/rename")
    @PreAuthorize("hasAuthority('BASE_USER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FolderDto> rename(
            @RequestParam(name = "folder_id") Long folderId,
            @RequestParam(name = "name") String name,
            HttpServletRequest request
    ){
        JwtAuthentication user = userService.getAuthInfo(request);

        return ResponseEntity.ok(folderService.rename(user.getId(), folderId, name));
    }

    @PatchMapping("/move")
    @PreAuthorize("hasAuthority('BASE_USER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FolderDto> updateFolder(
            @RequestParam(name = "folder_id") Long folderId,
            @RequestParam(name = "new_folder_id", required = false) Long newFolderId,
            HttpServletRequest request
    ){
        JwtAuthentication user = userService.getAuthInfo(request);

        return ResponseEntity.ok(folderService.updateFolder(user.getId(), folderId, newFolderId));
    }

    @PatchMapping("in_basket/to_false")
    @PreAuthorize("hasAuthority('BASE_USER')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FolderDto> inBasketToFalse(
            @RequestParam(name = "folder_id") Long folderId,
            HttpServletRequest request
    ) {
        JwtAuthentication user = userService.getAuthInfo(request);

        return ResponseEntity.ok(folderService.inBasketToFalse(folderId, user.getId()));
    }
}
