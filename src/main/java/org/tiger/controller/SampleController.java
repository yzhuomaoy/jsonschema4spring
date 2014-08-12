package org.tiger.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.tiger.annotation.JsonSchemaResource;
import org.tiger.pojo.POJO;

@Controller
@RequestMapping("sample")
public class SampleController {
    
    private static Logger log = LoggerFactory.getLogger(SampleController.class);
    
    @RequestMapping(method = RequestMethod.POST)
    @JsonSchemaResource(filename="sample", property="testValidator")
    public void testPost(@RequestBody POJO pojo) {
    	
    	System.out.println(pojo);
    }
}
