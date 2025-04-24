package com.credix.edcHtmlToPdf.mysqlEDC.services;

import com.credix.edcHtmlToPdf.Input;
import com.credix.edcHtmlToPdf.Parameters;
import com.credix.edcHtmlToPdf.Params;
import com.credix.edcHtmlToPdf.mysqlEDC.entities.Template;
import com.credix.edcHtmlToPdf.mysqlEDC.repositories.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.credix.edcHtmlToPdf.constants.EdcConstants.CATEGORY;
import static com.credix.edcHtmlToPdf.constants.EdcConstants.SECTION;

@Service
public class TemplateControlService {

    Params paramsObj;
    Input inputObj;
    Parameters parameters;
    Template template;

    private final TemplateRepository templateRepository;

    @Autowired
    public TemplateControlService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public String getTemplate() {
        parameters = new Parameters();
        inputObj = parameters.getInputParams();
        String cutDate = inputObj.getCartera();
        String text = templateRepository.findTemplateByFilter(cutDate, CATEGORY, SECTION);
        template = new Template();
        template.setText(text);
        return template.getText();
    }

}
