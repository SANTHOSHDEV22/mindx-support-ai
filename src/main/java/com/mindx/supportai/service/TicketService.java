package com.mindx.supportai.service;

import com.mindx.supportai.dto.TicketDetailsDTO;
import com.mindx.supportai.entity.*;
import com.mindx.supportai.repository.MessageRepository;
import com.mindx.supportai.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final MessageRepository messageRepository;

    public TicketService(TicketRepository ticketRepository,
                         MessageRepository messageRepository) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
    }

    // CREATE TICKET
    public String createTicket(String query) {

        if (query == null || query.trim().isEmpty()) {
            throw new RuntimeException("Query cannot be empty");
        }

        // Create ticket
        Ticket ticket = new Ticket();
        ticket.setQuery(query);
        ticket.setStatus(Status.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());

        ticketRepository.save(ticket);

        // Save user message
        Message message = new Message();
        message.setTicketId(ticket.getId());
        message.setSender(Sender.USER);
        message.setMessage(query);
        message.setTimestamp(LocalDateTime.now());

        messageRepository.save(message);

        return "Ticket created successfully";
    }

    // GET ALL TICKETS
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // GET TICKET DETAILS + MESSAGES
    public TicketDetailsDTO getTicketDetails(Long id) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        List<Message> messages = messageRepository.findByTicketId(id);

        TicketDetailsDTO dto = new TicketDetailsDTO();
        dto.setTicket(ticket);
        dto.setMessages(messages);

        return dto;
    }
}