package com.accenture.dhruv.healthdekho.recommendation.repository;

import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationRequest;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AiRecommendationRequestRepository {

    private final EntityManager entityManager;

    public AiRecommendationRequestRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AiRecommendationRequest create(AiRecommendationRequest entity) {
        entityManager.persist(entity);
        return entity;
    }

    public Optional<AiRecommendationRequest> findById(long id) {
        return Optional.ofNullable(entityManager.find(AiRecommendationRequest.class, id));
    }

    public List<AiRecommendationRequest> findAll() {
        return entityManager.createQuery("select e from AiRecommendationRequest e order by e.id", AiRecommendationRequest.class)
                .getResultList();
    }

    public void delete(AiRecommendationRequest entity) {
        entityManager.remove(entity);
    }

    public void flush() {
        entityManager.flush();
    }

    public void lockForSeeding() {
        entityManager.createNativeQuery("lock table ai_recommendation_request in share row exclusive mode").executeUpdate();
    }

    public int insertSeed(Map<String, String> row) {
        return entityManager.createNativeQuery("""
                insert into ai_recommendation_request (recommendation_request_id, customer_id, insurance_goals,
                        preferred_term, preferred_policy_type, calculated_age, model_name, model_version,
                        prompt_version, requested_at)
                values (cast(:recommendation_request_id as bigint), cast(:customer_id as bigint), :insurance_goals,
                        cast(nullif(:preferred_term, '') as integer), :preferred_policy_type, cast(:calculated_age
                        as integer), :model_name, :model_version, :prompt_version, cast(:requested_at as
                        timestamptz))
                on conflict (recommendation_request_id) do nothing
                """)
                .setParameter("recommendation_request_id", row.get("recommendation_request_id"))
                .setParameter("customer_id", row.get("customer_id"))
                .setParameter("insurance_goals", row.get("insurance_goals"))
                .setParameter("preferred_term", row.get("preferred_term"))
                .setParameter("preferred_policy_type", row.get("preferred_policy_type"))
                .setParameter("calculated_age", row.get("calculated_age"))
                .setParameter("model_name", row.get("model_name"))
                .setParameter("model_version", row.get("model_version"))
                .setParameter("prompt_version", row.get("prompt_version"))
                .setParameter("requested_at", row.get("requested_at"))
                .executeUpdate();
    }

    public void advanceIdentity() {
        entityManager.createNativeQuery("""
                select setval(pg_get_serial_sequence('ai_recommendation_request', 'recommendation_request_id'),
                    greatest(nextval(pg_get_serial_sequence('ai_recommendation_request', 'recommendation_request_id')), max(recommendation_request_id)))
                from ai_recommendation_request
                """).getSingleResult();
    }
}
