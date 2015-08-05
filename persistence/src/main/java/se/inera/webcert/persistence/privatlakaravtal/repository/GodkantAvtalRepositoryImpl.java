package se.inera.webcert.persistence.privatlakaravtal.repository;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.webcert.persistence.privatlakaravtal.model.GodkantAvtal;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by eriklupander on 2015-08-05.
 */
public class GodkantAvtalRepositoryImpl implements GodkantAvtalRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(GodkantAvtalRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void approveAvtal(String userId, Integer avtalVersion) {

        if (userHasApprovedAvtal(userId, avtalVersion)) {
            log.info("User '" + userId + "' has already approved privatlakaravtal version '" + avtalVersion + "'");
            return;
        }

        GodkantAvtal godkantAvtal = new GodkantAvtal();
        godkantAvtal.setAnvandarId(userId);
        godkantAvtal.setAvtalVersion(avtalVersion);
        godkantAvtal.setGodkandDatum(LocalDateTime.now());
        entityManager.persist(godkantAvtal);
    }

    @Override
    public boolean userHasApprovedAvtal(String userId, Integer avtalVersion) {
        try {
            GodkantAvtal godkantAvtal = entityManager.createQuery("SELECT ga FROM GodkantAvtal ga WHERE ga.anvandarId = :userId AND ga.avtalVersion = :avtalVersion", GodkantAvtal.class)
                    .setParameter("userId", userId)
                    .setParameter("avtalVersion", avtalVersion)
                    .getSingleResult();
            return godkantAvtal != null;
        } catch (NoResultException e) {
            return false;
        } catch (NonUniqueResultException e) {
            // This should never occur if we set up our constraints correctly.
            return true;
        }
    }

    @Override
    public void removeUserApprovement(String userId, Integer avtalVersion) {
        try {
            GodkantAvtal godkantAvtal = entityManager.createQuery("SELECT ga FROM GodkantAvtal ga WHERE ga.anvandarId = :userId AND ga.avtalVersion = :avtalVersion", GodkantAvtal.class)
                    .setParameter("userId", userId)
                    .setParameter("avtalVersion", avtalVersion)
                    .getSingleResult();

            if (godkantAvtal != null) {
                entityManager.remove(godkantAvtal);
            }
        } catch (NoResultException e) {
            log.warn("Could not remove GodkantAvtal for user '" + userId + "', avtal version '" + avtalVersion + "'. No approvment found.");
        }
    }

    @Override
    public void removeAllUserApprovments(String userId) {
        List<GodkantAvtal> godkandaAvtal = entityManager.createQuery("SELECT ga FROM GodkantAvtal ga WHERE ga.anvandarId = :userId", GodkantAvtal.class)
                .setParameter("userId", userId)
                .getResultList();

        for(GodkantAvtal godkantAvtal : godkandaAvtal) {
            entityManager.remove(godkantAvtal);
        }
    }
}
