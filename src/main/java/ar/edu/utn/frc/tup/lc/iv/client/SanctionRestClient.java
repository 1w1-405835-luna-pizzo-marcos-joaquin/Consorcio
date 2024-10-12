package ar.edu.utn.frc.tup.lc.iv.client;

import ar.edu.utn.frc.tup.lc.iv.dtos.sanction.FineDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SanctionRestClient {
    private final RestTemplate restTemplate;
    private static final String ROOT_URL = "https://my-json-server.typicode.com/EbeltramoUtn/demoTP";

    @Autowired
    public SanctionRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<FineDto[]> getFines(PeriodDto periodDto){
        String url = ROOT_URL + "/fines";
        return restTemplate.getForEntity(url, FineDto[].class);
    }
}
