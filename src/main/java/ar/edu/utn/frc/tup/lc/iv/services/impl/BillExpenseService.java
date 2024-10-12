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
import ar.edu.utn.frc.tup.lc.iv.repositories.BillExpenseInstallmentsRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.BillRecordRepository;
import ar.edu.utn.frc.tup.lc.iv.repositories.ExpenseRepository;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IBillExpenseService;
import ar.edu.utn.frc.tup.lc.iv.services.interfaces.IExpenseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Console;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
//TODO Ojo que se me agrego EXPENSE_TYPE NoteCredit debo verificar en que me molestas
@Service
public class BillExpenseService implements IBillExpenseService {

    private final BillRecordRepository billRecordRepository;
    private final ModelMapper modelMapper;
    private final OwnerRestClient ownerRestClient;
    private final SanctionRestClient sanctionRestClient;
    private final IExpenseService expenseService;
    private final BillExpenseInstallmentsRepository billExpenseInstallmentsRepository;

    @Autowired
    public BillExpenseService(BillRecordRepository billRecordRepository, ModelMapper modelMapper,
                              OwnerRestClient ownerRestClient, SanctionRestClient sanctionRestClient,
                              IExpenseService expenseService, BillExpenseInstallmentsRepository billExpenseInstallmentsRepository) {
        this.billRecordRepository = billRecordRepository;
        this.modelMapper = modelMapper;
        this.ownerRestClient = ownerRestClient;
        this.sanctionRestClient = sanctionRestClient;
        this.expenseService = expenseService;
        this.billExpenseInstallmentsRepository = billExpenseInstallmentsRepository;
    }

    //TODO Borrar esto y ajustar para cuando tengamos id de usuario que corresponde
    private final Integer CREATE_USER =1;
    private final Integer UPDATE_USER =1;

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
        return expenseService.getExpenseByPaymentDateRange(periodDto.getStartDate(), periodDto.getEndDate());
    }

    /**
     * Calculates the BillExpense for a specified period by distributing expenses among the owners.
     * @param periodDto {@link PeriodDto} Contains the start and end date for the period.
     * @return {@link BillRecordModel} The calculated bill.
     */
    private BillRecordModel calculateBillExpense(PeriodDto periodDto) {
        BillRecordModel result = BillRecordModel.builder()
                .start(periodDto.getStartDate())
                .end(periodDto.getEndDate())
                .build();
        result.setCreatedUser(CREATE_USER);
        result.setLastUpdatedUser(UPDATE_USER);

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

            billExpenseOwnerModel.setCreatedUser(CREATE_USER);
            billExpenseOwnerModel.setLastUpdatedUser(UPDATE_USER);

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

                    billExpenseFineModel.setCreatedUser(CREATE_USER);
                    billExpenseFineModel.setLastUpdatedUser(UPDATE_USER);

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
            BigDecimal amount = amountToInstall.multiply(proportionFieldSize).setScale(2, RoundingMode.HALF_UP);
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

            BigDecimal amountToProportion = amountToInstall.multiply(expenseDistributionModel.getProportion()).setScale(2, RoundingMode.HALF_UP);
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
            String description = expenseModel.getCategory().getDescription() + " - "
                    + expenseModel.getDescription();
            BillExpenseInstallmentModel billExpenseInstallmentModel = BillExpenseInstallmentModel.builder()
                    .amount(amount)
                    .expenseInstallment(installmentModel)
                    .description(description)
                    .expenseType(expenseModel.getExpenseType())
                    .build();

            billExpenseInstallmentModel.setCreatedUser(CREATE_USER);
            billExpenseInstallmentModel.setLastUpdatedUser(CREATE_USER);

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
        BigDecimal amount = expenseModel.getAmount();
        BigDecimal installments = BigDecimal.valueOf(expenseModel.getInstallments());
        if(installments.compareTo(BigDecimal.ZERO) <= 0){

            throw new CustomException("The installments must be greater than zero", HttpStatus.BAD_REQUEST);
        }
        return amount.divide(installments,2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the proportional field size for an owner relative to the total field size of all owners.
     * @param totalFieldSize The total field size of all owners.
     * @param ownerFieldSize The field size of the specific owner.
     * @return {@link BigDecimal} The proportion of the owner's field size relative to the total.
     */
    private BigDecimal getProportionFieldSize(Integer totalFieldSize, Integer ownerFieldSize) {
        // Proportion = (ownerFieldSize / totalFieldSize)
        BigDecimal ownerProportion = BigDecimal.valueOf(ownerFieldSize);
        BigDecimal totalSize = BigDecimal.valueOf(totalFieldSize);
        ownerProportion = ownerProportion.divide(totalSize,2,RoundingMode.HALF_UP);
        return ownerProportion;
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
    @Transactional
    protected BillRecordModel saveBillRecord(BillRecordModel billRecordModel) {
        // Map BillRecordModel to BillRecordEntity for database storage
        BillRecordEntity billRecordEntity = billRecordModelToEntity(billRecordModel);
        billRecordRepository.save(billRecordEntity);
        return billRecordEntityToModel(billRecordEntity);
    }

    /**
     * Maps a {@link BillRecordEntity} to a {@link BillRecordModel}.
     * @param billRecordEntity The entity to map.
     * @return {@link BillRecordModel} The mapped model.
     */
    private BillRecordModel billRecordEntityToModel(BillRecordEntity billRecordEntity) {
        return modelMapper.map(billRecordEntity, BillRecordModel.class);
    }

    /**
     * Maps a {@link BillRecordModel} to a {@link BillRecordEntity}.
     * This method also ensures that the relationships between {@link BillRecordEntity} and its
     * children ({@link BillExpenseOwnerEntity}, {@link BillExpenseFineEntity}, {@link BillExpenseInstallmentsEntity})
     * are properly set before the entity is persisted.
     *
     * @param billRecordModel The model to map.
     * @return {@link BillRecordEntity} The mapped entity with properly assigned relationships.
     */
    private BillRecordEntity billRecordModelToEntity(BillRecordModel billRecordModel) {
        BillRecordEntity billRecordEntity = modelMapper.map(billRecordModel, BillRecordEntity.class);

        // Ensure that each child (BillExpenseOwnerEntity) has a reference to its parent (BillRecordEntity)
        for (BillExpenseOwnerEntity ownerEntity : billRecordEntity.getBillExpenseOwner()) {
            if (ownerEntity.getBillRecord() == null) {
                ownerEntity.setBillRecord(billRecordEntity);
            }

            // Ensure each fine (BillExpenseFineEntity) has a reference to its owner (BillExpenseOwnerEntity)
            for (BillExpenseFineEntity fineEntity : ownerEntity.getBillExpenseFines()) {
                if (fineEntity.getBillExpenseOwner() == null) {
                    fineEntity.setBillExpenseOwner(ownerEntity);
                }
            }

            // Ensure each installment (BillExpenseInstallmentsEntity) has a reference to its owner (BillExpenseOwnerEntity)
            for (BillExpenseInstallmentsEntity installmentEntity : ownerEntity.getBillExpenseInstallments()) {
                if (installmentEntity.getBillExpenseOwner() == null) {
                    installmentEntity.setBillExpenseOwner(ownerEntity);
                }
            }
        }

        return billRecordEntity;
    }

    /**
     * Maps a {@link BillRecordModel} to a {@link BillExpenseDto}.
     * @param billRecordModel The model to map.
     * @return {@link BillExpenseDto} The mapped DTO.
     */
    private BillExpenseDto billRecordModelToDto(BillRecordModel billRecordModel) {
        try {
            Map<Integer,String> installmentsType = getInstallmentAndExpenseType(billRecordModel.getId());
            BillExpenseDto billExpenseDto = new BillExpenseDto();
            billExpenseDto.setId(billRecordModel.getId());
            billExpenseDto.setStartDate(billRecordModel.getStart());
            billExpenseDto.setEndDate(billRecordModel.getEnd());
            billExpenseDto.setOwners(new ArrayList<>());

            // Map each BillExpenseOwnerModel to BillOwnerDto
            for (BillExpenseOwnerModel ownerModel : billRecordModel.getBillExpenseOwner()) {
                billExpenseDto.getOwners().add(billOwnerModelToDto(ownerModel,installmentsType));
            }

            return billExpenseDto;
        }catch (Exception ex){
            // CustomException description: An error occurred while processing the BillRecord,
            // but the BillRecord exists or was generated. The return process failed, not the record generation.
            throw new CustomException("Error occurred processing the bill record. The record was created or already exists, " +
                    "but the return process failed.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Maps a {@link BillExpenseOwnerModel} to a {@link BillOwnerDto}.
     * @param ownerModel The model to map.
     * @return {@link BillOwnerDto} The mapped DTO.
     */
    private BillOwnerDto billOwnerModelToDto(BillExpenseOwnerModel ownerModel, Map<Integer,String> installmentsType) {
        BillOwnerDto ownerDto = BillOwnerDto.builder()
                .id(ownerModel.getId())
                .fieldSize(ownerModel.getFieldSize())
                .expenses_common(new ArrayList<>())
                .expenses_extraordinary(new ArrayList<>())
                .expenses_individual(new ArrayList<>())
                .fines(new ArrayList<>())
                .build();

        // Map fines and installments based on expense type
        for (BillExpenseFineModel fineModel : ownerModel.getBillExpenseFines()) {
            ownerDto.getFines().add(billFineModelToDto(fineModel));
        }
        for (BillExpenseInstallmentModel installmentModel : ownerModel.getBillExpenseInstallments()) {
            String type = installmentsType.get(installmentModel.getId());
            if (type == null)
                throw new CustomException("Expense Type is not defined", HttpStatus.BAD_REQUEST);
            ExpenseType typeInstallment = ExpenseType.valueOf(type);
            if (typeInstallment.equals(ExpenseType.COMUN)) {
                ownerDto.getExpenses_common().add(billInstallmentModelToDto(installmentModel));
            }
            if (typeInstallment.equals(ExpenseType.INDIVIDUAL)) {
                ownerDto.getExpenses_individual().add(billInstallmentModelToDto(installmentModel));
            }
            if (typeInstallment.equals(ExpenseType.EXTRAORDINARIO)) {
                ownerDto.getExpenses_extraordinary().add(billInstallmentModelToDto(installmentModel));
            }
        }

        return ownerDto;
    }

    /**
     * Maps a {@link BillExpenseFineModel} to an {@link ItemDto}.
     * This method uses the builderItemDto method to create an {@link ItemDto} from the fine model.
     *
     * @param fineModel The model to map.
     * @return {@link ItemDto} The mapped DTO, containing the fine ID, amount, and description.
     */
    private ItemDto billFineModelToDto(BillExpenseFineModel fineModel) {
        return builderItemDto(fineModel.getFineId(), fineModel.getAmount(), fineModel.getDescription());
    }

    /**
     * Maps a {@link BillExpenseInstallmentModel} to an {@link ItemDto}.
     * This method uses the builderItemDto method to create an {@link ItemDto} from the installment model.
     *
     * @param installmentModel The model to map.
     * @return {@link ItemDto} The mapped DTO, containing the installment ID, amount, and description.
     */
    private ItemDto billInstallmentModelToDto(BillExpenseInstallmentModel installmentModel) {
        return builderItemDto(installmentModel.getId(), installmentModel.getAmount(), installmentModel.getDescription());
    }

    /**
     * Creates an {@link ItemDto} object using the provided id, amount, and description.
     * This is a helper method to standardize the creation of {@link ItemDto} from various models.
     *
     * @param id The ID of the item.
     * @param amount The amount related to the item.
     * @param description The description of the item.
     * @return {@link ItemDto} The built DTO containing the id, amount, and description.
     */
    private ItemDto builderItemDto(Integer id, BigDecimal amount, String description) {
        return ItemDto.builder()
                .id(id)
                .amount(amount)
                .description(description)
                .build();
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
        // Validate that the period is not null
        if(periodDto == null)
            throw new CustomException("The period be can't null", HttpStatus.BAD_REQUEST);
        // Validate that the start date is not null
        if(periodDto.getStartDate() == null)
            throw new CustomException("The start date be can't null", HttpStatus.BAD_REQUEST);
        if(periodDto.getEndDate() == null)
            throw new CustomException("The end date be can't null", HttpStatus.BAD_REQUEST);
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

    /**
     * Retrieves a Map of installment IDs and their associated expense types.
     *
     * @param id The ID of the BillRecord.
     * @return Map with the installment ID as the key and the expense type as the value.
     */
    private Map<Integer,String> getInstallmentAndExpenseType(Integer id) {
        List<Object[]> repo = billExpenseInstallmentsRepository.findInstallmentIdAndExpenseTypeByBillRecordId(id);
        return repo.stream().collect(Collectors.toMap(row-> (Integer)row[0],row->(String)row[1]));
    }
}

