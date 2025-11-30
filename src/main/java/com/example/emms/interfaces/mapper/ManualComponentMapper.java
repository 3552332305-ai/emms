package com.example.emms.interfaces.mapper;

import com.example.emms.domain.model.ComponentType;
import com.example.emms.interfaces.dto.ComponentTypeDTO;
import org.springframework.stereotype.Component;

@Component
public class ManualComponentMapper {

    public ComponentTypeDTO toDTO(ComponentType componentType) {
        if (componentType == null) {
            return null;
        }

        ComponentTypeDTO dto = new ComponentTypeDTO();
        dto.setId(componentType.getId());
        dto.setName(componentType.getName());
        dto.setModel(componentType.getModel());
        dto.setUnitPrice(componentType.getUnitPrice());
        dto.setImage(componentType.getImage());
        dto.setStockQuantity(componentType.getStockQuantity());
        dto.setStatus(componentType.getStatus());

        if (componentType.getSpecification() != null) {
            dto.setParameters(componentType.getSpecification().getParameters());
        }

        if (componentType.getSupplier() != null) {
            dto.setSupplierId(componentType.getSupplier().getId());
            dto.setSupplierName(componentType.getSupplier().getName());
        }

        return dto;
    }
}
