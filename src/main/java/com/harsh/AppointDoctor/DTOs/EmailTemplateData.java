package com.harsh.AppointDoctor.DTOs;

import com.harsh.AppointDoctor.Models.AppointmentBooking;
import lombok.Data;

@Data
public class EmailTemplateData {
    private String emailType;
    private String statusBadge;
    private String statusClass;
    private AppointmentBooking appointment;
    private String message;
    private String confirmUrl;
    private String rescheduleUrl;
    private String cancelUrl;
    private String clinicName = "HeyDoctor";
    private String clinicAddress = "123 Health Street, Medical District, Jaipur 302001";
    private String clinicPhone = "+91 98765 43210";
    private String clinicEmail = "info@medicareClinic.com";

    // Constructors
    public EmailTemplateData() {}

    public EmailTemplateData(String emailType, AppointmentBooking appointment) {
        this.emailType = emailType;
        this.appointment = appointment;
        this.setStatusFromAppointment();
        this.generateUrls();
    }

    private void setStatusFromAppointment() {
        switch (appointment.getStatus()) {
            case BOOKED:
                this.statusBadge = "✓ Booked";
                this.statusClass = "status-confirmed";
                break;
            case RESCHEDULED:
                this.statusBadge = "⏳ Rescheduled";
                this.statusClass = "status-pending";
                break;
            case CANCELLED:
                this.statusBadge = "✕ Cancelled";
                this.statusClass = "status-cancelled";
                break;
            default:
                this.statusBadge = "📅 Scheduled";
                this.statusClass = "status-pending";
        }
    }

    private void generateUrls() {
        String baseUrl = "https://clinic.com";
        String appointmentId = appointment.getAppointmentId();
        this.confirmUrl = baseUrl + "/confirm/" + appointmentId;
        this.rescheduleUrl = baseUrl + "/reschedule/" + appointmentId;
        this.cancelUrl = baseUrl + "/cancel/" + appointmentId;
    }
}
