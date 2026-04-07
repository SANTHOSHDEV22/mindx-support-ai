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

        Ticket ticket = new Ticket();
        ticket.setQuery(query);
        ticket.setStatus(Status.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());

        if (query.toLowerCase().contains("refund") ||
                query.toLowerCase().contains("complaint") ||
                query.toLowerCase().contains("angry")) {
            ticket.setStatus(Status.NEEDS_HUMAN);
        }

        ticketRepository.save(ticket);

        // save user msg
        saveMessage(ticket.getId(), Sender.USER, query);

        // history
        List<Message> history = messageRepository.findByTicketId(ticket.getId());

        // AI reply
        String aiReply = aiService.getResponse(query, history);

        // save AI msg
        saveMessage(ticket.getId(), Sender.AI, aiReply);

        return aiReply + "||TICKET_ID=" + ticket.getId(); // return id
    }

    public String addMessage(Long ticketId, String query) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // escalation
        if (query.toLowerCase().contains("refund") ||
                query.toLowerCase().contains("complaint") ||
                query.toLowerCase().contains("angry")) {
            ticket.setStatus(Status.NEEDS_HUMAN);
        }

        // save user msg
        saveMessage(ticketId, Sender.USER, query);

        // get full history
        List<Message> history = messageRepository.findByTicketId(ticketId);

        // AI reply
        String aiReply = aiService.getResponse(query, history);

        // save AI msg
        saveMessage(ticketId, Sender.AI, aiReply);

        return aiReply;
    }

    private void saveMessage(Long ticketId, Sender sender, String text) {
        Message msg = new Message();
        msg.setTicketId(ticketId);
        msg.setSender(sender);
        msg.setMessage(text);
        msg.setTimestamp(LocalDateTime.now());

        messageRepository.save(msg);
    }
}