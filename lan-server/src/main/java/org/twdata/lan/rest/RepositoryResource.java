package org.twdata.lan.rest;

import org.twdata.lan.Repository;
import org.twdata.lan.manager.RepositoryManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 30/01/2009
 * Time: 11:04:36 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/repository")
public class RepositoryResource {

    private final RepositoryManager mgr = new RepositoryManager();
    @GET
    @Produces("application/xml")
    public List<Repository> get() {
        return mgr.getAll();
    }

    @Path("{id}")
    @GET
    public Response getPluginResource(@PathParam("id") String id)
    {
        return Response.ok(mgr.get(id)).build();
    }

}
