package com.mlb.mlbportal.services.utilities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class PaginationHandlerService {
    public <E, T> Page<T> paginateAndMap(List<E> elements, int page, int size, Function<E, T> mapper) {
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), elements.size());
        int end = Math.min(start + pageable.getPageSize(), elements.size());

        List<T> result = elements.subList(start, end).stream()
                .map(mapper)
                .toList();

        return new PageImpl<>(result, pageable, elements.size());
    }
}