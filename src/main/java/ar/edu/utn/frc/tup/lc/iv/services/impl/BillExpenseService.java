package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.client.OwnerRestClient;
import ar.edu.utn.frc.tup.lc.iv.client.SanctionRestClient;
import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.response.BillExpenseDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.owner.OwnerDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.sanction.FineDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.OwnerPlotDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseFineEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseInstallmentsEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseOwnerEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.BillRecordEntity;
import ar.edu.utn.frc.tup.lc.iv.models.*;
import ar.edu.utn.frc.tup.lc.iv.repositories.BillRecordRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IBillExpenseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BillExpenseService implements IBillExpenseService {

    private final BillRecordRepository billRecordRepository;
    private final ModelMapper modelMapper;
    private final OwnerRestClient ownerRestClient;
    private final SanctionRestClient sanctionRestClient;
    @Autowired
    public BillExpenseService(BillRecordRepository billRecordRepository, ModelMapper modelMapper,
                              OwnerRestClient ownerRestClient, SanctionRestClient sanctionRestClient) {
        this.billRecordRepository = billRecordRepository;
        this.modelMapper = modelMapper;
        this.ownerRestClient = ownerRestClient;
        this.sanctionRestClient = sanctionRestClient;
    }
    @Override
    public BillExpenseDto generateBillExpense(PeriodDto periodDto) {
        //Validar que el rango sea correcto
        //Validar si hay expensa con ese rango
        BillRecordModel billRecordModel = getBillRecord(periodDto);
        if (billRecordModel == null) {
            //Si no hay expensa, validar que no este superpuesta en rango
            if(existBillRecordInPeriod(periodDto))
                throw new CustomException("El periodo indicado se superpone con otro ya generado", HttpStatus.CONFLICT);

            calculateBillExpense(periodDto);


        }


        //Si esta tod0 ok, buscar owner y fines
        //Traer expensas
        return null;
    }

    private BillRecordModel getBillRecord(PeriodDto periodDto) {
        BillRecordModel result =null;
        Optional<BillRecordEntity> optBillRecordEntity = billRecordRepository.findFirstByStartAndEndAndEnabledTrue(periodDto.getStartDate(),periodDto.getEndDate());
        if (optBillRecordEntity.isPresent()) {
            result = entityToModel(optBillRecordEntity.get());
        }

        return result;
    }
    private boolean existBillRecordInPeriod(PeriodDto periodDto) {
        boolean result = billRecordRepository.findAnyByStartAndEnd(periodDto.getStartDate(), periodDto.getEndDate()).isPresent();
        return result;
    }
    private BillRecordModel entityToModel(BillRecordEntity billRecordEntity) {
        BillRecordModel billRecordModel = modelMapper.map(billRecordEntity, BillRecordModel.class);
        billRecordModel.setBillExpenseOwner(new ArrayList<BillExpenseOwnerModel>());
        for (BillExpenseOwnerEntity entity : billRecordEntity.getBillExpenseOwner()) {
            BillExpenseOwnerModel billExpenseOwnerModel = modelMapper.map(entity, BillExpenseOwnerModel.class);
            billExpenseOwnerModel.setBillExpenseFines(new ArrayList<BillExpenseFineModel>());
            billExpenseOwnerModel.setBillExpenseInstallments(new ArrayList<BillExpenseInstallmentModel>());
            for (BillExpenseFineEntity fineEntity : entity.getBillExpenseFines()) {
                billExpenseOwnerModel.getBillExpenseFines().add(modelMapper.map(fineEntity, BillExpenseFineModel.class));
            }
            for (BillExpenseInstallmentsEntity installmentEntity : entity.getBillExpenseInstallments()) {
                billExpenseOwnerModel.getBillExpenseInstallments().add(modelMapper.map(installmentEntity, BillExpenseInstallmentModel.class));
            }
            billRecordModel.getBillExpenseOwner().add(billExpenseOwnerModel);
        }
        return billRecordModel;
    }
    private List<OwnerDto> getOwners(){
        ResponseEntity<OwnerDto[]> response = ownerRestClient.getOwnerPlot();
        if (response.getStatusCode() != HttpStatus.OK)
            throw new CustomException("No se pudo obtener los propietarios", HttpStatus.BAD_REQUEST);
        return Arrays.asList(response.getBody());
    }
    private List<FineDto> getFines(PeriodDto periodDto) {
        ResponseEntity<FineDto[]> response = sanctionRestClient.getFines(periodDto);
        if(response.getStatusCode() != HttpStatus.OK)
            throw new CustomException("No se pudo obtener las Multas", HttpStatus.BAD_REQUEST);
        return Arrays.asList(response.getBody());
    }
    private List<ExpenseModel> getExpenses(PeriodDto periodDto) {
        //TODO implement
        return new ArrayList<ExpenseModel>();
    }
    private BillRecordModel calculateBillExpense(PeriodDto periodDto) {
        List<OwnerDto> ownersDto = getOwners();
        List<FineDto> finesDto = getFines(periodDto);
        List<ExpenseModel> expenseModels = getExpenses(periodDto);

    }

}
