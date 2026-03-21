package com.mlb.mlbportal.mappers.analytics;

import com.mlb.mlbportal.dto.analytics.EndpointAnalyticsDTO;
import com.mlb.mlbportal.models.analytics.Endpoint;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EndpointMapper {
    EndpointAnalyticsDTO toEndpointDTO(Endpoint endpoint);
    List<EndpointAnalyticsDTO> toListEndpointDTO(Collection<Endpoint> list);
}