package ru.disk.Disk.features.basket;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.disk.Disk.features.file.FileService;
import ru.disk.Disk.features.folder.FolderService;
import ru.disk.Disk.features.user.UserService;
import ru.disk.Disk.features.user.dto.JwtAuthentication;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("basket")
@Tag(name = "Basket")
public class BasketController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderService folderService;

    @DeleteMapping
    @PreAuthorize("hasAuthority('BASE_USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Transient
    public void delete(HttpServletRequest request) {
        JwtAuthentication user = userService.getAuthInfo(request);

        folderService.deleteAllInBasket(user.getId());
        fileService.deleteAllInBasket(user.getId());
    }
}
