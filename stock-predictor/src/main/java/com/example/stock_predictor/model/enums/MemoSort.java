package com.example.stock_predictor.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum MemoSort {
    LATEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    OLDEST(Sort.by(Sort.Direction.ASC, "createdAt")),
    TITLE(Sort.by(Sort.Direction.ASC, "title"));

    private final Sort sort;

    public static MemoSort fromString(String value){
        try {
            return MemoSort.valueOf(value.toUpperCase());
        }catch (Exception e){
            return LATEST;
        }
    }
}
