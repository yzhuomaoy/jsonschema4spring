package org.tiger.exception;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ExceptionControllerAdvice {
    private Logger logger = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e)
            throws Exception {

        ModelAndView mav = new ModelAndView();
        mav.addObject("error", e.getMessage());
        return mav;
    }
    
}
