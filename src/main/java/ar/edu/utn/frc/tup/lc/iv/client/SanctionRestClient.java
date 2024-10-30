package ar.edu.utn.frc.tup.lc.iv.client;

import ar.edu.utn.frc.tup.lc.iv.dtos.sanction.FineDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SanctionRestClient {
    private final RestTemplate restTemplate;
    @Value("${app.api-sanction}")
    private String ROOT_URL;

    @Autowired
    public SanctionRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<FineDto[]> getFines(PeriodDto periodDto){
        String url = ROOT_URL + "/fines";
        return restTemplate.getForEntity(url, FineDto[].class);
    }
}
