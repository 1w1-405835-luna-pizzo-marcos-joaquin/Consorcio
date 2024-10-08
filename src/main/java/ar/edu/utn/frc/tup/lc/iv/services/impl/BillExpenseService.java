package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.BillExpenseDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseFineEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseInstallmentsEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseOwnerEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.BillRecordEntity;
import ar.edu.utn.frc.tup.lc.iv.models.BillExpenseFineModel;
import ar.edu.utn.frc.tup.lc.iv.models.BillExpenseInstallmentModel;
import ar.edu.utn.frc.tup.lc.iv.models.BillExpenseOwnerModel;
import ar.edu.utn.frc.tup.lc.iv.models.BillRecordModel;
import ar.edu.utn.frc.tup.lc.iv.repositories.BillRecordRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IBillExpenseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BillExpenseService implements IBillExpenseService {

    private final BillRecordRepository billRecordRepository;
    private final ModelMapper modelMapper;
    @Autowired
    public BillExpenseService(BillRecordRepository billRecordRepository, ModelMapper modelMapper) {
        this.billRecordRepository = billRecordRepository;
        this.modelMapper = modelMapper;
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
            //TODO get owners, fines and expense
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


}
