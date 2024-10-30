package ar.edu.utn.frc.tup.lc.iv.client;


import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.owner.OwnerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Service
public class OwnerRestClient {
    private final RestTemplate restTemplate;
    @Value("${app.api-owner}")
    private String ROOT_URL;
    @Autowired
    public OwnerRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<OwnerDto[]> getOwnerPlot() {
        String url = ROOT_URL + "/owners";
        return restTemplate.getForEntity(url, OwnerDto[].class);
    }

    public String getOwnerFullName(Integer ownerId) {
       OwnerDto[] owners = getOwnerPlot().getBody();
         if (owners != null) {
              for (OwnerDto owner : owners) {
                if (owner.getId().equals(ownerId)) {
                     return owner.getName() + " " + owner.getLastName();
                }
              }
         }
            return null;
    }
}
