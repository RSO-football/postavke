package rso.football.postavke.services.beans;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import rso.football.postavke.lib.PlaceMetadata;
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
import java.util.Arrays;
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
    private String baseUrlUporabniki;

    @PostConstruct
    private void init() {
        String uniqueID = UUID.randomUUID().toString();
        log.info("Inicializacija zrna: " + PostavkeMetadataBean.class.getSimpleName() + " id: " + uniqueID);

        httpClient = ClientBuilder.newClient();
        baseUrlRezervacije = ConfigurationUtil.getInstance().get("rezervacije-storitev.url").orElse("http://localhost:8082/");
        baseUrlRekviziti = ConfigurationUtil.getInstance().get("rekviziti-storitev.url").orElse("http://localhost:8085/");
        baseUrlUporabniki = ConfigurationUtil.getInstance().get("uporabniki-storitev.url").orElse("http://localhost:8083/");
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

        String trenerId = uriInfo.getQueryParameters().getFirst("trenerId");

        List<PostavkeMetadata> results = JPAUtils.queryEntities(em, PostavkeMetadataEntity.class, queryParameters).stream()
                .map(PostavkeMetadataConverter::toDto).collect(Collectors.toList());

        if (trenerId != null){
            List<PostavkeMetadata> newResults = new ArrayList<>();
            for (PostavkeMetadata postavka : results){
                if (postavka.getUporabnikID() == Integer.parseInt(trenerId)){
                    newResults.add(postavka);
                }
            }
            return newResults;
        }

        return results;
    }

    public boolean existsPostavakaTrenerja(Integer uporabnikID) {
        List<PostavkeMetadata> postavkeMetadata = getPostavkeMetadata();
        for (PostavkeMetadata p : postavkeMetadata){
            if (p.getUporabnikID() == uporabnikID){
                return true;
            }
        }
        return false;
    }

    public PostavkeMetadata getPostavkeMetadataByTrenerId(Integer trenerId){
        TypedQuery<PostavkeMetadataEntity> query = em.createNamedQuery(
                "PostavkeMetadataEntity.getAllTrenerId", PostavkeMetadataEntity.class);
        query.setParameter(1, trenerId);

        List<PostavkeMetadata> results = query.getResultList().stream().map(PostavkeMetadataConverter::toDto).collect(Collectors.toList());

        if (results.size() > 0){
            return results.get(0);
        }
        return null;
    }

    public PostavkeMetadata getPostavkeMetadata(Integer id) {

        PostavkeMetadataEntity postavkeMetadataEntity = em.find(PostavkeMetadataEntity.class, id);

        if (postavkeMetadataEntity == null) {
            throw new NotFoundException();
        }

        PostavkeMetadata postavkeMetadata = PostavkeMetadataConverter.toDto(postavkeMetadataEntity);

        return postavkeMetadata;
    }

    public List<PlaceMetadata> getPlaceMetadata() {
        String trenerjiString = getTrenerjiId();
        List<Integer> trenerjiId = Arrays.stream(trenerjiString.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        List<PlaceMetadata> place = new ArrayList<>();

        for (Integer trenerId : trenerjiId){
            double salary = 0.0;
            Integer trenerRezervacije = Integer.parseInt(getTrenerRezervacije(trenerId));
            log.info("Trener " + trenerId + " ima " + Integer.toString(trenerRezervacije) + "rezervacij");
            PostavkeMetadata postavkaTrenerja = getPostavkeMetadataByTrenerId(trenerId);
            if (postavkaTrenerja != null){
                salary = postavkaTrenerja.getPay() * trenerRezervacije;
            }

            Integer rekvizitiCost = getCenaRekvizitiTrenerja(trenerId);
            salary += rekvizitiCost * 0.1;

            place.add(new PlaceMetadata(trenerId, salary));
        }

        return place;
    }

    public PostavkeMetadata createPostavkeMetadata(PostavkeMetadata postavkeMetadata) {

        PostavkeMetadataEntity postavkeMetadataEntity = PostavkeMetadataConverter.toEntity(postavkeMetadata);

        String trenerjiString = getTrenerjiId();
        List<Integer> trenerjiId = Arrays.stream(trenerjiString.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        System.out.println(trenerjiString);

        if (!trenerjiId.contains(postavkeMetadataEntity.getUporabnikID())){
            return null;
        }
//        log.info(postavkeMetadataEntity.getUporabnikID().toString());
//
//        Integer trenerRezervacije = Integer.parseInt(getTrenerRezervacije(postavkeMetadataEntity.getUporabnikID()));
//        log.info("Trener " + postavkeMetadataEntity.getUporabnikID() + " ima " + Integer.toString(trenerRezervacije) + "rezervacij");
//        Float pay = trenerRezervacije * (float) 100.0;
//
//        // informacije o prodanih rekvizitih
//        Integer rekvizitiCost = getCenaRekvizitiTrenerja(postavkeMetadataEntity.getUporabnikID());
//        pay += rekvizitiCost * (float) 0.1;
//
//        postavkeMetadataEntity.setPay(pay);

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

    public Integer getCenaRekvizitiTrenerja(Integer trenerId){
        String url = baseUrlRekviziti + "v1/rekviziti/cena/" + trenerId;
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

    public String getTrenerjiId(){
        String url = baseUrlUporabniki + "v1/uporabniki/trenerjiId";
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