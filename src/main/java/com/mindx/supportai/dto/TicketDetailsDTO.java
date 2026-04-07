package com.mindx.supportai.dto;

import com.mindx.supportai.entity.Message;
import com.mindx.supportai.entity.Ticket;
import lombok.Data;

import java.util.List;

@Data
public class TicketDetailsDTO {
    private Ticket ticket;
    private List<Message> messages;
}