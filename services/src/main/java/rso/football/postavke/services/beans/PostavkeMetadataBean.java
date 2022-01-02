package rso.football.postavke.services.beans;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import rso.football.postavke.lib.PostavkeMetadata;
import rso.football.postavke.lib.RekvizitiMetadata;
import rso.football.postavke.models.converters.PostavkeMetadataConverter;
import rso.football.postavke.models.entities.PostavkeMetadataEntity;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequestScoped
public class PostavkeMetadataBean {

    private Logger log = Logger.getLogger(PostavkeMetadataBean.class.getName());

    @Inject
    private EntityManager em;

    private Client httpClient;
    private String baseUrlRezervacije;
    private String baseUrlRekviziti;

    @PostConstruct
    private void init() {
        String uniqueID = UUID.randomUUID().toString();
        log.info("Inicializacija zrna: " + PostavkeMetadataBean.class.getSimpleName() + " id: " + uniqueID);

        httpClient = ClientBuilder.newClient();
        baseUrlRezervacije = ConfigurationUtil.getInstance().get("rezervacije-storitev.url").orElse("http://localhost:8082/");
        baseUrlRekviziti = ConfigurationUtil.getInstance().get("rekviziti-storitev.url").orElse("http://localhost:8085/");
    }

    public List<PostavkeMetadata> getPostavkeMetadata() {

        TypedQuery<PostavkeMetadataEntity> query = em.createNamedQuery(
                "PostavkeMetadataEntity.getAll", PostavkeMetadataEntity.class);

        List<PostavkeMetadataEntity> resultList = query.getResultList();

        return resultList.stream().map(PostavkeMetadataConverter::toDto).collect(Collectors.toList());

    }

    public List<PostavkeMetadata> getPostavkeMetadataFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, PostavkeMetadataEntity.class, queryParameters).stream()
                .map(PostavkeMetadataConverter::toDto).collect(Collectors.toList());
    }

    public PostavkeMetadata getPostavkeMetadata(Integer id) {

        PostavkeMetadataEntity postavkeMetadataEntity = em.find(PostavkeMetadataEntity.class, id);

        if (postavkeMetadataEntity == null) {
            throw new NotFoundException();
        }

        PostavkeMetadata postavkeMetadata = PostavkeMetadataConverter.toDto(postavkeMetadataEntity);

        return postavkeMetadata;
    }

    public PostavkeMetadata createPostavkeMetadata(PostavkeMetadata postavkeMetadata) {

        PostavkeMetadataEntity postavkeMetadataEntity = PostavkeMetadataConverter.toEntity(postavkeMetadata);
        log.info(postavkeMetadataEntity.getUporabnikID().toString());

        Integer trenerRezervacije = Integer.parseInt(getTrenerRezervacije(postavkeMetadataEntity.getUporabnikID()));
        log.info("Trener " + postavkeMetadataEntity.getUporabnikID() + " ima " + Integer.toString(trenerRezervacije) + "rezervacij");
        Float pay = trenerRezervacije * (float) 100.0;

        // informacije o prodanih rekvizitih
        Integer rekvizitiCost = getSkupnaCenaRekviziti();
        pay += rekvizitiCost * (float) 0.1;

        postavkeMetadataEntity.setPay(pay);

        try {
            beginTx();
            em.persist(postavkeMetadataEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
        }

        if (postavkeMetadataEntity.getId() == null) {
            throw new RuntimeException("Entity was not persisted");
        }

        return PostavkeMetadataConverter.toDto(postavkeMetadataEntity);
    }

    public PostavkeMetadata putPostavkeMetadata(Integer id, PostavkeMetadata postavkeMetadata) {

        PostavkeMetadataEntity c = em.find(PostavkeMetadataEntity.class, id);

        if (c == null) {
            return null;
        }

        PostavkeMetadataEntity updatedPostavkeMetadataEntity = PostavkeMetadataConverter.toEntity(postavkeMetadata);

        try {
            beginTx();
            updatedPostavkeMetadataEntity.setId(c.getId());
            updatedPostavkeMetadataEntity = em.merge(updatedPostavkeMetadataEntity);
            commitTx();
        }
        catch (Exception e) {
            rollbackTx();
        }

        return PostavkeMetadataConverter.toDto(updatedPostavkeMetadataEntity);
    }

    public boolean deletePostavkeMetadata(Integer id) {

        PostavkeMetadataEntity postavkeMetadata = em.find(PostavkeMetadataEntity.class, id);

        if (postavkeMetadata != null) {
            try {
                beginTx();
                em.remove(postavkeMetadata);
                commitTx();
            }
            catch (Exception e) {
                rollbackTx();
            }
        }
        else {
            return false;
        }

        return true;
    }

    public Integer getSkupnaCenaRekviziti(){
        String url = baseUrlRekviziti + "v1/rekviziti/skupna";
        log.info("url je " + url);

        try {
            return httpClient
                    .target(url)
                    .request().get(Integer.class);
        } catch (WebApplicationException | ProcessingException e){
            throw new InternalServerErrorException(e);
        }

    }

    public String getTrenerRezervacije(Integer trenerId){
        String url = baseUrlRezervacije + "v1/rezervacije/trener/" + trenerId;
        log.info("url je " + url);

        try {
            return httpClient
                    .target(url)
                    .request().get(String.class);
        } catch (WebApplicationException | ProcessingException e){
            throw new InternalServerErrorException(e);
        }
    }

    private void beginTx() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    private void commitTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}