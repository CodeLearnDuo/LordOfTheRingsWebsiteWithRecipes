package blps.duo.project.services;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
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


    public Mono<String> uploadLogo(FilePart file) {
        String logoId = UUID.randomUUID().toString();
        String fileName = logoId + "_" + file.filename();

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
                                minioClient.putObject(
                                        PutObjectArgs.builder()
                                                .bucket(bucketName)
                                                .object(fileName)
                                                .stream(bais, baos.size(), 10485760)
                                                .contentType(file.headers().getContentType().toString())
                                                .build());
                                String url = minioClient.getPresignedObjectUrl(
                                        GetPresignedObjectUrlArgs.builder()
                                                .method(Method.GET)
                                                .bucket(bucketName)
                                                .object(fileName)
                                                .build());
                                return Mono.just(url);
                            } catch (Exception e) {
                                return Mono.error(e);
                            } finally {
                                try {
                                    bais.close();
                                    baos.close();
                                } catch (IOException e) {
                                    return Mono.error(e);
                                }
                            }
                        })
        );
    }

    public Flux<ByteBuffer> downloadFile(String objectName) {
        return Mono.fromCallable(() -> minioClient.statObject(
                        StatObjectArgs.builder().bucket(bucketName).object(objectName).build()
                ))
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
                .onErrorResume(e -> Flux.empty())
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


}
