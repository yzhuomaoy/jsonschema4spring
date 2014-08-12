package org.tiger.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import org.tiger.exception.JsonValidationException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;

public class CustomizedMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
    
    private static Logger log = LoggerFactory.getLogger(CustomizedMappingJackson2HttpMessageConverter.class);
    
    private JsonSchemaStore schemaStore = null;
    
    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        inputMessage = doValidationPreRead(inputMessage);
        return super.read(type, contextClass, inputMessage);
    }
    
    
    private HttpInputMessage doValidationPreRead(HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        HttpServletRequest servletRequest = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        String method = servletRequest.getMethod();
        String uri = (String) servletRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        
        if (schemaStore != null && schemaStore.checkReqestSchemaExist(method, uri)) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                IOUtils.copy(inputMessage.getBody(), baos);
    
                InputStream inputCloned = new ByteArrayInputStream(baos.toByteArray());
                
                final InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
                
                ProcessingReport report;
                report = schemaStore.validate(method, uri, new CloseShieldInputStream(inputCloned));
                
                if (!report.isSuccess()) {
                    log.debug("Processing report:\n " + report);
                    List<String> descriptions = new ArrayList<String>();
                    for(Iterator<ProcessingMessage> iter = report.iterator(); iter.hasNext();) {
                        ProcessingMessage message = iter.next();
                        descriptions.add(message.getMessage());
                    }
                    throw new JsonValidationException(descriptions.get(0));
                }
                
                inputMessage = new ServletServerHttpRequest(servletRequest) {
                    @Override
                    public InputStream getBody() throws IOException {
                        return inputStream;
                    }
                };
                
            } catch (IOException ioe) {
               log.warn("IOException while reading json", ioe);
               throw new JsonValidationException(ioe.getMessage(), ioe);
            } catch (ProcessingException pe) {
                log.warn("ProcessingException while reading json", pe);
                throw new JsonValidationException(pe.getMessage(), pe);
            }
        }
        return inputMessage;
    }
    
	public JsonSchemaStore getSchemaStore() {
		return schemaStore;
	}


	public void setSchemaStore(JsonSchemaStore schemaStore) {
		this.schemaStore = schemaStore;
	}

}

