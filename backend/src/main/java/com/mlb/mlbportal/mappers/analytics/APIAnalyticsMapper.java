package com.mlb.mlbportal.mappers.analytics;

import com.mlb.mlbportal.dto.analytics.APIAnalyticsDTO;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.models.analytics.APIPerformance;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", uses = {EndpointMapper.class, StadiumMapper.class})
public interface APIAnalyticsMapper {
    APIAnalyticsDTO toAPIDTO(APIPerformance apiPerformance);
    APIPerformance toDomain(APIAnalyticsDTO dto);
    List<APIAnalyticsDTO> toListAPIDTO(Collection<APIPerformance> list);
}