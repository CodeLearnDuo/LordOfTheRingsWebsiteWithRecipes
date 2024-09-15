package blps.duo.project.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CustomFilePart implements FilePart {
    private final String filename;
    private final InputStream inputStream;
    private final String mimeType;
    private static final Logger logger = LoggerFactory.getLogger(CustomFilePart.class);


    public CustomFilePart(String filename, InputStream inputStream, String mimeType) {
        this.filename = filename;
        this.inputStream = inputStream;
        this.mimeType = mimeType;
    }

    @Override
    public String filename() {
        return filename;
    }

    @Override
    public Mono<Void> transferTo(File dest) {
        return FilePart.super.transferTo(dest);
    }

    @Override
    public Mono<Void> transferTo(java.nio.file.Path dest) {
        return Mono.fromRunnable(() -> {
            logger.info("Transferring file to destination: {}", dest);
            try (InputStream is = this.inputStream) {
                Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
                logger.info("File successfully transferred");
            } catch (IOException e) {
                logger.error("Error transferring file: {}", e.getMessage(), e);
                throw new RuntimeException("Error transferring file to destination", e);
            }
        });
    }


    @Override
    public String name() {
        return "logo_field";
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(this.mimeType));
        return headers;
    }


    @Override
    public Flux<DataBuffer> content() {
        try {
            logger.info("Reading content from file: {}", filename);
            return Flux.just(new DefaultDataBufferFactory().wrap(inputStream.readAllBytes()));
        } catch (IOException e) {
            logger.error("Error reading file content: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> delete() {
        return FilePart.super.delete();
    }

    public String getMimeType() {
        return mimeType;
    }
}
