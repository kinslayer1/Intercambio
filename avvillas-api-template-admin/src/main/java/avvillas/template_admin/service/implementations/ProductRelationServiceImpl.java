package avvillas.template_admin.service.implementations;

import avvillas.template_admin.persistence.repository.FormatRepository;
import avvillas.template_admin.persistence.repository.ObjectRepository;
import avvillas.template_admin.service.ProductRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductRelationServiceImpl implements ProductRelationService {

    private final ObjectRepository objectRepository;
    private final FormatRepository formatRepository;

    public boolean isProductRelated(String productId) {
        if (objectRepository.existsByProductId(productId)) return true;
        return formatRepository.existsByProductId(productId);
    }
}

