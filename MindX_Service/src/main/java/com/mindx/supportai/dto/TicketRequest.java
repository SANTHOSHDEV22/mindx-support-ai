package com.mindx.supportai.dto;

import lombok.Data;

@Data
public class TicketRequest {
    private String query;
    private Long userId;
}