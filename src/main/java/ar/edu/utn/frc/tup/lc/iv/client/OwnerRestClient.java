package ar.edu.utn.frc.tup.lc.iv.client;

import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.OwnerPlotDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
public class OwnerRestClient {
    private RestTemplate restTemplate;
    private static final String ROOT_URL = "http://localhost:8080/api/v1/sanctions";
    @Autowired
    public OwnerRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<OwnerPlotDto[]> getOwnerPlot() {
        String url = ROOT_URL + "/ownerPlot";
        return restTemplate.getForEntity(url, OwnerPlotDto[].class);
    }
}
