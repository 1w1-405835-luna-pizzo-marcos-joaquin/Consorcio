package ar.edu.utn.frc.tup.lc.iv.comunication;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class FileServerRestClient {

    String baseResourceUrl = "http://localhost:8089/fileServer"; //mockeado
    private final RestTemplate restTemplate;

    public FileServerRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UUID saveFile(MultipartFile file) {
        return UUID.randomUUID(); /*restTemplate.postForObject(baseResourceUrl + "/save", file, UUID.class);*/
    }
}
