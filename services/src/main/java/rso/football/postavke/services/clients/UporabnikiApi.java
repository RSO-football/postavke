package rso.football.postavke.services.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.Dependent;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/uporabniki/trenerjiId")
@RegisterRestClient(configKey = "uporabniki-api")
@Dependent
public interface UporabnikiApi {

    @GET
    String getTrenerjiId();
}
