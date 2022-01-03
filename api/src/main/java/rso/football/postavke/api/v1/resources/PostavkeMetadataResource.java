package rso.football.postavke.api.v1.resources;

import com.kumuluz.ee.cors.annotations.CrossOrigin;
import rso.football.postavke.lib.PlaceMetadata;
import rso.football.postavke.lib.PostavkeMetadata;
import rso.football.postavke.services.beans.PostavkeMetadataBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/postavke")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@CrossOrigin(supportedMethods = "GET, POST, DELETE, PUT, HEAD, OPTIONS")
public class PostavkeMetadataResource {

    private Logger log = Logger.getLogger(PostavkeMetadataResource.class.getName());

    @Inject
    private PostavkeMetadataBean postavkeMetadataBean;

    @Context
    protected UriInfo uriInfo;

    @GET
    public Response getPostavkeMetadata() {

        List<PostavkeMetadata> postavkeMetadata = postavkeMetadataBean.getPostavkeMetadataFilter(uriInfo);

        return Response.status(Response.Status.OK).entity(postavkeMetadata).build();
    }

    @GET
    @Path("/{postavkeMetadataId}")
    public Response getPostavkeMetadata(@PathParam("postavkeMetadataId") Integer postavkeMetadataId) {

        PostavkeMetadata postavkeMetadata = postavkeMetadataBean.getPostavkeMetadata(postavkeMetadataId);

        if (postavkeMetadata == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(postavkeMetadata).build();
    }

    @GET
    @Path("/place")
    public Response getPlaceMetadata() {

        List<PlaceMetadata> placeMetadata = postavkeMetadataBean.getPlaceMetadata();

        if (placeMetadata == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(placeMetadata).build();
    }

    @POST
    public Response createPostavkeMetadata(PostavkeMetadata postavkeMetadata) {
        if ((postavkeMetadata.getUporabnikID() == null || new Float(postavkeMetadata.getPay()) == null)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else {
            if (postavkeMetadataBean.existsPostavakaTrenerja(postavkeMetadata.getUporabnikID())){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            postavkeMetadata = postavkeMetadataBean.createPostavkeMetadata(postavkeMetadata);
            if (postavkeMetadata == null){
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        return Response.status(Response.Status.CREATED).entity(postavkeMetadata).build();

    }

    @PUT
    @Path("{postavkeMetadataId}")
    public Response putPostavkeMetadata(@PathParam("postavkeMetadataId") Integer postavkeMetadataId,
                                     PostavkeMetadata postavkeMetadata) {

//        if (postavkeMetadata.getUporabnikID() != null && postavkeMetadataBean.existsPostavakaTrenerja(postavkeMetadata.getUporabnikID())){
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }

        postavkeMetadata = postavkeMetadataBean.putPostavkeMetadata(postavkeMetadataId, postavkeMetadata);

        if (postavkeMetadata == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();

    }

    @DELETE
    @Path("{postavkeMetadataId}")
    public Response deletePostavkeMetadata(@PathParam("postavkeMetadataId") Integer postavkeMetadataId) {

        boolean deleted = postavkeMetadataBean.deletePostavkeMetadata(postavkeMetadataId);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}