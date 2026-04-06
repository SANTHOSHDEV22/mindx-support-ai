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
    private final AiService aiService;

    public TicketService(TicketRepository ticketRepository,
                         MessageRepository messageRepository,
                         AiService aiService) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.aiService = aiService;
    }

    public String createTicket(String query) {

        if (query == null || query.trim().isEmpty()) {
            throw new RuntimeException("Query cannot be empty");
        }

        String lower = query.toLowerCase();

        Status status = Status.OPEN;

        if (lower.contains("refund") ||
                lower.contains("complaint") ||
                lower.contains("angry")) {
            status = Status.NEEDS_HUMAN;
        }

        Ticket ticket = new Ticket();
        ticket.setQuery(query);
        ticket.setStatus(status);
        ticket.setCreatedAt(LocalDateTime.now());

        ticketRepository.save(ticket);

        Message userMsg = new Message();
        userMsg.setTicketId(ticket.getId());
        userMsg.setSender(Sender.USER);
        userMsg.setMessage(query);
        userMsg.setTimestamp(LocalDateTime.now());

        messageRepository.save(userMsg);

        String aiReply = aiService.getResponse(query);

        Message aiMsg = new Message();
        aiMsg.setTicketId(ticket.getId());
        aiMsg.setSender(Sender.AI);
        aiMsg.setMessage(aiReply);
        aiMsg.setTimestamp(LocalDateTime.now());

        messageRepository.save(aiMsg);

        return aiReply;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

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