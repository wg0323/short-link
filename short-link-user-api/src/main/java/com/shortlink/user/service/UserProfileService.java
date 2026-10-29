package com.shortlink.user.service;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.constant.RedisKeys;
import com.shortlink.common.exception.BizException;
import com.shortlink.user.dto.UserProfileVO;
import com.shortlink.user.entity.User;
import com.shortlink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 个人信息：查询、改昵称、改头像、改密码。
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final long MAX_AVATAR_SIZE = 2L * 1024 * 1024;

    private static final List<String> ALLOWED_AVATAR_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final AvatarStorage avatarStorage;

    public UserProfileVO profile(long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return toProfileVO(user);
    }

    public void updateNickname(long userId, String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, "昵称不能为空");
        }
        User update = new User();
        update.setId(userId);
        update.setNickname(nickname.trim());
        userMapper.updateById(update);
    }

    /**
     * 头像文件存 MinIO，库里只留对象名。
     */
    public UserProfileVO updateAvatar(long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR, "请选择头像图片");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BizException(ResultCode.PARAM_ERROR, "头像不能超过 2MB");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) {
            throw new BizException(ResultCode.PARAM_ERROR, "仅支持 jpg / png / webp / gif 格式");
        }

        String objectName = avatarStorage.upload(userId, file, extension);
        User update = new User();
        update.setId(userId);
        update.setAvatar(objectName);
        userMapper.updateById(update);
        return profile(userId);
    }

    /**
     * 修改密码后清掉刷新令牌，强制其它端重新登录。
     */
    public void changePassword(long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BizException(ResultCode.PASSWORD_ERROR, "原密码不正确");
        }
        if (oldPassword.equals(newPassword)) {
            throw new BizException(ResultCode.PARAM_ERROR, "新密码不能与原密码相同");
        }

        User update = new User();
        update.setId(userId);
        update.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);
        redisTemplate.delete(RedisKeys.refreshToken(userId));
        redisTemplate.delete(RedisKeys.refreshTokenPrevious(userId));
    }

    private UserProfileVO toProfileVO(User user) {
        return new UserProfileVO(user.getId(), user.getPhone(), user.getNickname(),
                avatarStorage.urlOf(user.getAvatar()), user.getCreatedAt());
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
