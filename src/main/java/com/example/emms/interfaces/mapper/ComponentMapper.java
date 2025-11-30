package com.example.emms.interfaces.mapper;

import com.example.emms.domain.model.ComponentType;
import com.example.emms.interfaces.dto.ComponentTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComponentMapper {

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "parameters", source = "specification.parameters")
    ComponentTypeDTO toDTO(ComponentType componentType);
}