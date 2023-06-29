package ru.disk.Disk.features.user;

import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.disk.Disk.features.user.dto.JwtAuthentication;
import ru.disk.Disk.features.user.dto.JwtRequestDto;
import ru.disk.Disk.features.user.dto.JwtResponseDto;
import ru.disk.Disk.features.user.dto.RegisterDto;
import ru.disk.Disk.features.user.entity.UserEntity;
import ru.disk.Disk.features.user.entity.UserRole;
import ru.disk.Disk.security.JwtProvider;
import ru.disk.Disk.utils.exceptions.AlreadyExistException;

import javax.security.auth.message.AuthException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ru.disk.Disk.security.JwtFilter.getTokenFromRequest;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtProvider jwtProvider;

    @SneakyThrows
    public JwtResponseDto register(RegisterDto dto) {

        if(userRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new AlreadyExistException("user.email.already_exist");
        }

        UserEntity entity = new UserEntity(dto);

        Set<UserRole> roles = new HashSet<>();

        roles.add(UserRole.BASE_USER);

        entity.setRoles(roles);

        userRepository.save(entity);

        return login(new JwtRequestDto(entity.getEmail(), entity.getPassword()));
    }

    @SneakyThrows
    public JwtResponseDto login(@NonNull JwtRequestDto authRequest) {
        final UserEntity user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new AuthException("Пользователь не найден"));
        if (user.getPassword().equals(authRequest.getPassword())) {
            final String accessToken = jwtProvider.generateAccessToken(user);
            final String refreshToken = jwtProvider.generateRefreshToken(user);
            refreshStorage.put(user.getEmail(), refreshToken);
            return new JwtResponseDto(accessToken, refreshToken);
        } else {
            throw new AuthException("Неправильный пароль");
        }
    }

    @SneakyThrows
    public JwtResponseDto getAccessToken(@NonNull String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String email = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(email);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final UserEntity user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new AuthException("Пользователь не найден"));
                final String accessToken = jwtProvider.generateAccessToken(user);
                return new JwtResponseDto(accessToken, null);
            }
        }
        return new JwtResponseDto(null, null);
    }

    @SneakyThrows
    public JwtResponseDto refresh(@NonNull String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String email = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(email);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final UserEntity user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new AuthException("Пользователь не найден"));
                final String accessToken = jwtProvider.generateAccessToken(user);
                final String newRefreshToken = jwtProvider.generateRefreshToken(user);
                refreshStorage.put(user.getEmail(), newRefreshToken);
                return new JwtResponseDto(accessToken, newRefreshToken);
            }
        }
        throw new AuthException("Невалидный JWT токен");
    }

    @SneakyThrows
    public JwtAuthentication getAuthInfo(HttpServletRequest request) {
        final String token = getTokenFromRequest(request);

        if(token != null && jwtProvider.validateAccessToken(token)){
            return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
        }

        throw new AuthException();
    }
}
