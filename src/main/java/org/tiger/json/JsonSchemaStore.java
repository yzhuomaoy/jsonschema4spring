package org.tiger.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tiger.annotation.JsonSchemaResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration;
import com.github.fge.jsonschema.core.load.configuration.LoadingConfigurationBuilder;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

public class JsonSchemaStore {
    
    private static Logger log = LoggerFactory.getLogger(JsonSchemaStore.class);
    
    private static final String URI_BASE = "http://xxx.org/schema/";
    
    private LoadingConfigurationBuilder builder = LoadingConfiguration.newBuilder();
    private LoadingConfiguration loadingCfg;
    private JsonSchemaFactory factory;
    
    private Map<String, JsonNode> schemaCache = new HashMap<String, JsonNode>();
    
    private Map<String, JsonSchemaResource> uriCache = new HashMap<String, JsonSchemaResource>();
    
    private String schemaDir;

    public JsonSchemaStore(String schemaClassPath) {
        loadSchemaNodeFromDir(schemaClassPath);
        loadingCfg = builder.freeze();
        factory = JsonSchemaFactory.newBuilder().setLoadingConfiguration(loadingCfg).freeze();
    }
    
    private String getSecureResource(String method, String uri){
        uri = StringUtils.trim(uri);
        if (uri != null && uri.endsWith(".*")) {
            uri = uri.substring(0, uri.lastIndexOf(".*"));
        } else if (uri != null && uri.endsWith("/")) {
            uri = uri.substring(0, uri.lastIndexOf("/"));
        }
        return new StringBuffer().append(StringUtils.upperCase(method))
                                 .append(":/")
                                 .append(uri)
                                 .toString();
    }
    
    /**
     * load schema json node file from schema class path
     * @param schemaPath schema class path
     */
    private void loadSchemaNodeFromDir(String schemaClassPath) {
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(schemaClassPath);
            if (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                schemaDir = resource.getFile();
            }
            
            log.info("Root json schemas of dir: " + schemaDir);
            
            if (schemaDir != null) {
                List<File> list = new ArrayList<File>();
                File dir = new File(schemaDir);
                
                File[] files = dir.listFiles();
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(".json")) {
                        list.add(f);
                    }
                }
                
                parseJsonNodeResource(list);
            }
        } catch (Exception e) {
            log.error("Error while load json schemas from path: " + schemaClassPath, e);
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * parse json file to json node set put it in cache
     * 
     * @param list list of json file
     */
    private void parseJsonNodeResource(List<File> list) throws IOException {
        for (File file : list) {
            log.info("Load json schema with file: {}", file.getPath());
            
            JsonNode node = JsonLoader.fromFile(file);
            String uri = URI_BASE + file.getName();
            
            builder.preloadSchema(uri, node);
            
            schemaCache.put(uri, node);
            
            log.info("Load json schema with uri: {}, node:\n {}", uri, node);
        }
    }
    
    public boolean checkReqestSchemaExist(String method, String uri) {
    	JsonSchemaResource resource = uriCache.get(this.getSecureResource(method, uri));
        return resource != null ? true : false;
    }
    
    public void addSecureResource(String method, String uri, JsonSchemaResource resource) {
        String secureResource = this.getSecureResource(method, uri);
        String filename = resource.filename();
        String property = resource.property();
        if (StringUtils.isNotBlank(filename)) {
        	String jsonUri = URI_BASE + filename + ".json";
        	JsonNode jsonNode = schemaCache.get(jsonUri);
        	if(jsonNode != null && jsonNode.has(property)) {
        		uriCache.put(secureResource, resource);
        	}
        }
    }
    
    private ProcessingReport validate(String method, String uri, Reader reader) throws ProcessingException, IOException {
        String secureResource = this.getSecureResource(method, uri);
        JsonSchemaResource resource = uriCache.get(secureResource);
        
        if (resource != null) {
            String reqUri = URI_BASE + resource.filename() + ".json#/" + resource.property();
            
            JsonSchema schema = factory.getJsonSchema(reqUri);
            JsonNode instance = JsonLoader.fromReader(reader);
            
            log.debug("Looking validation json schema:\n {} \nfrom method: {}, path: {}, instance:\n {}",
                    new Object[]{reqUri, method, uri, instance});
            
            return schema.validate(instance, true);
        }
        
        throw new ProcessingException("Not mapping schema found for secure resource: " + secureResource);
    }
    
    public ProcessingReport validate(String method, String uri, InputStream in) throws ProcessingException, IOException {
        return validate(method, uri, new InputStreamReader(in));
    }
    
    public ProcessingReport validate(String method, String uri, String json) throws ProcessingException, IOException {
        return validate(method, uri, new StringReader(json));
    }
    
}
