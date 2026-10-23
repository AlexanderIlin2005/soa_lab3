package org.itmo.vehicle.infrastructure.web;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;

@ControllerAdvice
class StrictParameterBinding {

    @InitBinder
    void strictIntegers(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(Integer.valueOf(text));
            }
        });
    }
}
