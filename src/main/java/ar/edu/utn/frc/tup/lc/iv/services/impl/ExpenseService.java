package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.comunication.FileServerRestClient;
import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.common.*;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseDistributionEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.ExpenseInstallmentEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseCategoryModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseDistributionModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseInstallmentModel;
import ar.edu.utn.frc.tup.lc.iv.models.ExpenseModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseCategoryRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseDistributionRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseInstallmentRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ExpenseService implements IExpenseService {

    @Autowired
    private  ExpenseRepository expenseRepository;
    @Autowired
    private ExpenseDistributionRepository expenseDistributionRepository;
    @Autowired
    private ExpenseInstallmentRepository expenseInstallmentRepository;
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ExpenseCategoryService expenseCategoryService;
    @Autowired
    private FileServerRestClient fileServerRestClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public DtoResponseExpense postExpense(DtoRequestExpense request,MultipartFile file) {

        Boolean expenseExist = fetchValidExpenseModel(request);
        if(!expenseExist)
        {
            ExpenseModel expenseModel = setDataToExpenseModel(request);
            List<ExpenseInstallmentModel> expenseInstallmentModels = setExpenseInstallmentModels(request,expenseModel);
            List<ExpenseDistributionModel> expenseDistributionModels = setExpenseDistributionModels(request);
            expenseModel.setInstallmentsList(expenseInstallmentModels);
            expenseModel.setDistributions(expenseDistributionModels);
            UUID fileId = UUID.randomUUID(); // remplazar por fileServerRestClient
            expenseModel.setFileId(fileId);
            DtoResponseExpense dtoResponseExpense = setDtoResponseExpense(expenseModel);
            ExpenseEntity savedExpenseEntity = saveExpenseEntity(expenseModel, expenseInstallmentModels, expenseDistributionModels);
            return dtoResponseExpense;

        }
        {
            throw new CustomException("The expense is not valid", HttpStatus.CONFLICT);
        }



    }

    private ExpenseEntity saveExpenseEntity(ExpenseModel expenseModel, List<ExpenseInstallmentModel> expenseInstallmentModels, List<ExpenseDistributionModel> expenseDistributionModels) {
        ExpenseEntity expenseEntity = modelMapper.map(expenseModel, ExpenseEntity.class);
        expenseEntity.setDistributions(new ArrayList<>());
        expenseEntity.setInstallmentsList(new ArrayList<>());
        for (ExpenseDistributionModel distributionModel : expenseDistributionModels) {
            ExpenseDistributionEntity distributionEntity = modelMapper.map(distributionModel, ExpenseDistributionEntity.class);
            distributionEntity.setExpense(expenseEntity);
            expenseEntity.getDistributions().add(distributionEntity);
        }

        for (ExpenseInstallmentModel installmentModel : expenseInstallmentModels) {
            ExpenseInstallmentEntity expenseInstallmentEntity = modelMapper.map(installmentModel, ExpenseInstallmentEntity.class);
            expenseInstallmentEntity.setExpense(expenseEntity);
            expenseEntity.getInstallmentsList().add(expenseInstallmentEntity);
        }

        return expenseRepository.save(expenseEntity);
    }


    private DtoResponseExpense setDtoResponseExpense(ExpenseModel expenseModel) {
        DtoResponseExpense dtoResponseExpense = new DtoResponseExpense();
        dtoResponseExpense.setExpenseDate(expenseModel.getExpenseDate());
        dtoResponseExpense.setExpenseType(expenseModel.getExpenseType());
        dtoResponseExpense.setDescription(expenseModel.getDescription());
        DtoCategory dtoCategory = new DtoCategory();
        dtoCategory.setId(expenseModel.getCategory().getId());
        dtoCategory.setDescription(expenseModel.getCategory().getDescription());
        dtoResponseExpense.setDtoCategory(dtoCategory);
        dtoResponseExpense.setInvoiceNumber(expenseModel.getInvoiceNumber());
        dtoResponseExpense.setProviderId(expenseModel.getProviderId());
        List<DtoInstallment> dtoInstallments = new ArrayList<>();
        for(ExpenseInstallmentModel expenseInstallmentModel : expenseModel.getInstallmentsList())
        {
            DtoInstallment dtoInstallment = new DtoInstallment();
            dtoInstallment.setPaymentDate(expenseInstallmentModel.getPaymentDate());
            dtoInstallment.setInstallmentNumber(expenseInstallmentModel.getInstallmentNumber());
            dtoInstallments.add(dtoInstallment);
        }
        dtoResponseExpense.setDtoInstallmentList(dtoInstallments);
        List<DtoDistribution> dtoDistributions = new ArrayList<>();
        for(ExpenseDistributionModel expenseDistributionModel : expenseModel.getDistributions()){
            DtoDistribution dtoDistribution = new DtoDistribution();
            dtoDistribution.setProportion(expenseDistributionModel.getProportion());
            dtoDistribution.setOwnerId(expenseDistributionModel.getOwnerId());
            dtoDistributions.add(dtoDistribution);
        }
        dtoResponseExpense.setDtoDistributionList(dtoDistributions);
        return dtoResponseExpense;

    }

    private List<ExpenseInstallmentModel> setExpenseInstallmentModels(DtoRequestExpense request, ExpenseModel expenseModel) {
        //todo validaciones
        List<ExpenseInstallmentModel> expenseInstallmentModels = new ArrayList<>();
        Integer installments = 1;
        do {
            ExpenseInstallmentModel expenseInstallmentModel = new ExpenseInstallmentModel();
            expenseInstallmentModel.setInstallmentNumber(installments);
            expenseInstallmentModel.setEnabled(Boolean.TRUE);
            expenseInstallmentModel.setCreatedDatetime(LocalDateTime.now());
            expenseInstallmentModel.setCreatedUser(1);
            expenseInstallmentModel.setExpenseModel(expenseModel);
            if (installments.equals(1)) {
                expenseInstallmentModel.setPaymentDate(LocalDate.now());
            } else {
                expenseInstallmentModel.setPaymentDate(LocalDate.now().plusMonths(installments - 1)); // al usar
                // solo el valor de los installments y ir incrementandose en cada ciclo while los meses se sumaban de mas
            }

            expenseInstallmentModel.setLastUpdatedDatetime(LocalDateTime.now());
            expenseInstallmentModel.setLastUpdatedUser(1);
            expenseInstallmentModels.add(expenseInstallmentModel);
            installments++;
        } while (installments <= request.getInstallments());

        return expenseInstallmentModels;

    }

    private List<ExpenseDistributionModel> setExpenseDistributionModels(DtoRequestExpense request) {
         List<ExpenseDistributionModel> expenseDistributionModels = new ArrayList<>();
        for (DtoDistribution dtoDistribution : request.getDistributions()) {
            ExpenseDistributionModel expenseDistributionModel = new ExpenseDistributionModel();
            expenseDistributionModel.setEnabled(Boolean.TRUE);
            expenseDistributionModel.setCreatedUser(1);
            expenseDistributionModel.setLastUpdatedDatetime(LocalDateTime.now());
            expenseDistributionModel.setCreatedDatetime(LocalDateTime.now());
            expenseDistributionModel.setLastUpdatedUser(1);
            expenseDistributionModel.setOwnerId(dtoDistribution.getOwnerId());
            expenseDistributionModel.setProportion(dtoDistribution.getProportion());
            expenseDistributionModels.add(expenseDistributionModel);
        }
        return expenseDistributionModels;
    }

    private ExpenseModel setDataToExpenseModel(DtoRequestExpense request) {
        ExpenseModel expenseModel = new ExpenseModel();
        expenseModel.setDescription(request.getDescription());
        expenseModel.setProviderId(request.getProviderId());
        expenseModel.setExpenseDate(request.getExpenseDate());
        expenseModel.setInvoiceNumber(request.getInvoiceNumber());
        expenseModel.setExpenseType(ExpenseType.valueOf(request.getTypeExpense()));
        ExpenseCategoryModel expenseCategoryModel = expenseCategoryService.getCategoryModel(request.getCategoryId());
        expenseModel.setCategory(expenseCategoryModel);
        expenseModel.setAmount(request.getAmount());
        expenseModel.setInstallments(request.getInstallments());
        expenseModel.setCreatedDatetime(LocalDateTime.now());
        expenseModel.setLastUpdatedDatetime(LocalDateTime.now());
        expenseModel.setCreatedUser(1);
        expenseModel.setLastUpdatedUser(1);
        expenseModel.setEnabled(Boolean.TRUE);
        return expenseModel;

    }

    private Boolean fetchValidExpenseModel(DtoRequestExpense request) {

        Optional<ExpenseEntity> expenseEntityValidateExist =  expenseRepository.findExpenseEntitiesByInvoiceNumberAndProviderId(request.getInvoiceNumber(), request.getProviderId());
        if (expenseEntityValidateExist.isPresent()) {
            throw new CustomException("The expense have already exist", HttpStatus.BAD_REQUEST);
        }
        ExpenseCategoryModel expenseCategoryModel = expenseCategoryService.getCategoryModel(request.getCategoryId());
        if (expenseCategoryModel == null) {
            throw new CustomException("The category does not exist", HttpStatus.BAD_REQUEST);
        }
        return false;
    }

    public DtoExpenseQuery getExpenseById(Integer expenseId) {

        DtoExpenseQuery dtoExpenseQuery = new DtoExpenseQuery();
        //consultar todos los gastos de la base de datos
        ExpenseEntity expenseEntity = expenseRepository.findById(expenseId).orElseThrow(() -> new CustomException("The expense does not exist", HttpStatus.NOT_FOUND));
        if (!expenseEntity.getEnabled()){
            throw new CustomException("The expense does not exist", HttpStatus.NOT_FOUND);
        }
        dtoExpenseQuery= modelMapper.map(expenseEntity, DtoExpenseQuery.class);

        if (expenseEntity.getProviderId() != null) {
            dtoExpenseQuery.setProvider(getProvider(expenseEntity.getProviderId()));
        } else {
            dtoExpenseQuery.setProvider("");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = expenseEntity.getCreatedDatetime().format(formatter);
        dtoExpenseQuery.setCreatedDatetime(formattedDate);

        dtoExpenseQuery.setCategory(expenseEntity.getCategory().getDescription());
        dtoExpenseQuery.setDistributionList(new ArrayList<>());

        for (ExpenseDistributionEntity distributionEntity : expenseEntity.getDistributions()) {
            //get owner name and amount
            String ownerName= getOwnerFullName(distributionEntity.getOwnerId());
            BigDecimal amount = expenseEntity.getAmount().multiply(distributionEntity.getProportion());
            //set owner name and amount to dto
            DtoExpenseDistributionQuery dtoExpenseDistributionQuery = new DtoExpenseDistributionQuery();
            dtoExpenseDistributionQuery.setOwnerFullName(ownerName);
            dtoExpenseDistributionQuery.setAmount(amount);
            dtoExpenseDistributionQuery.setOwnerId(distributionEntity.getOwnerId());
            //add dto to list
           dtoExpenseQuery.getDistributionList().add(dtoExpenseDistributionQuery);
        }

        return dtoExpenseQuery;
    }


    public List<DtoExpenseQuery> getExpenses(String expenseType,String category, String provider, String dateFrom, String dateTo) {

        DtoExpenseQuery dtoExpenseQuery = new DtoExpenseQuery();
        List<DtoExpenseQuery> dtoExpenseQueryList = new ArrayList<>();

        //consultar todos los gastos de la base de datos
        List<ExpenseEntity> expenseEntityList = expenseRepository.findAll();

        //agrergar a la lista de gastos solo los que esten activos
        for (ExpenseEntity expenseEntity:expenseEntityList){
            if (!expenseEntity.getEnabled()){
                continue;
            }
            dtoExpenseQuery= getExpenseById(expenseEntity.getId());
            if (expenseType != null && !dtoExpenseQuery.getExpenseType().equalsIgnoreCase(expenseType)){
                continue;
            }
            if (category != null && !dtoExpenseQuery.getCategory().equalsIgnoreCase(category)){
                continue;
            }
            if (provider != null && !dtoExpenseQuery.getProvider().equalsIgnoreCase(provider)){
                continue;
            }
            if (dateFrom != null ){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                try {
                    LocalDate from = LocalDate.parse(dateFrom, formatter);
                    LocalDate created = LocalDate.parse(dtoExpenseQuery.getCreatedDatetime(), formatter);
                    if (created.isBefore(from)) {
                        continue;
                    }
                } catch (DateTimeParseException e) {
                    e.printStackTrace();
                    throw new CustomException("The date format is not correct", HttpStatus.BAD_REQUEST);
                }
            }
            if (dateTo != null ){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                try {
                    LocalDate to = LocalDate.parse(dateTo, formatter);
                    LocalDate created = LocalDate.parse(dtoExpenseQuery.getCreatedDatetime(), formatter);
                    if (created.isAfter(to)) {
                        continue;
                    }
                } catch (DateTimeParseException e) {
                    e.printStackTrace();
                    throw new CustomException("The date format is not correct", HttpStatus.BAD_REQUEST);
                }
            }

            dtoExpenseQueryList.add(dtoExpenseQuery);
        }

        return dtoExpenseQueryList;
    }
    private String getOwnerFullName(Integer ownerId) {
        //buscar en la api de propietarios el nombre del propietario por cada expensa que venga
        String ownerFullName="";
        String url="https://my-json-server.typicode.com/405786MoroBenjamin/users-responses/owners?id=";
        String response= restTemplate.getForObject(url+ownerId, String.class);

        try {
            List<HashMap<String, Object>> seccionMapList = objectMapper.readValue(response, List.class);
            for (HashMap<String, Object> seccionMap : seccionMapList) {
                ownerFullName= (String) seccionMap.get("name");
                ownerFullName+=" "+(String) seccionMap.get("surname");
            }
        }catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("The owner does not exist", HttpStatus.NOT_FOUND);
        }
        return ownerFullName;
    }
    public String getProvider(int providerId) {
        //buscar en la api de proveedores el nombre del proveedor por cada expensa que venga
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
        //return providerName;
        return "empresa anonima";

    }
}
