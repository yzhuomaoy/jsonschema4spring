package org.tiger.annotation;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.tiger.json.JsonSchemaStore;


public class CustomizedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private final JsonSchemaStore schemaStore;

    public CustomizedRequestMappingHandlerMapping(JsonSchemaStore schemaStore) {
        this.schemaStore = schemaStore;
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);

        if (info != null) {
            prepareJsonSchema(method, info);
        }

        return info;
    }

    private void prepareJsonSchema(Method method, RequestMappingInfo info) {
        JsonSchemaResource jsonschema = AnnotationUtils.findAnnotation(method, JsonSchemaResource.class);
        if (jsonschema != null) {
            for (String uri : info.getPatternsCondition().getPatterns()) {
                for (RequestMethod uriMethod : info.getMethodsCondition().getMethods()) {
                    schemaStore.addSecureResource(uriMethod.toString(), uri, jsonschema);
                }
            }
        }
    }

}
