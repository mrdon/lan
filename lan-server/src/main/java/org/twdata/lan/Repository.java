package org.twdata.lan;

import javax.xml.bind.annotation.XmlElement;
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
    private String url;
    private Map<String,String> properties;

    private Repository() {}
    
    public Repository(String url, Map<String, String> properties) {
        this.url = url;
        this.properties = properties;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
