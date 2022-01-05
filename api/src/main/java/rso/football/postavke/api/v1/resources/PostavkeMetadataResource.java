package rso.football.postavke.api.v1.resources;

import com.kumuluz.ee.cors.annotations.CrossOrigin;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
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

    @Operation(description = "Get all postavke metadata.", summary = "Get all metadata")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "List of postavke metadata",
                    content = @Content(schema = @Schema(implementation = PostavkeMetadata.class, type = SchemaType.ARRAY)),
                    headers = {@Header(name = "X-Total-Count", description = "Number of objects in list")}
            )})
    @GET
    public Response getPostavkeMetadata() {

        List<PostavkeMetadata> postavkeMetadata = postavkeMetadataBean.getPostavkeMetadataFilter(uriInfo);

        return Response.status(Response.Status.OK).entity(postavkeMetadata).build();
    }

    @Operation(description = "Get metadata for one postavka.", summary = "Get metadata for one postavka")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Igrisce metadata",
                    content = @Content(
                            schema = @Schema(implementation = PostavkeMetadata.class))
            )})
    @GET
    @Path("/{postavkeMetadataId}")
    public Response getPostavkeMetadata(@Parameter(description = "Metadata ID.", required = true)
                                            @PathParam("postavkeMetadataId") Integer postavkeMetadataId) {

        PostavkeMetadata postavkeMetadata = postavkeMetadataBean.getPostavkeMetadata(postavkeMetadataId);

        if (postavkeMetadata == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(postavkeMetadata).build();
    }

    @Operation(description = "Get place trenerjev.", summary = "Get place from trenerji")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Place metadata",
                    content = @Content(
                            schema = @Schema(implementation = PlaceMetadata.class))
            )})
    @GET
    @Path("/place")
    public Response getPlaceMetadata() {

        List<PlaceMetadata> placeMetadata = postavkeMetadataBean.getPlaceMetadata();

        if (placeMetadata == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(placeMetadata).build();
    }

    @Operation(description = "Add postavka metadata.", summary = "Add metadata")
    @APIResponses({
            @APIResponse(responseCode = "201",
                    description = "Metadata successfully added."
            ),
            @APIResponse(responseCode = "400", description = "Bad request.")
    })
    @POST
    public Response createPostavkeMetadata(@RequestBody(
            description = "DTO object with postavke metadata.",
            required = true, content = @Content(
            schema = @Schema(implementation = PostavkeMetadata.class))) PostavkeMetadata postavkeMetadata) {
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

    @Operation(description = "Update metadata for on postavka.", summary = "Update metadata")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Metadata successfully updated."
            ),
            @APIResponse(responseCode = "404", description = "Not found.")
    })
    @PUT
    @Path("{postavkeMetadataId}")
    public Response putPostavkeMetadata(@Parameter(description = "Metadata ID.", required = true)
                                            @PathParam("postavkeMetadataId") Integer postavkeMetadataId,
                                        @RequestBody(
                                                description = "DTO object with postavka metadata.",
                                                required = true, content = @Content(
                                                schema = @Schema(implementation = PostavkeMetadata.class)))
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

    @Operation(description = "Delete metadata for one postavka.", summary = "Delete metadata")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Metadata successfully deleted."
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Not found."
            )
    })
    @DELETE
    @Path("{postavkeMetadataId}")
    public Response deletePostavkeMetadata(@Parameter(description = "Metadata ID.", required = true)
                                               @PathParam("postavkeMetadataId") Integer postavkeMetadataId) {

        boolean deleted = postavkeMetadataBean.deletePostavkeMetadata(postavkeMetadataId);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}