package avvillas.template_admin.web.controller.object.Common;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.CellEntity;
import avvillas.template_admin.persistence.entity.ObjectEntity;
import avvillas.template_admin.persistence.entity.ObjectImageEntity;
import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import avvillas.template_admin.service.dto.object.TableBodyConfig;
import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import avvillas.template_admin.service.dto.object.patch.PatchObjectGenericDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectImageDTO;
import avvillas.template_admin.service.dto.object.patch.PatchObjectTableDTO;
import avvillas.template_admin.service.dto.object.request.CellDTO;
import avvillas.template_admin.service.dto.object.request.ObjectGenericDTO;
import avvillas.template_admin.service.dto.object.request.ObjectImageDTO;
import avvillas.template_admin.service.dto.object.request.ObjectTableDTO;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ObjectTestData {
    public static final String VALID_PRODUCT_ID = "prod-uuid-1234";
    public static final String NON_EXISTENT_PRODUCT_ID = "prod-uuid-9999";


    public static final String VALID_BASE64_IMAGE_PNG = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";
    public static final String UNSUPPORTED_MIME_TYPE_IMAGE = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
    public static final String MALFORMED_BASE64_IMAGE = "data:image/png;base64,invalid-base64-string";
    public static final String VALID_PRODUCT_ID_2 = "prod-uuid-5678";

    public ObjectGenericDTO createNewGenericObjectDto() {
        ObjectGenericDTO dto = new ObjectGenericDTO();
        dto.setProductId(VALID_PRODUCT_ID);
        dto.setName("Main Title");
        dto.setIdentifier("MAIN_TITLE");
        dto.setType("TEXT");
        dto.setObjectType("GENERIC");
        dto.setEsCierreTabla(Boolean.TRUE);
        dto.setContent("Welcome to our new credit card application!");

        Map<String, Object> styles = new HashMap<>();
        styles.put("color", "#333333");
        styles.put("fontSize", "24px");
        dto.setStyle(styles);

        return dto;
    }

    public ObjectImageDTO createNewImageObjectDto() {
        ObjectImageDTO dto = new ObjectImageDTO();
        dto.setProductId(VALID_PRODUCT_ID);
        dto.setName("Company Logo");
        dto.setIdentifier("LOGO");
        dto.setType("IMAGE");
        dto.setObjectType("IMAGE");
        dto.setEsCierreTabla(Boolean.TRUE);
        dto.setImage(VALID_BASE64_IMAGE_PNG);
        return dto;
    }

    public ObjectTableDTO createNewTableObjectDto() {
        ObjectTableDTO tableDto = new ObjectTableDTO();
        tableDto.setProductId(VALID_PRODUCT_ID);
        tableDto.setName("Account Summary Table");
        tableDto.setIdentifier("ACC_SUM");
        tableDto.setType("TABLE");
        tableDto.setObjectType("TABLE");
        tableDto.setEsCierreTabla(Boolean.TRUE);
        tableDto.setFixed(true);

        TableHeaderConfig headerConfig = new TableHeaderConfig();
        TableHeaderConfig.ColumnConfig headerCol1 = new TableHeaderConfig.ColumnConfig();
        headerCol1.setLabel("Account Type");
        headerCol1.setRowIndex(0);
        headerCol1.setColumnIndex(0);

        TableHeaderConfig.ColumnConfig headerCol2 = new TableHeaderConfig.ColumnConfig();
        headerCol2.setLabel("Balance");
        headerCol2.setRowIndex(0);
        headerCol2.setColumnIndex(1);

        headerConfig.setColumns(List.of(headerCol1, headerCol2));
        tableDto.setHeader(headerConfig);

        TableBodyConfig bodyConfig = new TableBodyConfig();
        CellDTO cell = new CellDTO();
        cell.setContent("Savings Account");
        cell.setRowIndex(0);
        cell.setColumnIndex(0);

        bodyConfig.setCells(Collections.singletonList(cell));
        tableDto.setBody(bodyConfig);

        return tableDto;
    }

    public ObjectEntity createNewGenericObjectEntity() {
        ObjectEntity entity = new ObjectEntity();
        entity.setProductId(VALID_PRODUCT_ID);
        entity.setName("Main Title");
        entity.setIdentifier("MAIN_TITLE");
        entity.setType("TEXT");
        entity.setContent("Welcome to our new credit card application!");

        Map<String, Object> styles = new HashMap<>();
        styles.put("color", "#333333");
        styles.put("fontSize", "24px");
        entity.setStyleProperties(styles);

        return entity;
    }

    public ObjectImageEntity createNewImageObjectEntity() {
        // Extract image data from base64 string
        String[] imageParts = VALID_BASE64_IMAGE_PNG.split(",");
        String mimeType = imageParts[0].split(":")[1].split(";")[0];
        byte[] imageBytes = Base64.getDecoder().decode(imageParts[1]);

        ObjectImageEntity entity = new ObjectImageEntity();
        entity.setProductId(VALID_PRODUCT_ID);
        entity.setName("Company Logo");
        entity.setIdentifier("LOGO");
        entity.setType("IMAGE");
        entity.setImage(imageBytes);
        entity.setMimeType(mimeType);

        return entity;
    }

    public ObjectTableEntity createNewTableObjectEntity() {
        ObjectTableEntity entity = new ObjectTableEntity();
        entity.setProductId(VALID_PRODUCT_ID);
        entity.setName("Account Summary Table");
        entity.setIdentifier("ACC_SUM");
        entity.setType("TABLE");
        entity.setFixed(1);

        TableHeaderConfig headerConfig = new TableHeaderConfig();
        TableHeaderConfig.ColumnConfig headerCol1 = new TableHeaderConfig.ColumnConfig();
        headerCol1.setLabel("Account Type");
        headerCol1.setRowIndex(0);
        headerCol1.setColumnIndex(0);

        TableHeaderConfig.ColumnConfig headerCol2 = new TableHeaderConfig.ColumnConfig();
        headerCol2.setLabel("Balance");
        headerCol2.setRowIndex(0);
        headerCol2.setColumnIndex(1);

        headerConfig.setColumns(List.of(headerCol1, headerCol2));
        entity.setHeader(headerConfig);

        TableBodyConfig bodyConfig = new TableBodyConfig();
        CellDTO cellDto = new CellDTO();
        cellDto.setContent("Savings Account");
        cellDto.setRowIndex(0);
        cellDto.setColumnIndex(0);
        bodyConfig.setCells(Collections.singletonList(cellDto));
        entity.setBody(bodyConfig);

        CellEntity cellEntity = new CellEntity();
        cellEntity.setTableObject(entity);
        cellEntity.setRowIndex(0);
        cellEntity.setColumnIndex(0);
        cellEntity.setContent("Savings Account");
        entity.setCells(List.of(cellEntity));

        return entity;
    }

    public PatchObjectGenericDTO createPatchWithNewContent(String newContent) {
        PatchObjectGenericDTO patchDto = new PatchObjectGenericDTO();
        patchDto.setObjectType(ObjectMessage.GENERIC_TYPE);
        patchDto.setContent(Optional.of(newContent));
        return patchDto;
    }

    public PatchObjectImageDTO createPatchWithNewImage(String newImage) {
        PatchObjectImageDTO patchDto = new PatchObjectImageDTO();
        patchDto.setObjectType(ObjectMessage.IMAGE_TYPE);
        patchDto.setImage(Optional.of(newImage));
        return patchDto;
    }

    public PatchObjectTableDTO createPatchWithNewCell(String cellContent) {
        PatchObjectTableDTO patchDto = new PatchObjectTableDTO();
        patchDto.setObjectType(ObjectMessage.TABLE_TYPE);

        TableHeaderConfig headerConfig = new TableHeaderConfig();
        TableHeaderConfig.ColumnConfig col1 = new TableHeaderConfig.ColumnConfig();
        col1.setLabel("Col 1");
        col1.setRowIndex(0);
        col1.setColumnIndex(0);
        headerConfig.setColumns(List.of(col1));
        patchDto.setHeader(Optional.of(headerConfig));

        TableBodyConfig bodyConfig = new TableBodyConfig();
        CellDTO cell = new CellDTO();
        cell.setContent(cellContent);
        cell.setRowIndex(0);
        cell.setColumnIndex(0);
        bodyConfig.setCells(List.of(cell));
        patchDto.setBody(Optional.of(bodyConfig));

        return patchDto;
    }

    public ObjectEntity createObjectEntity(String productId, String name, String identifier) {
        ObjectEntity entity = new ObjectEntity();
        entity.setProductId(productId);
        entity.setName(name);
        entity.setIdentifier(identifier);
        entity.setType("TEXT");
        entity.setContent("Sample content");
        return entity;
    }

    public List<ObjectEntity> createObjectsForPagination() {
        return List.of(
                createObjectEntity(VALID_PRODUCT_ID, "Object 1", "OBJ_1"),
                createObjectEntity(VALID_PRODUCT_ID, "Object 2", "OBJ_2"),
                createObjectEntity(VALID_PRODUCT_ID_2, "Object 3", "OBJ_3"),
                createObjectEntity(VALID_PRODUCT_ID_2, "Object 4", "OBJ_4"),
                createObjectEntity(VALID_PRODUCT_ID, "Object 5", "OBJ_5")
        );
    }
}