package org.twdata.lan.rest;

import org.twdata.lan.Repository;
import org.twdata.lan.RepositoryService;
import org.twdata.lan.manager.RepositoryManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
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

    private final RepositoryService repositoryService;

    public RepositoryResource(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GET
    @Produces("application/xml")
    public List<Repository> get() {
        return repositoryService.getAll();
    }

    @Path("{id}")
    @PUT
    public Response addPluginResource(@PathParam("id") String id, Repository repo)
    {
        repositoryService.add(id, repo);
        return Response.ok("Repository added successfully").build();
    }

}
