package blps.duo.project.services;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    private final TransactionalOperator requiredTransactionalOperator;

    @Value("${minio.bucket.name}")
    private String bucketName;

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);


    public Flux<ByteBuffer> downloadFileOrDefault(String objectName) {
        if ("default-recipe-logo.jpeg".equals(objectName)) {
            return downloadDefaultImage();
        } else {
            return downloadFile(objectName);
        }
    }


    public Mono<String> uploadLogo(FilePart file) {
        String logoId = UUID.randomUUID().toString();
        String fileName = logoId + "_" + file.filename();

        logger.info("Uploading logo with filename: {}", fileName);

        return requiredTransactionalOperator.transactional(
                DataBufferUtils.join(file.content())
                        .flatMap(dataBuffer -> {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            baos.write(bytes, 0, bytes.length);

                            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                            try {
                                HttpHeaders headers = file.headers();
                                String contentType = (headers != null && headers.getContentType() != null)
                                        ? headers.getContentType().toString()
                                        : "application/octet-stream";

                                minioClient.putObject(
                                        PutObjectArgs.builder()
                                                .bucket(bucketName)
                                                .object(fileName)
                                                .stream(bais, baos.size(), 10485760)
                                                .contentType(contentType)
                                                .build()
                                );

                                String url = minioClient.getPresignedObjectUrl(
                                        GetPresignedObjectUrlArgs.builder()
                                                .method(Method.GET)
                                                .bucket(bucketName)
                                                .object(fileName)
                                                .build()
                                );

                                logger.info("Successfully uploaded logo with URL: {}", url);
                                return Mono.just(url);
                            } catch (Exception e) {
                                logger.error("Error uploading logo: {}", e.getMessage(), e);
                                return Mono.error(e);
                            } finally {
                                try {
                                    bais.close();
                                    baos.close();
                                } catch (IOException e) {
                                    logger.error("Error closing streams after uploading logo", e);
                                    return Mono.error(e);
                                }
                            }
                        })
        );
    }



    public Flux<ByteBuffer> downloadFile(String objectName) {
        return Mono.fromCallable(() -> {
                    StatObjectResponse statObjectResponse = minioClient.statObject(
                            StatObjectArgs.builder().bucket(bucketName).object(objectName).build()
                    );
                    logger.info("Object size: {}", statObjectResponse.size());
                    return statObjectResponse;
                })
                .flatMapMany(stat -> {
                    if (stat.size() > 0) {
                        return Flux.usingWhen(
                                Mono.fromCallable(() -> minioClient.getObject(GetObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(objectName)
                                        .build())),
                                responseInputStream -> Flux.<ByteBuffer>generate(sink -> {
                                    byte[] buffer = new byte[4096];
                                    try {
                                        int bytesRead = responseInputStream.read(buffer);
                                        if (bytesRead > 0) {
                                            sink.next(ByteBuffer.wrap(buffer, 0, bytesRead));
                                        } else {
                                            sink.complete();
                                        }
                                    } catch (Exception e) {
                                        sink.error(e);
                                    }
                                }),
                                responseInputStream -> Mono.fromRunnable(() -> {
                                    try {
                                        responseInputStream.close();
                                    } catch (Exception e) {
                                        throw new RuntimeException("Failed to close the response stream", e);
                                    }
                                })
                        );
                    } else {
                        return Flux.error(new RuntimeException("No such object in the bucket"));
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error downloading file from MinIO: {}", e.getMessage());
                    return Flux.empty();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }


    public Mono<Void> deleteLogo(String objectName) {
        return Mono.fromRunnable(() -> {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build()
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete object from MinIO", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private Flux<ByteBuffer> downloadDefaultImage() {
        return Mono.fromCallable(() -> {
                    File defaultImageFile = new File("main-service/src/main/resources/static/images/default-recipe-logo.jpg");
                    if (!defaultImageFile.exists()) {
                        throw new FileNotFoundException("Default image not found");
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (FileInputStream fis = new FileInputStream(defaultImageFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                    }
                    return ByteBuffer.wrap(baos.toByteArray());
                })
                .flux()
                .subscribeOn(Schedulers.boundedElastic());
    }


}
