package avvillas.template_admin.service.implementations;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.fields.ObjectType;
import avvillas.template_admin.constant.message.FormatMessage;
import avvillas.template_admin.persistence.entity.FormatEntity;
import avvillas.template_admin.persistence.entity.FormatObjectsEntity;
import avvillas.template_admin.persistence.entity.FormatPageEntity;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.mapper.FormatMapper;
import avvillas.template_admin.persistence.mapper.FormatResponseMapper;
import avvillas.template_admin.persistence.mapper.ObjectMapper;
import avvillas.template_admin.persistence.repository.FormatRepository;
import avvillas.template_admin.persistence.repository.ObjectRepository;
import avvillas.template_admin.service.FormatService;
import avvillas.template_admin.service.PdfService;
import avvillas.template_admin.service.dto.format.FormatDto;
import avvillas.template_admin.service.dto.format.FormatObjectsDto;
import avvillas.template_admin.service.dto.format.FormatPageDto;
import avvillas.template_admin.service.dto.format.FormatPageResponseDto;
import avvillas.template_admin.service.dto.format.FormatResponseDto;
import avvillas.template_admin.service.dto.format.LastActiveFormatDTO;
import avvillas.template_admin.service.dto.input_product_structure.InputProductStructureDto;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import avvillas.template_admin.service.dto.product.ProductCodeDescriptionDTO;
import avvillas.template_admin.service.implementations.ObjectServiceImp.validator.ObjectValidator;
import avvillas.template_admin.service.specification.FormatSpecification;
import avvillas.template_admin.web.exception.FormatNotFoundException;
import avvillas.template_admin.web.traits.EntryTrait;
import avvillas.template_admin.web.traits.ProductTrait;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormatServiceImpl implements FormatService {

    private static final int INACTIVE = 0;
    private static final int ACTIVE = 1;
    private final FormatRepository formatRepository;
    private final ObjectRepository objectRepository;
    private final ObjectValidator objectValidator;
    private final EntryTrait entryTrait;
    private final FormatMapper formatMapper;
    private final FormatResponseMapper formatResponseMapper;
    private final ObjectMapper mapper;
    private final ProductTrait productTrait;
    private final PdfService pdfService;

    @Override
    @Transactional
    public FormatResponseDto create(FormatDto formatDto, String formatId) {
        objectValidator.validateProductExist(formatDto.getProductId());

        if (formatId != null) {
            FormatEntity existingFormat = getFormatById(formatId);

            if (isPdfConfigUpdated(existingFormat, formatDto) && !areObjectsDifferent(existingFormat, formatDto)) {
                updatePdfConfig(existingFormat, formatDto);
                FormatDto.PdfConfigDto pdfConfig = mapPdfConfig(existingFormat);
                List<FormatPageDto> mapData = existingFormat.getPages().stream()
                        .map(page -> new FormatPageDto(
                                page.getId(),
                                page.getPageNumber(),
                                page.getObjects().stream()
                                        .map(this::mapFormatObjectToDtoWithRepository)
                                        .toList()
                        ))
                        .toList();
                String htmlContent = pdfService.generateHtmlFromDto(pdfConfig, mapData);
                existingFormat.setHtmlContent(htmlContent);

                FormatEntity updatedEntity = formatRepository.save(existingFormat);
                return formatResponseMapper.toResponseDto(updatedEntity);
            }
        }
        FormatEntity formatEntity = formatMapper.toEntity(formatDto);

        deactivatePreviousFormat(formatEntity);

        List<FormatPageEntity> pages = new ArrayList<>();
        for (var pageDto : formatDto.getPages()) {
            FormatPageEntity pageEntity = new FormatPageEntity();

            pageEntity.setFormat(formatEntity);
            pageEntity.setPageNumber(pageDto.getPageNumber());

            List<FormatObjectsEntity> objects = mapAndCombineObjects(pageDto);
            objects.forEach(object -> object.setPage(pageEntity));

            pageEntity.setObjects(objects);

            pages.add(pageEntity);
        }

        formatEntity.setPages(pages);


        List<FormatPageDto> mapData = formatEntity.getPages().stream()
                .map(page -> new FormatPageDto(
                        page.getId(),
                        page.getPageNumber(),
                        page.getObjects().stream()
                                .map(this::mapFormatObjectToDtoWithRepository)
                                .toList()
                ))
                .toList();

        FormatDto.PdfConfigDto pdfConfig = mapPdfConfig(formatEntity);

        String htmlContent = pdfService.generateHtmlFromDto(pdfConfig, mapData);

        formatEntity.setHtmlContent(htmlContent);

        FormatEntity savedEntity = formatRepository.save(formatEntity);
        return formatResponseMapper.toResponseDto(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormatResponseDto getById(String id) {
        FormatEntity entity = getFormatById(id);
        return formatResponseMapper.toResponseDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormatResponseDto getByProductId(String id) {
        FormatEntity entity = getFormatByProductId(id);

        if (entity == null)
            throw new FormatNotFoundException(FormatMessage.ERROR_GET_FORMAT);

        return formatResponseMapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public FormatResponseDto toggleFormatActivation(String id) {
        FormatEntity formatToToggle = getFormatById(id);
        List<FormatEntity> otherActiveFormats = formatRepository.findByProductId(formatToToggle.getProductId());

        otherActiveFormats.removeIf(otherFormat -> Objects.equals(formatToToggle.getId(), otherFormat.getId()));

        if (otherActiveFormats.isEmpty())
            throw new RuntimeException(FormatMessage.ERROR_UPDATE_FORMAT);

        deactivateActiveFormats(otherActiveFormats);

        formatToToggle.setActive(ACTIVE);
        formatRepository.save(formatToToggle);

        return formatResponseMapper.toResponseDto(formatToToggle);
    }

    private void deactivateActiveFormats(List<FormatEntity> activeFormats) {
        activeFormats.forEach(format -> format.setActive(INACTIVE));
        formatRepository.saveAll(activeFormats);
    }

    public Page<FormatPageResponseDto> searchGlobal(String search, Pageable pageable) {
        final List<String> productIdsFromSearch = getProductIdsFromSearch(search);
        final Specification<FormatEntity> specification =
                buildGlobalSearchSpecification(search, productIdsFromSearch);

        final List<FormatEntity> allResults = formatRepository.findAll(specification);

        final Map<String, ProductCodeDescriptionDTO> productMap =
                buildProductMap(search, new PageImpl<>(allResults));

        List<FormatEntity> sortedResults = allResults.stream()
                .sorted(
                        Comparator
                                .comparing((FormatEntity e) -> {
                                    ProductCodeDescriptionDTO p = productMap.get(e.getProductId());
                                    return p != null ? p.getCode() : "";
                                })
                                .thenComparing(e -> e.getActive() == null || e.getActive() == 0)
                                .thenComparing(FormatEntity::getVersion, Comparator.reverseOrder())
                )
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedResults.size());

        List<FormatEntity> pageContent = sortedResults.subList(start, end);

        Page<FormatEntity> finalPage = new PageImpl<>(
                pageContent,
                pageable,
                sortedResults.size()
        );

        List<String> allFieldIds = sortedResults.stream()
                .flatMap(entity -> entity.getPages().stream())
                .flatMap(page -> page.getObjects().stream())
                .map(FormatObjectsEntity::getFieldId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, InputProductStructureDto> fieldIdToStructureMap =
                productTrait.getInputProductStructureByIds(allFieldIds).stream()
                        .collect(Collectors.toMap(InputProductStructureDto::getId, dto -> dto));

        return mapToResponsePage(finalPage, productMap, fieldIdToStructureMap);
    }

    private List<String> getProductIdsFromSearch(String search) {
        if (isSearchValueEmpty(search)) return Collections.emptyList();

        return productTrait.searchProducts(search).stream()
                .map(ProductCodeDescriptionDTO::getId)
                .collect(Collectors.toList());
    }

    private Page<FormatPageResponseDto> mapToResponsePage(Page<FormatEntity> resultPage,
                                                          Map<String, ProductCodeDescriptionDTO> productMap,
                                                          Map<String, InputProductStructureDto> fieldIdToStructureMap) {
        return resultPage.map(entity -> createResponseDTO(entity, productMap, fieldIdToStructureMap));
    }

    private FormatPageResponseDto createResponseDTO(FormatEntity entity,
                                                    Map<String, ProductCodeDescriptionDTO> productMap,
                                                    Map<String, InputProductStructureDto> fieldIdToStructureMap) {
        final ProductCodeDescriptionDTO product = productMap.get(
                entity.getProductId()
        );

        FormatDto.PdfConfigDto pdfConfig = mapPdfConfig(entity);

        List<FormatPageDto> pages = entity.getPages().stream()
                .sorted(Comparator.comparingInt(FormatPageEntity::getPageNumber))
                .map(page -> new FormatPageDto(
                        page.getId(),
                        page.getPageNumber(),
                        page.getObjects().stream()
                                .map(obj -> mapFormatObjectToDto(obj, fieldIdToStructureMap))
                                .toList()
                ))
                .toList();

        return new FormatPageResponseDto(product, entity.getId(), entity.getProductId(), entity.getActive() == 1, entity.getVersion(), pdfConfig, pages);
    }

    private ObjectResponseDTO mapFormatObjectToDto(FormatObjectsEntity entity, Map<String, InputProductStructureDto> fieldIdToStructureMap) {
        if (entity.getObject() != null) {
            ObjectResponseDTO response = mapper.toResponseDTO(entity.getObject());
            response.setPositionY(entity.getPositionY());
            response.setPositionX(entity.getPositionX());
            return response;
        }

        if (entity.getFieldId() != null) {
            InputProductStructureDto structure = fieldIdToStructureMap.get(entity.getFieldId());

            return new FormatObjectsDto(
                    entity.getId(),
                    entity.getFieldId(),
                    entity.getPositionX(),
                    entity.getPositionY(),
                    entity.getType() != null ? entity.getType().name() : null,
                    structure != null ? structure.getFieldName() : null,
                    structure != null ? structure.getTypeRegister() : null
            );
        }
        return null;
    }

    private ObjectResponseDTO mapFormatObjectToDtoWithRepository(FormatObjectsEntity entity) {
        if (entity.getObject() != null) {

            ObjectEntity fullObject = objectRepository.findById(entity.getObject().getId())
                    .orElse(entity.getObject());

            ObjectResponseDTO response = mapper.toResponseDTO(fullObject);
            response.setPositionY(entity.getPositionY());
            response.setPositionX(entity.getPositionX());

            return response;
        }

        if (entity.getFieldId() != null) {

            return new FormatObjectsDto(
                    entity.getId(),
                    entity.getFieldId(),
                    entity.getPositionX(),
                    entity.getPositionY(),
                    entity.getType() != null ? entity.getType().name() : null,
                    null,
                    null
            );
        }
        return null;
    }

    private FormatDto.PdfConfigDto mapPdfConfig(FormatEntity entity) {
        return new FormatDto.PdfConfigDto(
                entity.getContinues(),
                entity.getPaperType(),
                entity.getFontFamily(),
                new FormatDto.MarginDto(
                        entity.getTopMargin(),
                        entity.getBottomMargin(),
                        entity.getLeftMargin(),
                        entity.getRightMargin()
                )
        );
    }

    private Specification<FormatEntity> buildGlobalSearchSpecification(String search, List<String> productIds) {
        if (isSearchValueEmpty(search)) return Specification.where(null);

        return Specification.where(
                        FormatSpecification.filterByState(search)
                ).or(FormatSpecification.filterByVersion(search))
                .or(FormatSpecification.filterById(search))
                .or(FormatSpecification.filterByProductIds(productIds));
    }

    private Map<String, ProductCodeDescriptionDTO> buildProductMap(String search, Page<FormatEntity> resultPage) {
        final List<String> allProductIdsInResults = extractProductIdsFromResults(resultPage);
        final List<ProductCodeDescriptionDTO> allProducts = productTrait.getProductsByIds(allProductIdsInResults);

        return createMergedProductMap(search, allProducts);
    }

    private boolean isSearchValueEmpty(String search) {
        return search == null || search.isBlank();
    }

    private Map<String, ProductCodeDescriptionDTO> createMergedProductMap(String search,
                                                                          List<ProductCodeDescriptionDTO> products) {
        final Map<String, ProductCodeDescriptionDTO> mergedMap = new HashMap<>();

        if (!isSearchValueEmpty(search))
            productTrait.searchProducts(search).forEach(product -> mergedMap.put(product.getId(), product));

        products.forEach(product -> mergedMap.put(product.getId(), product));

        return Collections.unmodifiableMap(mergedMap);
    }

    private List<String> extractProductIdsFromResults(Page<FormatEntity> resultPage) {
        return resultPage.getContent().stream()
                .map(FormatEntity::getProductId)
                .distinct()
                .collect(Collectors.toList());
    }

    private void deactivatePreviousFormat(FormatEntity formatEntity) {
        List<FormatEntity> otherActiveFormats = formatRepository.findByProductId(formatEntity.getProductId());

        otherActiveFormats.removeIf(otherFormat -> Objects.equals(formatEntity.getId(), otherFormat.getId()));

        deactivateActiveFormats(otherActiveFormats);

        FormatEntity latestFormat = formatRepository.findFirstByProductIdOrderByVersionDesc(formatEntity.getProductId());
        if (latestFormat != null) {
            latestFormat.setActive(INACTIVE);
            formatRepository.save(latestFormat);
            formatEntity.setVersion(latestFormat.getVersion() + 1);
        }
    }

    private boolean isPdfConfigUpdated(FormatEntity entity, FormatDto formatDto) {
        return areMarginsDifferent(entity, formatDto);
    }

    private boolean areMarginsDifferent(FormatEntity entity, FormatDto formatDto) {
        return !Objects.equals(entity.getTopMargin(), formatDto.getPdfConfig().getMargins().getTop()) ||
                !Objects.equals(entity.getBottomMargin(), formatDto.getPdfConfig().getMargins().getBottom()) ||
                !Objects.equals(entity.getLeftMargin(), formatDto.getPdfConfig().getMargins().getLeft()) ||
                !Objects.equals(entity.getRightMargin(), formatDto.getPdfConfig().getMargins().getRight()) ||
                !Objects.equals(entity.getPaperType(), formatDto.getPdfConfig().getPaperType()) ||
                !Objects.equals(entity.getFontFamily(), formatDto.getPdfConfig().getFontFamily());
    }

    private boolean areObjectsDifferent(FormatEntity entity, FormatDto formatDto) {
        List<FormatObjectsEntity> currentObjects = entity.getPages().stream()
                .flatMap(page -> page.getObjects().stream())
                .toList();

        List<FormatObjectsEntity> newObjects = formatDto.getPages().stream()
                .flatMap(pageDto -> mapAndCombineObjects(pageDto).stream())
                .toList();


        if (currentObjects.size() != newObjects.size()) return true;

        for (int i = 0; i < currentObjects.size(); i++) {
            FormatObjectsEntity current = currentObjects.get(i);
            FormatObjectsEntity incoming = newObjects.get(i);

            if (!Objects.equals(current.getPositionX(), incoming.getPositionX()) ||
                    !Objects.equals(current.getPositionY(), incoming.getPositionY())) {
                return true;
            }
        }
        return false;
    }

    private void updatePdfConfig(FormatEntity entity, FormatDto dto) {
        entity.setTopMargin(dto.getPdfConfig().getMargins().getTop());
        entity.setBottomMargin(dto.getPdfConfig().getMargins().getBottom());
        entity.setLeftMargin(dto.getPdfConfig().getMargins().getLeft());
        entity.setRightMargin(dto.getPdfConfig().getMargins().getRight());
        entity.setPaperType(dto.getPdfConfig().getPaperType());
        entity.setFontFamily(dto.getPdfConfig().getFontFamily());
        entity.setContinues(dto.getPdfConfig().getContinues());
    }

    private List<FormatObjectsEntity> mapAndCombineObjects(FormatDto.PageDto pageDto) {
        List<FormatObjectsEntity> elements = formatMapper.toEntitiesFromElements(pageDto.getElements());
        List<FormatObjectsEntity> fields = formatMapper.toEntitiesFromFields(pageDto.getFields());

        List<FormatObjectsEntity> allObjects = new ArrayList<>();
        allObjects.addAll(elements);
        allObjects.addAll(fields);

        allObjects.forEach(this::validateAndSetObjectType);

        return allObjects;
    }

    private void validateAndSetObjectType(FormatObjectsEntity object) {
        if (object.getObject() != null && object.getObject().getId() != null) {
            String objectId = object.getObject().getId();

            if (!objectRepository.existsById(objectId)) {
                throw new NoSuchElementException(MessageConstant.format(FormatMessage.OBJECT_NOT_FOUND, objectId));
            }

            object.setType(ObjectType.OBJECT);
        } else if (object.getFieldId() != null) {
            entryTrait.getEntryById(object.getFieldId());
            object.setType(ObjectType.FIELD);
        }
    }

    private FormatEntity getFormatById(String id) {
        return formatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(FormatMessage.FORMAT_NOT_FOUND, id)));
    }

    public FormatResponseDto getActiveFormatByProductId(String productId) {
        FormatEntity entity = formatRepository.findByProductIdAndActive(productId, 1);
        return formatResponseMapper.toResponseDto(entity);
    }

    private FormatEntity getFormatByProductId(String id) {
        return formatRepository.findFirstByProductIdOrderByVersionDesc(id);
    }

    @Override
    @Transactional
    public void updateFormatContent(String formatId, String htmlContent, String base64Content) {
        FormatEntity format = getFormatById(formatId);
        format.setHtmlContent(htmlContent);
        format.setBase64Content(base64Content);
        formatRepository.save(format);
    }

    @Override
    public LastActiveFormatDTO getLastActiveFormatByProduct(String productId) {
        LastActiveFormatDTO version = new LastActiveFormatDTO();
        FormatEntity format = formatRepository.findByProductIdAndActive(productId, 1);

        if (format == null)
            throw new NoSuchElementException(MessageConstant.format(FormatMessage.FORMAT_NOT_FOUND, productId));

        version.setVersion(format.getVersion());
        return version;
    }
}

