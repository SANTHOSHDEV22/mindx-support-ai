package com.mindx.supportai.controller;

import com.mindx.supportai.dto.TicketDetailsDTO;
import com.mindx.supportai.dto.TicketRequest;
import com.mindx.supportai.entity.Ticket;
import com.mindx.supportai.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // CREATE TICKET
    @PostMapping
    public String createTicket(@RequestBody TicketRequest request) {
        return ticketService.createTicket(request.getQuery());
    }

    // GET ALL TICKETS
    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    // GET TICKET DETAILS
    @GetMapping("/{id}")
    public TicketDetailsDTO getTicket(@PathVariable Long id) {
        return ticketService.getTicketDetails(id);
    }
}