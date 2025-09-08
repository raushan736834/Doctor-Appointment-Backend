package com.harsh.AppointDoctor.Services;

import com.harsh.AppointDoctor.Models.ContactUs;
import com.harsh.AppointDoctor.Repo.ContactUsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ContactUsService {

    private final ContactUsRepo contactUsRepo;
    private final MailService mailService;

    @Transactional
    public ContactUs contactUs(ContactUs details) {
        details = contactUsRepo.save(details);

        mailService.sendSimpleEmail("try.harsh95@gmail.com", details.getSubject(),
                details.getMessage());
        return details;
    }
}
