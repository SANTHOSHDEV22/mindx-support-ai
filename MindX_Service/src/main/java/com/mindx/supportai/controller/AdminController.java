package com.mindx.supportai.controller;

import com.mindx.supportai.entity.Ticket;
import com.mindx.supportai.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;
import com.mindx.supportai.service.TicketService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {

    private final TicketRepository ticketRepository;
    private final TicketService ticketService;

    public AdminController(TicketRepository ticketRepository,
                           TicketService ticketService) {
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
    }

    // 🔹 GET ALL TICKETS
    @GetMapping("/tickets")
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // 🔹 UPDATE STATUS
    @PutMapping("/tickets/{id}/status")
    public Ticket updateStatus(@PathVariable Long id,
                               @RequestParam String status) {

        Ticket ticket = ticketRepository.findById(id).orElseThrow();

        ticket.setStatus(Enum.valueOf(
                com.mindx.supportai.entity.Status.class,
                status
        ));

        return ticketRepository.save(ticket);
    }

    // 🔥 ADD THIS (ADMIN REPLY)
    @PostMapping("/tickets/{ticketId}/reply")
    public String adminReply(@PathVariable Long ticketId,
                             @RequestBody String message) {

        return ticketService.adminReply(ticketId, message);
    }
}