package com.shortlink.user.service;

import com.shortlink.common.api.ResultCode;
import com.shortlink.common.exception.BizException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * 头像的对象存储，落地在 MinIO。
 *
 * <p>库里只存对象名，对外地址由对象名拼出来：桶只对 {@code avatar/} 前缀开放匿名只读，
 * 因此这个地址不含签名、不会过期。改用 CDN 或换 endpoint 时只需改配置，不用洗数据。</p>
 */
@Service
@Slf4j
public class AvatarStorage {

    private final MinioClient minioClient;
    private final String endpoint;
    private final String bucket;

    public AvatarStorage(MinioClient minioClient,
                         @Value("${shortlink.minio.endpoint}") String endpoint,
                         @Value("${shortlink.minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.endpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        this.bucket = bucket;
    }

    /**
     * 上传头像并返回对象名。桶或只读策略缺失时自动补齐，首次上传即可用。
     */
    public String upload(long userId, MultipartFile file, String extension) {
        String objectName = "avatar/" + userId + "-" + UUID.randomUUID().toString().replace("-", "")
                + "." + extension;
        ensureBucketReady();
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(in, file.getSize(), -1)
                    .contentType(contentTypeOf(extension))
                    .build());
        } catch (Exception e) {
            log.error("头像上传失败 object={}", objectName, e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "头像上传失败，请稍后再试");
        }
        return objectName;
    }

    /**
     * 对象名转公开访问地址，空对象返回空串。
     */
    public String urlOf(String objectName) {
        if (objectName == null || objectName.trim().isEmpty()) {
            return "";
        }
        return endpoint + "/" + bucket + "/" + objectName;
    }

    private void ensureBucketReady() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(publicReadAvatarPolicy())
                    .build());
        } catch (ErrorResponseException e) {
            // 并发首次上传时桶可能已被其它请求创建
            log.debug("头像桶已存在 bucket={}", bucket);
        } catch (Exception e) {
            log.error("头像桶不可用 bucket={}", bucket, e);
            throw new BizException(ResultCode.SYSTEM_ERROR, "头像存储不可用，请稍后再试");
        }
    }

    /**
     * 只给 avatar/ 前缀匿名只读，桶内其它对象仍然需要凭证。
     */
    private String publicReadAvatarPolicy() {
        return "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},"
                + "\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucket + "/avatar/*\"]}]}";
    }

    private String contentTypeOf(String extension) {
        if ("png".equals(extension)) {
            return "image/png";
        }
        if ("webp".equals(extension)) {
            return "image/webp";
        }
        if ("gif".equals(extension)) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}
