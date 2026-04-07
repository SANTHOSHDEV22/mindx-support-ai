package com.mindx.supportai.repository;

import com.mindx.supportai.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByTicketId(Long ticketId);
}