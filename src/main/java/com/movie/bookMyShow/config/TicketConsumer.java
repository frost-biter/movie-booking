package com.movie.bookMyShow.config;

import com.movie.bookMyShow.dto.TicketDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TicketConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketConsumer.class);

    @KafkaListener(topics = "book_movie", groupId = "ticket_group", containerFactory = "bookMovieKafkaListenerContainerFactory")
    public void consumeTicket(ConsumerRecord<String , TicketDTO> record) {
        var ticketDTO = record.value();
        log.info("record consumed in ticket consumer , key: {}, value: {}", record.key() , record.value());
        System.out.println("Consumed Message :" + ticketDTO.toString());
        log.info("exiting ticket consumer!!!");
    }
}
