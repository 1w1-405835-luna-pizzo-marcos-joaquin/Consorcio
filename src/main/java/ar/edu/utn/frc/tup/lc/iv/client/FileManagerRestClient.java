package ar.edu.utn.frc.tup.lc.iv.client;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.fileManager.FileResponseDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.fileManager.UuidResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class FileManagerRestClient {
    private final RestTemplate restTemplate;
    //TODO Ajustar url
    private static final String ROOT_URL = "http://localhost:8085/fileManager";

    public FileManagerRestClient(RestTemplate restTemplateParam) {
        this.restTemplate = restTemplateParam;
    }
    public ResponseEntity<UuidResponseDto> uploadFile(MultipartFile file, String hashMd5, String hashSha256) {
        String url = ROOT_URL + "/savefile";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Body with file and optional hashes
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource()); // Convert MultipartFile to Resource
        if (hashMd5 != null) {
            body.add("hashMd5", hashMd5);
        }
        if (hashSha256 != null) {
            body.add("hashSha256", hashSha256);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // Send POST request
            ResponseEntity<UuidResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    UuidResponseDto.class
            );
            return response;
        } catch (HttpClientErrorException e) {
            handleClientError(e);
            throw new CustomException("Unexpected error during file upload", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public FileResponseDto getFile(UUID uuid) {
        String url = ROOT_URL + "/getFile/" + uuid.toString();

        try {
            // Send GET request
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    byte[].class
            );

            // Retrieve file data (bytes)
            byte[] fileData = response.getBody();
            // Get headers (metadata)
            HttpHeaders headers = response.getHeaders();
            String sha256 = headers.getFirst("sha256");
            String fileName = headers.getFirst("fileName");
            String mimeType = headers.getFirst("mimeType");
            String extension = headers.getFirst("extension");

            return FileResponseDto.builder()
                    .fileName(fileName)
                    .extension(extension)
                    .mimeType(mimeType)
                    .sha256(sha256)
                    .bytes(fileData)
                    .build();
        } catch (HttpClientErrorException e) {
            handleClientError(e);
            throw new CustomException("Unexpected error during file retrieval", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Handles client-side errors and throws appropriate CustomExceptions
     * based on the status code and message from the server response.
     */
    private void handleClientError(HttpClientErrorException e) {
        HttpStatus statusCode = (HttpStatus) e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();
        //TODO caputrar bien los errores
        switch (statusCode) {
            case NOT_FOUND:
                throw new CustomException("File not found: " + responseBody, HttpStatus.NOT_FOUND, e);
            case CONFLICT:
                throw new CustomException("File integrity conflict: " + responseBody, HttpStatus.CONFLICT, e);
            case BAD_REQUEST:
                throw new CustomException("Invalid request: " + responseBody, HttpStatus.BAD_REQUEST, e);
            default:
                throw new CustomException("An unexpected error occurred: " + responseBody, HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
