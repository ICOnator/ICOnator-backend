package io.modum.tokenapp.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {

    private TemplateEngine templateEngine;

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String buildConfirmationEmail(String message) {



        Context context = new Context();
        context.setVariable("confirmationEmaiLink", message);
        return templateEngine.process("confirmation_email", context);
    }

}
