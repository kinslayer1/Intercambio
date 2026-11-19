package avvillas.template_admin.service.implementations.ObjectServiceImp.helpers;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.persistence.entity.CellEntity;
import avvillas.template_admin.persistence.entity.ObjectTableEntity;
import avvillas.template_admin.persistence.mapper.ObjectMapper;
import avvillas.template_admin.persistence.repository.CellRepository;
import avvillas.template_admin.service.dto.object.request.CellDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CellUpdater {

    public static void updateCells(ObjectTableEntity table, List<CellDTO> cellDTOs,
                                   CellRepository repository, ObjectMapper mapper) {

        Map<String, CellEntity> existingCellsMap = table.getCells().stream()
                .collect(Collectors.toMap(CellEntity::getId, Function.identity()));

        Map<String, CellDTO> dtosById = cellDTOs.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toMap(CellDTO::getId, Function.identity(), (dto1, dto2) -> dto2));

        List<CellDTO> dtosWithoutId = cellDTOs.stream()
                .filter(dto -> dto.getId() == null)
                .toList();

        List<CellEntity> cellsToSaveOrUpdate = new ArrayList<>();

        for (Map.Entry<String, CellDTO> entry : dtosById.entrySet()) {
            String dtoId = entry.getKey();
            CellDTO dto = entry.getValue();
            CellEntity existingCell = existingCellsMap.get(dtoId);

            if (existingCell != null) {
                updateExistingCellProperties(existingCell, dto);
                cellsToSaveOrUpdate.add(existingCell);
            } else {
                throw new IllegalArgumentException(MessageConstant.format(ObjectMessage.CELL_NOT_EXIST, dtoId));
            }
        }


        for (CellDTO dtoToCreate : dtosWithoutId) {
            cellsToSaveOrUpdate.add(createNewCellEntity(dtoToCreate, mapper, table));
        }

        List<CellEntity> savedCells = new ArrayList<>();
        if (!cellsToSaveOrUpdate.isEmpty()) {
            savedCells = repository.saveAll(cellsToSaveOrUpdate);
        }

        List<CellEntity> finalCellList = new ArrayList<>(savedCells);
        Set<String> updatedCellIds = cellsToSaveOrUpdate.stream()
                .map(CellEntity::getId)
                .filter(existingCellsMap::containsKey)
                .collect(Collectors.toSet());

        for (CellEntity originalExistingCell : existingCellsMap.values()) {
            boolean wasUpdated = updatedCellIds.contains(originalExistingCell.getId());

            if (!wasUpdated) {
                finalCellList.add(originalExistingCell);
            }
        }
        table.getCells().clear();
        table.getCells().addAll(finalCellList);
    }

    private static void updateExistingCellProperties(CellEntity cell, CellDTO dto) {
        cell.setContent(dto.getContent());
        cell.setRowIndex(dto.getRowIndex());
        cell.setColumnIndex(dto.getColumnIndex());
        cell.setColSpan(dto.getColSpan());
        cell.setRowSpan(dto.getRowSpan());
        cell.setStyleProperties(dto.getStyle());
    }

    private static CellEntity createNewCellEntity(CellDTO dto, ObjectMapper mapper, ObjectTableEntity table) {
        CellEntity cell = mapper.toCellEntity(dto);
        cell.setId(null);
        cell.setTableObject(table);
        cell.setFixed(1);
        return cell;
    }

    private static boolean isContentEmpty(String content) {
        return content == null || content.trim().isEmpty();
    }
}