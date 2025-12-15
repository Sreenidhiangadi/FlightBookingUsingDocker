package com.flightapp.serviceimpl;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.flightapp.messaging.BookingEvent;

class NotificationServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void bookingConfirmed_shouldSendEmail() {
        BookingEvent event = new BookingEvent();
        event.setEventType("BOOKING_CONFIRMED");
        event.setPnr("PNR123");
        event.setUserEmail("user@example.com");
        event.setTotalPrice(500.0);

        notificationService.handleBookingEvent(event);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo() != null;
        assert msg.getTo()[0].equals("user@example.com");
        assert msg.getSubject().equals("Your flight booking is confirmed - PNR PNR123");
        assert msg.getText().contains("PNR123");
    }

    @Test
    void bookingCancelled_shouldSendEmail() {
        BookingEvent event = new BookingEvent();
        event.setEventType("BOOKING_CANCELLED");
        event.setPnr("PNR999");
        event.setUserEmail("user2@example.com");
        event.setTotalPrice(0.0);

        notificationService.handleBookingEvent(event);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo()[0].equals("user2@example.com");
        assert msg.getSubject().equals("Your flight booking is cancelled - PNR PNR999");
        assert msg.getText().contains("PNR999");
    }

    @Test
    void unknownEvent_shouldSendDefaultEmail() {
        BookingEvent event = new BookingEvent();
        event.setEventType("UNKNOWN");
        event.setPnr("PNR777");
        event.setUserEmail("user3@example.com");
        event.setTotalPrice(100.0);

        notificationService.handleBookingEvent(event);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assert msg.getTo()[0].equals("user3@example.com");
        assert msg.getSubject().equals("Flight booking update");
        assert msg.getText().contains("PNR777");
    }

    @Test
    void mailFailure_shouldNotThrowException() {
        doThrow(new RuntimeException("SMTP down"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        BookingEvent event = new BookingEvent();
        event.setEventType("BOOKING_CONFIRMED");
        event.setPnr("PNR123");
        event.setUserEmail("user@example.com");
        event.setTotalPrice(500.0);

        notificationService.handleBookingEvent(event);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
