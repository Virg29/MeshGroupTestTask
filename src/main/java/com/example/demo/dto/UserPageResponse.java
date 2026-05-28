package com.example.demo.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class UserPageResponse {

    private List<UserResponse> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public static UserPageResponse of(Page<UserResponse> pageData) {
        UserPageResponse response = new UserPageResponse();
        response.setContent(pageData.getContent());
        response.setTotalElements(pageData.getTotalElements());
        response.setTotalPages(pageData.getTotalPages());
        response.setPage(pageData.getNumber());
        response.setSize(pageData.getSize());
        return response;
    }
}
