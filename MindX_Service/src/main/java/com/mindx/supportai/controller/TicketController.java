package com.mindx.supportai.controller;

import com.mindx.supportai.entity.Ticket;
import com.mindx.supportai.repository.TicketRepository;
import com.mindx.supportai.dto.TicketRequest;
import com.mindx.supportai.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = "*")
public class TicketController {
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;


    public TicketController(TicketService ticketService,
                            TicketRepository ticketRepository) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody TicketRequest request) {

        try {
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Query cannot be empty");
            }

            String response = ticketService.createTicket(request.getQuery(),request.getUserId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error creating ticket: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Ticket getTicket(@PathVariable Long id) {

        return ticketRepository.findById(id).orElseThrow();
    }

    @PostMapping("/{id}/message")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long id,
            @RequestBody TicketRequest request) {

        try {
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message cannot be empty");
            }

            String response = ticketService.addMessage(id, request.getQuery());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body("Ticket not found: " + id);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error processing message: " + e.getMessage());
        }
    }
}