package org.twdata.lan;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 30/01/2009
 * Time: 11:19:31 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
public class Repository {
    private String name;
    private String path;
    private Map<String,String> properties;

    private Repository() {
    }
    
    public Repository(String name, String path, Map<String, String> properties) {
        this.name = name;
        this.path = path;
        this.properties = properties;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
