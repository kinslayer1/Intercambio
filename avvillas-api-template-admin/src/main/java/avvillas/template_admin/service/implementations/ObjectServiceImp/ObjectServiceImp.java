package avvillas.template_admin.service.implementations.ObjectServiceImp;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.mapper.ObjectMapper;
import avvillas.template_admin.persistence.repository.ObjectRepository;
import avvillas.template_admin.service.ObjectService;
import avvillas.template_admin.service.dto.FieldChange;
import avvillas.template_admin.service.dto.PatchUpdateResponse;
import avvillas.template_admin.service.dto.object.patch.PatchObjectBaseDTO;
import avvillas.template_admin.service.dto.object.request.ObjectBaseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectPageResponseDTO;
import avvillas.template_admin.service.dto.object.response.ObjectResponseDTO;
import avvillas.template_admin.service.dto.product.ProductCodeDescriptionDTO;
import avvillas.template_admin.service.implementations.ObjectServiceImp.handlers.ObjectHandler;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.TypeConversionHandler;
import avvillas.template_admin.service.implementations.ObjectServiceImp.validator.ObjectValidator;
import avvillas.template_admin.service.specification.ObjectSpecification;
import avvillas.template_admin.util.CalculateFieldChanges;
import avvillas.template_admin.web.traits.ProductTrait;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ObjectServiceImp implements ObjectService {

    private final ObjectRepository objectRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, ObjectHandler> handlers;
    private final ObjectValidator validator;
    private final ProductTrait productTrait;

    @Override
    @Transactional
    public void deleteById(String id) {
        ObjectEntity existEntity = existObjectById(id);
        deleteObjectAndDependencies(existEntity);
    }

    @Override
    public ObjectResponseDTO create(ObjectBaseDTO dto) {
        return handlers.get(getObjectType(dto.getType()) + ObjectMessage.HANDLER)
                .create(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectResponseDTO getById(String id) {
        ObjectEntity entity = existObjectById(id);
        return objectMapper.toResponseDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObjectResponseDTO> getByProductId(String id) {
        validator.validateProductExist(id);
        return objectMapper.toResponseListDTO(objectRepository.findAllByProductId(id));
    }

    @Override
    @Transactional
    public PatchUpdateResponse<ObjectResponseDTO> update(String id, PatchObjectBaseDTO dto) {
        ObjectEntity entity = existObjectById(id);

        List<FieldChange> differences = CalculateFieldChanges.calculate(entity, dto);

        ObjectResponseDTO objectResponse;
        if (dto.getType().isPresent() && !dto.getType().get().equals(entity.getType())) {
            objectResponse = handleTypeChange(entity, dto);
        } else {
            objectResponse = handlers.get(getObjectType(entity.getType()) + ObjectMessage.HANDLER)
                    .update(entity, dto);
        }
        return new PatchUpdateResponse<>(differences, objectResponse);
    }

    protected ObjectResponseDTO handleTypeChange(ObjectEntity oldEntity, PatchObjectBaseDTO dto) {
        validator.validateTypeChange(dto);
        ObjectBaseDTO newDto = TypeConversionHandler.convertType(oldEntity, dto);
        deleteObjectAndDependencies(oldEntity);
        return create(newDto);
    }

    private void deleteObjectAndDependencies(ObjectEntity entity) {
        handlers.get(getObjectType(entity.getType()) + ObjectMessage.HANDLER)
                .deleteWithDependencies(entity);
    }

    private ObjectEntity existObjectById(String id) {
        return objectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ObjectMessage.OBJECT_NOT_FOUND));
    }

    private String getObjectType(String type) {
        return switch (type.toUpperCase()) {
            case ObjectMessage.TABLE_TYPE -> ObjectMessage.TABLE_TYPE.toLowerCase();
            case ObjectMessage.IMAGE_TYPE -> ObjectMessage.IMAGE_TYPE.toLowerCase();
            default -> ObjectMessage.GENERIC_TYPE.toLowerCase();
        };
    }

    public Page<ObjectPageResponseDTO> searchGlobal(String search, Pageable pageable) {
        final List<String> productIdsFromSearch = getProductIdsFromSearch(search);
        final Specification<ObjectEntity> specification = buildGlobalSearchSpecification(search, productIdsFromSearch);

        final Page<ObjectEntity> resultPage = objectRepository.findAll(specification, pageable);
        final Map<String, ProductCodeDescriptionDTO> productMap = buildProductMap(search, resultPage);

        return mapToResponsePage(resultPage, productMap);
    }

    private List<String> getProductIdsFromSearch(String search) {
        if (search == null || search.isBlank()) return Collections.emptyList();

        return productTrait.searchProducts(search).stream()
                .map(ProductCodeDescriptionDTO::getId)
                .collect(Collectors.toList());
    }

    private Map<String, ProductCodeDescriptionDTO> buildProductMap(String search, Page<ObjectEntity> resultPage) {
        final List<String> allProductIdsInResults = extractProductIdsFromResults(resultPage);
        final List<ProductCodeDescriptionDTO> allProducts = productTrait.getProductsByIds(allProductIdsInResults);

        return createMergedProductMap(search, allProducts);
    }

    private List<String> extractProductIdsFromResults(Page<ObjectEntity> resultPage) {
        return resultPage.getContent().stream()
                .map(ObjectEntity::getProductId)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<String, ProductCodeDescriptionDTO> createMergedProductMap(String search,
                                                                          List<ProductCodeDescriptionDTO> products) {
        final Map<String, ProductCodeDescriptionDTO> mergedMap = new HashMap<>();

        if (search != null && !search.isBlank()) {
            productTrait.searchProducts(search).forEach(product -> mergedMap.put(product.getId(), product));
        }

        products.forEach(product -> mergedMap.put(product.getId(), product));

        return Collections.unmodifiableMap(mergedMap);
    }

    private Page<ObjectPageResponseDTO> mapToResponsePage(Page<ObjectEntity> resultPage,
                                                          Map<String, ProductCodeDescriptionDTO> productMap) {
        return resultPage.map(entity -> createResponseDTO(entity, productMap));
    }

    private ObjectPageResponseDTO createResponseDTO(ObjectEntity entity,
                                                    Map<String, ProductCodeDescriptionDTO> productMap) {
        final ProductCodeDescriptionDTO product = productMap.get(
                entity.getProductId()
        );

        return new ObjectPageResponseDTO(
                product,
                entity.getId(),
                entity.getName(),
                entity.getIdentifier(),
                entity.getEsCierreTabla()
        );
    }

    private Specification<ObjectEntity> buildGlobalSearchSpecification(String search, List<String> productIds) {
        if (search == null || search.trim().isEmpty()) {
            return Specification.where(null);
        }

        return Specification.where(
                ObjectSpecification.filterByProductIds(productIds)
                        .or(ObjectSpecification.filterByIdentifier(search))
                        .or(ObjectSpecification.filterByName(search))
        );
    }
}