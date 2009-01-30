package org.twdata.lan.manager;

import org.twdata.lan.Repository;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 30/01/2009
 * Time: 11:33:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryManager {

    private static final Map<String,Repository> data = new HashMap<String,Repository>() {{
        put("one", new Repository("http://localhost/one", null));
        put("two", new Repository("http://localhost/one", null));
        put("three", new Repository("http://localhost/one", Collections.singletonMap("foo", "bar")));
    }};

    public List<Repository> getAll() {
        return new ArrayList<Repository>(data.values());
    }

    public Repository get(String id) {
        return data.get(id);
    }

    public void remove(String id) {
        data.remove(id);
    }

    public void update(String id, Repository repository) {
        data.put(id, repository);
    }

    public void add(String id, Repository repository) {
        data.put(id, repository);
    }


}
