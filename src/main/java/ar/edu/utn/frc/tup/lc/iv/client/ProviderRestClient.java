package ar.edu.utn.frc.tup.lc.iv.client;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Service
public class ProviderRestClient {

    private final RestTemplate restTemplate;
    private static final String ROOT_URL = "https://my-json-server.typicode.com/EbeltramoUtn/demoTP";
    @Autowired
    public ProviderRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Autowired
    private ObjectMapper objectMapper;

    public String getProvider(int providerId) {
//        buscar en la api de proveedores el nombre del proveedor por cada expensa que venga
//        String providerName="";
//        String url="https://my-json-server.typicode.com/405786MoroBenjamin/users-responses/providers?id=";
//        String response= restTemplate.getForObject(url+providerId, String.class);
//
//        try {
//            List<HashMap<String, Object>> seccionMapList = objectMapper.readValue(response, List.class);
//            for (HashMap<String, Object> seccionMap : seccionMapList) {
//                providerName= (String) seccionMap.get("name");
//            }
//        }catch (IOException e) {
//            e.printStackTrace();
//            throw new CustomException("The provider does not exist", HttpStatus.NOT_FOUND);
//        }
//        return providerName;
        return "empresa anonima";

    }
}
