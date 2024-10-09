package ar.edu.utn.frc.tup.lc.iv.services.impl;

import ar.edu.utn.frc.tup.lc.iv.client.OwnerRestClient;
import ar.edu.utn.frc.tup.lc.iv.client.SanctionRestClient;
import ar.edu.utn.frc.tup.lc.iv.controllers.manageExceptions.CustomException;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.response.BillExpenseDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.response.BillOwnerDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.response.ItemDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.owner.OwnerDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.owner.PlotDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.sanction.FineDto;
import ar.edu.utn.frc.tup.lc.iv.dtos.billExpense.PeriodDto;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseFineEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseInstallmentsEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.BillExpenseOwnerEntity;
import ar.edu.utn.frc.tup.lc.iv.entities.BillRecordEntity;
import ar.edu.utn.frc.tup.lc.iv.enums.ExpenseType;
import ar.edu.utn.frc.tup.lc.iv.models.*;
import ar.edu.utn.frc.tup.lc.iv.repositories.BillRecordRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IBillExpenseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Generates a new BillExpense for a specific period. If a BillRecord already exists for the period,
     * it returns the existing BillRecord. If no record exists, it validates the period, calculates
     * the expenses, and saves a new BillRecord.
     * @param periodDto {@link PeriodDto} Contains the start and end date for the period.
     * @return {@link BillExpenseDto} The generated or existing BillExpense.
     */
    @Override
    public BillExpenseDto generateBillExpense(PeriodDto periodDto) {
        // Validate that the period is well-defined and does not include today
        validatePeriod(periodDto);
        BillRecordModel billRecordModel = getBillRecord(periodDto);

        if (billRecordModel == null) {
            // Validate that there is no overlap with other BillRecords
            if (existBillRecordInPeriod(periodDto)) {
                throw new CustomException("The specified period overlaps with an existing generated one", HttpStatus.CONFLICT);
            }
            // Calculate the bill expense for the given period
            billRecordModel = calculateBillExpense(periodDto);
            // Save the new BillRecord in the database
            billRecordModel = saveBillRecord(billRecordModel);
        }

        return billRecordModelToDto(billRecordModel);
    }

    /**
     * Retrieves a BillRecord from the database for the specified period if it exists.
     * @param periodDto {@link PeriodDto} Contains the start and end date for the period.
     * @return {@link BillRecordModel} The BillRecord for the period, or null if no record exists.
     */
    private BillRecordModel getBillRecord(PeriodDto periodDto) {
        BillRecordModel result = null;
        Optional<BillRecordEntity> optBillRecordEntity = billRecordRepository.findFirstByStartAndEndAndEnabledTrue(periodDto.getStartDate(), periodDto.getEndDate());
        if (optBillRecordEntity.isPresent()) {
            result = billRecordEntityToModel(optBillRecordEntity.get());
        }
        return result;
    }

    /**
     * Checks if there is an existing BillRecord that overlaps with the specified period.
     * @param periodDto {@link PeriodDto} Contains the start and end date for the period.
     * @return true if there is an overlapping BillRecord, false otherwise.
     */
    private boolean existBillRecordInPeriod(PeriodDto periodDto) {
        return billRecordRepository.findAnyByStartAndEnd(periodDto.getStartDate(), periodDto.getEndDate()).isPresent();
    }

    /**
     * Retrieves a list of owners from the owner service using a REST client.
     * @return List of {@link OwnerDto} with owner details.
     */
    private List<OwnerDto> getOwners() {
        ResponseEntity<OwnerDto[]> response = ownerRestClient.getOwnerPlot();
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new CustomException("Could not retrieve owners", HttpStatus.BAD_REQUEST);
        }
        return Arrays.asList(response.getBody());
    }

    /**
     * Retrieves a list of fines applicable within the specified period from the sanctions service.
     * @param periodDto {@link PeriodDto} Contains the start and end date for the period.
     * @return List of {@link FineDto} with fine details.
     */
    private List<FineDto> getFines(PeriodDto periodDto) {
        ResponseEntity<FineDto[]> response = sanctionRestClient.getFines(periodDto);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new CustomException("Could not retrieve fines", HttpStatus.BAD_REQUEST);
        }
        return Arrays.asList(response.getBody());
    }

    /**
     * Retrieves all expenses with installment payment dates within the specified period.
     * @param periodDto {@link PeriodDto} Contains the start and end date for the period.
     * @return List of {@link ExpenseModel} with applicable expenses.
     */
    private List<ExpenseModel> getExpenses(PeriodDto periodDto) {
        // TODO: Implement logic to retrieve expenses from the database or another service
        return new ArrayList<ExpenseModel>();
    }

    /**
     * Calculates the BillExpense for a specified period by distributing expenses among the owners.
     * @param periodDto {@link PeriodDto} Contains the start and end date for the period.
     * @return {@link BillRecordModel} The calculated bill.
     */
    private BillRecordModel calculateBillExpense(PeriodDto periodDto) {
        BillRecordModel result = new BillRecordModel();
        result.setStart(periodDto.getStartDate());
        result.setEnd(periodDto.getEndDate());

        // Retrieve the applicable expenses and distribute them among owners
        List<ExpenseModel> expenseModels = getExpenses(periodDto);
        result.setBillExpenseOwner(constraintBillExpenseOwners(getOwners(), getFines(periodDto)));

        // Calculate total field size across all owners
        Integer totalSize = result.getBillExpenseOwner().stream().mapToInt(BillExpenseOwnerModel::getFieldSize).sum();

        // Distribute non-individual expenses proportionally
        for (ExpenseModel expense : expenseModels.stream().filter(m -> !m.getExpenseType().equals(ExpenseType.INDIVIDUAL)).toList()) {
            expensesDistributionNotIndividual(result, expense, totalSize);
        }

        // Distribute individual expenses based on specified proportions
        for (ExpenseModel expense : expenseModels.stream().filter(m -> m.getExpenseType().equals(ExpenseType.INDIVIDUAL)).toList()) {
            expensesDistributionIndividual(result, expense);
        }

        return result;
    }

    /**
     * Generates a list of owners, calculates their total field size, and assigns applicable fines.
     * Uses a Map for faster lookup of fines by plotId to optimize performance.
     *
     * @param ownersDto List of {@link OwnerDto} representing the owners.
     * @param finesDto List of {@link FineDto} representing the fines.
     * @return List of {@link BillExpenseOwnerModel} with owners and their corresponding fines.
     */
    private List<BillExpenseOwnerModel> constraintBillExpenseOwners(List<OwnerDto> ownersDto, List<FineDto> finesDto) {
        List<BillExpenseOwnerModel> result = new ArrayList<>();

        // Create a Map that relates each plotId to its list of fines for fast lookup
        Map<Integer, List<FineDto>> plotIdToFinesMap = finesDto.stream()
                .collect(Collectors.groupingBy(FineDto::getPlotId));

        for (OwnerDto owner : ownersDto) {
            BillExpenseOwnerModel billExpenseOwnerModel = new BillExpenseOwnerModel();
            billExpenseOwnerModel.setOwnerId(owner.getId());
            billExpenseOwnerModel.setFieldSize(owner.getPlots().stream().mapToInt(PlotDto::getFieldSize).sum());
            billExpenseOwnerModel.setBillExpenseFines(new ArrayList<>());
            billExpenseOwnerModel.setBillExpenseInstallments(new ArrayList<>());

            // For each plot owned by the current owner, fetch the related fines from the Map
            for (PlotDto plot : owner.getPlots()) {
                // Get the list of fines for the current plotId from the Map, or an empty list if none exist
                List<FineDto> finesForPlot = plotIdToFinesMap.getOrDefault(plot.getId(), Collections.emptyList());

                // Add each fine related to this plot to the owner's list of fines
                for (FineDto fine : finesForPlot) {
                    BillExpenseFineModel billExpenseFineModel = new BillExpenseFineModel();
                    billExpenseFineModel.setFineId(fine.getId());
                    billExpenseFineModel.setAmount(fine.getAmount());
                    billExpenseFineModel.setDescription(fine.getDescription());
                    billExpenseOwnerModel.getBillExpenseFines().add(billExpenseFineModel);
                }
            }

            result.add(billExpenseOwnerModel);
        }

        return result;
    }
    /**
     * Distributes non-individual expenses proportionally among all owners based on their field size.
     * @param billRecordModel The BillRecord to which expenses will be added.
     * @param expenseModel The non-individual expense to distribute.
     * @param totalSize The total field size of all owners.
     */
    private void expensesDistributionNotIndividual(BillRecordModel billRecordModel, ExpenseModel expenseModel, Integer totalSize) {
        // Calculate the amount to distribute for each installment
        BigDecimal amountToInstall = getAmountToInstall(expenseModel);

        // Distribute the expense proportionally to each owner based on their field size
        for (BillExpenseOwnerModel billExpenseOwnerModel : billRecordModel.getBillExpenseOwner()) {
            BigDecimal proportionFieldSize = getProportionFieldSize(totalSize, billExpenseOwnerModel.getFieldSize());
            BigDecimal amount = amountToInstall.multiply(proportionFieldSize);
            expensesDistribution(billExpenseOwnerModel, expenseModel, billRecordModel.getStart(), billRecordModel.getEnd(), amount);
        }
    }

    /**
     * Distributes individual expenses based on the specific proportion assigned to each owner in the expense's distribution.
     * @param billRecordModel The BillRecord to which the individual expenses will be added.
     * @param expenseModel The individual expense to distribute.
     */
    private void expensesDistributionIndividual(BillRecordModel billRecordModel, ExpenseModel expenseModel) {
        // Calculate the amount to distribute for each installment
        BigDecimal amountToInstall = getAmountToInstall(expenseModel);

        // Distribute the expense only to the owners listed in the expense's distribution
        for (BillExpenseOwnerModel billExpenseOwnerModel :
                billRecordModel.getBillExpenseOwner().stream().filter(m -> expenseModel.getDistributions().stream()
                        .anyMatch(l -> l.getOwnerId().equals(m.getOwnerId()))).toList()) {

            // Find the specific distribution entry for the owner
            ExpenseDistributionModel expenseDistributionModel = expenseModel.getDistributions().stream()
                    .filter(m -> m.getOwnerId().equals(billExpenseOwnerModel.getOwnerId())).findFirst().get();

            BigDecimal amountToProportion = amountToInstall.multiply(expenseDistributionModel.getProportion());
            expensesDistribution(billExpenseOwnerModel, expenseModel, billRecordModel.getStart(), billRecordModel.getEnd(), amountToProportion);
        }
    }

    /**
     * Distributes the specified amount of an expense to an owner's installments, adjusting for the billing period.
     * @param billExpenseOwnerModel The owner to whom the expense is being distributed.
     * @param expenseModel The expense being distributed.
     * @param startDate The start date of the billing period.
     * @param endDate The end date of the billing period.
     * @param amount The amount to distribute to the owner.
     */
    private void expensesDistribution(BillExpenseOwnerModel billExpenseOwnerModel, ExpenseModel expenseModel,
                                      LocalDate startDate, LocalDate endDate, BigDecimal amount) {
        // Filter the installments that fall within the billing period
        for (ExpenseInstallmentModel installmentModel : getExpenseInstallmentsFilter(expenseModel.getInstallmentsList(), startDate, endDate)) {
            String description = installmentModel.getExpenseModel().getCategory().getDescription() + " - "
                    + installmentModel.getExpenseModel().getDescription();
            BillExpenseInstallmentModel billExpenseInstallmentModel = new BillExpenseInstallmentModel();
            billExpenseInstallmentModel.setAmount(amount);
            billExpenseInstallmentModel.setExpenseInstallment(installmentModel);
            billExpenseInstallmentModel.setDescription(description);

            // Add the calculated installment to the owner's bill
            billExpenseOwnerModel.getBillExpenseInstallments().add(billExpenseInstallmentModel);
        }
    }

    /**
     * Calculates the amount to charge for each installment of an expense.
     * @param expenseModel The expense for which the installment amount is being calculated.
     * @return {@link BigDecimal} The amount per installment.
     */
    private BigDecimal getAmountToInstall(ExpenseModel expenseModel) {
        // Total amount divided by the number of installments
        return expenseModel.getAmount().divide(new BigDecimal(expenseModel.getInstallments()));
    }

    /**
     * Calculates the proportional field size for an owner relative to the total field size of all owners.
     * @param totalFieldSize The total field size of all owners.
     * @param ownerFieldSize The field size of the specific owner.
     * @return {@link BigDecimal} The proportion of the owner's field size relative to the total.
     */
    private BigDecimal getProportionFieldSize(Integer totalFieldSize, Integer ownerFieldSize) {
        // Proportion = (ownerFieldSize / totalFieldSize)
        return (BigDecimal.valueOf(ownerFieldSize).multiply(BigDecimal.valueOf(100.00)))
                .divide(BigDecimal.valueOf(totalFieldSize)).divide(BigDecimal.valueOf(100.00));
    }

    /**
     * Filters the list of installments to only include those that fall within the specified billing period.
     * @param expenseInstallmentModels The list of installments to filter.
     * @param startDate The start date of the billing period.
     * @param endDate The end date of the billing period.
     * @return A list of {@link ExpenseInstallmentModel} that fall within the billing period.
     */
    private List<ExpenseInstallmentModel> getExpenseInstallmentsFilter(List<ExpenseInstallmentModel> expenseInstallmentModels,
                                                                       LocalDate startDate, LocalDate endDate) {
        return expenseInstallmentModels.stream()
                .filter(installment -> !installment.getPaymentDate().isBefore(startDate) &&
                        !installment.getPaymentDate().isAfter(endDate)).toList();
    }

    /**
     * Saves a BillRecord to the database and returns the saved model.
     * @param billRecordModel The BillRecord to save.
     * @return {@link BillRecordModel} The saved BillRecordModel.
     */
    private BillRecordModel saveBillRecord(BillRecordModel billRecordModel) {
        // Map BillRecordModel to BillRecordEntity for database storage
        BillRecordEntity billRecordEntity = billRecordModelToEntity(billRecordModel);
        billRecordEntity = billRecordRepository.saveAndFlush(billRecordEntity);
        return billRecordEntityToModel(billRecordEntity);
    }

    /**
     * Maps a {@link BillRecordEntity} to a {@link BillRecordModel}.
     * @param billRecordEntity The entity to map.
     * @return {@link BillRecordModel} The mapped model.
     */
    private BillRecordModel billRecordEntityToModel(BillRecordEntity billRecordEntity) {
        BillRecordModel billRecordModel = modelMapper.map(billRecordEntity, BillRecordModel.class);
        billRecordModel.setBillExpenseOwner(new ArrayList<>());

        // Map each BillExpenseOwnerEntity to BillExpenseOwnerModel
        for (BillExpenseOwnerEntity entity : billRecordEntity.getBillExpenseOwner()) {
            BillExpenseOwnerModel billExpenseOwnerModel = modelMapper.map(entity, BillExpenseOwnerModel.class);
            billExpenseOwnerModel.setBillExpenseFines(new ArrayList<>());
            billExpenseOwnerModel.setBillExpenseInstallments(new ArrayList<>());

            // Map fines and installments
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

    /**
     * Maps a {@link BillRecordModel} to a {@link BillRecordEntity}.
     * @param billRecordModel The model to map.
     * @return {@link BillRecordEntity} The mapped entity.
     */
    private BillRecordEntity billRecordModelToEntity(BillRecordModel billRecordModel) {
        BillRecordEntity billRecordEntity = modelMapper.map(billRecordModel, BillRecordEntity.class);
        billRecordEntity.setBillExpenseOwner(new ArrayList<>());

        // Map each BillExpenseOwnerModel to BillExpenseOwnerEntity
        for (BillExpenseOwnerModel ownerModel : billRecordModel.getBillExpenseOwner()) {
            BillExpenseOwnerEntity billExpenseOwnerEntity = modelMapper.map(ownerModel, BillExpenseOwnerEntity.class);
            billExpenseOwnerEntity.setBillExpenseFines(new ArrayList<>());
            billExpenseOwnerEntity.setBillExpenseInstallments(new ArrayList<>());

            // Map fines and installments
            for (BillExpenseFineModel fineModel : ownerModel.getBillExpenseFines()) {
                BillExpenseFineEntity fineEntity = modelMapper.map(fineModel, BillExpenseFineEntity.class);
                billExpenseOwnerEntity.getBillExpenseFines().add(fineEntity);
            }
            for (BillExpenseInstallmentModel installmentModel : ownerModel.getBillExpenseInstallments()) {
                BillExpenseInstallmentsEntity installmentEntity = modelMapper.map(installmentModel, BillExpenseInstallmentsEntity.class);
                billExpenseOwnerEntity.getBillExpenseInstallments().add(installmentEntity);
            }

            billRecordEntity.getBillExpenseOwner().add(billExpenseOwnerEntity);
        }

        return billRecordEntity;
    }

    /**
     * Maps a {@link BillRecordModel} to a {@link BillExpenseDto}.
     * @param billRecordModel The model to map.
     * @return {@link BillExpenseDto} The mapped DTO.
     */
    private BillExpenseDto billRecordModelToDto(BillRecordModel billRecordModel) {
        BillExpenseDto billExpenseDto = new BillExpenseDto();
        billExpenseDto.setId(billRecordModel.getId());
        billExpenseDto.setStartDate(billRecordModel.getStart());
        billExpenseDto.setEndDate(billRecordModel.getEnd());
        billExpenseDto.setOwners(new ArrayList<>());

        // Map each BillExpenseOwnerModel to BillOwnerDto
        for (BillExpenseOwnerModel ownerModel : billRecordModel.getBillExpenseOwner()) {
            billExpenseDto.getOwners().add(billOwnerModelToDto(ownerModel));
        }

        return billExpenseDto;
    }

    /**
     * Maps a {@link BillExpenseOwnerModel} to a {@link BillOwnerDto}.
     * @param ownerModel The model to map.
     * @return {@link BillOwnerDto} The mapped DTO.
     */
    private BillOwnerDto billOwnerModelToDto(BillExpenseOwnerModel ownerModel) {
        BillOwnerDto ownerDto = new BillOwnerDto();
        ownerDto.setId(ownerModel.getOwnerId());
        ownerDto.setFieldSize(ownerModel.getFieldSize());
        ownerDto.setExpenses_extraordinary(new ArrayList<>());
        ownerDto.setFines(new ArrayList<>());
        ownerDto.setExpenses_individual(new ArrayList<>());

        // Map fines and installments based on expense type
        for (BillExpenseFineModel fineModel : ownerModel.getBillExpenseFines()) {
            ownerDto.getFines().add(billFineModelToDto(fineModel));
        }
        for (BillExpenseInstallmentModel installmentModel : ownerModel.getBillExpenseInstallments()) {
            if (installmentModel.getExpenseInstallment().getExpenseModel().getExpenseType().equals(ExpenseType.COMUN)) {
                ownerDto.getExpenses_common().add(billInstallmentModelToDto(installmentModel));
            }
            if (installmentModel.getExpenseInstallment().getExpenseModel().getExpenseType().equals(ExpenseType.INDIVIDUAL)) {
                ownerDto.getExpenses_individual().add(billInstallmentModelToDto(installmentModel));
            }
            if (installmentModel.getExpenseInstallment().getExpenseModel().getExpenseType().equals(ExpenseType.EXTRAORDINARIO)) {
                ownerDto.getExpenses_extraordinary().add(billInstallmentModelToDto(installmentModel));
            }
        }

        return ownerDto;
    }

    /**
     * Maps a {@link BillExpenseFineModel} to an {@link ItemDto}.
     * @param fineModel The model to map.
     * @return {@link ItemDto} The mapped DTO.
     */
    private ItemDto billFineModelToDto(BillExpenseFineModel fineModel) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(fineModel.getFineId());
        itemDto.setAmount(fineModel.getAmount());
        itemDto.setDescription(fineModel.getDescription());
        return itemDto;
    }

    /**
     * Maps a {@link BillExpenseInstallmentModel} to an {@link ItemDto}.
     * @param installmentModel The model to map.
     * @return {@link ItemDto} The mapped DTO.
     */
    private ItemDto billInstallmentModelToDto(BillExpenseInstallmentModel installmentModel) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(installmentModel.getId());
        itemDto.setAmount(installmentModel.getAmount());
        itemDto.setDescription(installmentModel.getDescription());
        return itemDto;
    }
    /**
     * Validates the period provided in the {@link PeriodDto}.
     * - The start date must be earlier than the end date.
     * - Both start and end dates must be in the past (before today).
     * - The start date and end date cannot be the same.
     * - The end date cannot be today's date.
     *
     * @param periodDto The {@link PeriodDto} containing the start and end dates.
     * @throws CustomException if any validation condition is not met.
     */
    private void validatePeriod(PeriodDto periodDto) {
        LocalDate today = LocalDate.now();

        // Validate that the start date is before the end date
        if (periodDto.getStartDate().isAfter(periodDto.getEndDate())) {
            throw new CustomException("The start date must be earlier than the end date.", HttpStatus.BAD_REQUEST);
        }

        // Validate that the start date is in the past
        if (!periodDto.getStartDate().isBefore(today)) {
            throw new CustomException("The start date must be in the past.", HttpStatus.BAD_REQUEST);
        }

        // Validate that the end date is in the past and not today
        if (!periodDto.getEndDate().isBefore(today)) {
            throw new CustomException("The end date must be before today.", HttpStatus.BAD_REQUEST);
        }

        // Validate that the start date and end date are not the same
        if (periodDto.getStartDate().isEqual(periodDto.getEndDate())) {
            throw new CustomException("The start date and end date cannot be the same.", HttpStatus.BAD_REQUEST);
        }
    }
}

