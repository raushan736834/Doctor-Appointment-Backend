package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.DTOs.EmailTemplateData;
import com.harsh.AppointDoctor.Models.AppointmentBooking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
public class EmailService2 {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendAppointmentEmail(String to, String subject, String emailType, AppointmentBooking appointment) {
        try {
            EmailTemplateData templateData = new EmailTemplateData(emailType, appointment);

            // Set appropriate message based on email type
            switch (emailType.toLowerCase()) {
                case "appointment confirmation":
                    templateData.setMessage("Your appointment has been successfully booked. Please arrive 15 minutes early and bring your ID and insurance card.");
                    break;
                case "appointment rescheduled":
                    templateData.setMessage("Your appointment has been rescheduled. Please note the new date and time below.");
                    break;
                case "appointment cancelled":
                    templateData.setMessage("Your appointment has been cancelled. If you need to book a new appointment, please use the booking link below.");
                    break;
                default:
                    templateData.setMessage("Please review the appointment details below.");
            }

            String htmlContent = generateEmailContent(templateData);
            sendHtmlEmail(to, subject, htmlContent);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String generateEmailContent(EmailTemplateData templateData) {
        Context context = new Context();
        context.setVariable("data", templateData);
        return templateEngine.process("email-template", context);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("noreply@mediclinic.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
