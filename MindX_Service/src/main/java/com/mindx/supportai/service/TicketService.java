package com.mindx.supportai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindx.supportai.entity.*;
import com.mindx.supportai.repository.MessageRepository;
import com.mindx.supportai.repository.TicketRepository;
import com.mindx.supportai.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final MessageRepository messageRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TicketService(TicketRepository ticketRepository,
                         MessageRepository messageRepository,
                         UserRepository userRepository,
                         AiService aiService) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    // ================= CREATE TICKET =================
    public String createTicket(String query, Long userId) {

        if (query == null || query.trim().isEmpty()) {
            throw new RuntimeException("Query cannot be empty");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = new Ticket();
        ticket.setUser(user); // ✅ IMPORTANT
        ticket.setQuery(query);
        ticket.setStatus(Status.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());

        // escalation logic
        if (isEscalation(query)) {
            ticket.setStatus(Status.NEEDS_HUMAN);
        }

        ticket = ticketRepository.save(ticket);

        // save USER message
        saveMessage(ticket, Sender.USER, query);

        // get history
        List<Message> history = messageRepository.findByTicketId(ticket.getId());

        // AI response
        String aiResponse = aiService.getResponse(query, history);

        // extract clean reply
        String replyText = extractReply(aiResponse);

        // save AI message
        saveMessage(ticket, Sender.AI, replyText);



        return aiResponse + "||TICKET_ID=" + ticket.getId();
    }

    // ================= ADD MESSAGE =================
    public String addMessage(Long ticketId, String query) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // escalation logic
        if (isEscalation(query)) {
            ticket.setStatus(Status.NEEDS_HUMAN);
        }

        // save USER message
        saveMessage(ticket, Sender.USER, query);

        // get history
        List<Message> history = messageRepository.findByTicketId(ticketId);

        // AI response
        String aiResponse = aiService.getResponse(query, history);

        // extract clean reply
        String replyText = extractReply(aiResponse);

        // save AI message
        saveMessage(ticket, Sender.AI, replyText);

        return aiResponse;
    }
    // ================= ADMIN MESSAGE =================
    public String adminReply(Long ticketId, String message) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // save ADMIN message
        saveMessage(ticket, Sender.ADMIN, message);

        ticketRepository.save(ticket);

        return "Admin reply sent";
    }

    // ================= SAVE MESSAGE =================
    private void saveMessage(Ticket ticket, Sender sender, String text) {

        Message msg = new Message();
        msg.setTicket(ticket);
        msg.setSender(sender);
        msg.setMessage(text);
        msg.setTimestamp(LocalDateTime.now());

        messageRepository.save(msg);
    }

    // ================= EXTRACT REPLY =================
    private String extractReply(String aiResponse) {
        try {
            JsonNode node = objectMapper.readTree(aiResponse);

            if (node.has("reply")) {
                return node.get("reply").asText();
            }

        } catch (Exception ignored) {
        }

        return aiResponse; // fallback
    }

    // ================= ESCALATION CHECK =================
    private boolean isEscalation(String query) {
        String q = query.toLowerCase();
        return q.contains("refund") ||
                q.contains("complaint") ||
                q.contains("angry");
    }
}