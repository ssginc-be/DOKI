package com.ssginc.commonservice.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author Queue-ri
 */

@Component
@RequiredArgsConstructor
public class S3Uploader {
    private final AmazonS3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    /* 대표 이미지 업로드 - 1개의 이미지 파일만 필요 */
    // 대표 이미지 1장으로 MAIN_THUMBNAIL, SUB_THUMBNAIL 생성
    public void uploadStoreMainImage(MultipartFile mfile) throws IOException {

        // s3 업로드 데이터 생성
        ObjectMetadata objectMetadata = generateObjectMetaData(mfile);
        String fileName = mfile.getOriginalFilename();
        String imageKey = "original/thumb/" + fileName;

        // S3 업로드 -> Lambda 트리거 됨
        s3Client.putObject(new PutObjectRequest(bucket, imageKey, mfile.getInputStream(), objectMetadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        // S3 bucket은 private이므로 직접 접근하지 않음. 대신 CloudFront URL 사용
        // ex) ${cloudFrontDomain}/resize/thumb/400x400_${fileName}
        // String uploadImageUrl = s3Client.getUrl(bucket, imageKey).toString(); // 프로젝트에서 해당 방식 사용하지 않음.
        return;

    }

    /* 상세 조회 페이지 이미지 업로드 - 1 ~ 최대 5개의 이미지 파일 필요 */
    // 이미지 파일로 CONTENT_DETAIL 생성
    public void uploadStoreImages(List<MultipartFile> mfiles) throws IOException {

        for (MultipartFile mfile : mfiles) {

            // s3 업로드 데이터 생성
            ObjectMetadata objectMetadata = generateObjectMetaData(mfile);
            String fileName = mfile.getOriginalFilename();
            String imageKey = "original/content/" + fileName;

            // S3 업로드 -> Lambda 트리거 됨
            s3Client.putObject(new PutObjectRequest(bucket, imageKey, mfile.getInputStream(), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

        }
        // S3 bucket은 private이므로 직접 접근하지 않음. 대신 CloudFront URL 사용
        // ex) ${cloudFrontDomain}/resize/content/450xauto_${fileName}
        // String uploadImageUrl = s3Client.getUrl(bucket, imageKey).toString(); // 프로젝트에서 해당 방식 사용하지 않음.
        return;
    }


    private ObjectMetadata generateObjectMetaData(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());
        return objectMetadata;
    }

}
