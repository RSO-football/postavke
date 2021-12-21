package rso.football.postavke.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import rso.football.postavke.lib.PostavkeMetadata;
import rso.football.postavke.models.converters.PostavkeMetadataConverter;
import rso.football.postavke.models.entities.PostavkeMetadataEntity;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequestScoped
public class PostavkeMetadataBean {

    private Logger log = Logger.getLogger(PostavkeMetadataBean.class.getName());

    @Inject
    private EntityManager em;

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