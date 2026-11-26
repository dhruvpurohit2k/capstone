package com.accenture.dhruv.healthdekho.recommendation.repository;

import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationItem;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AiRecommendationItemRepository {

    private final EntityManager entityManager;

    public AiRecommendationItemRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AiRecommendationItem create(AiRecommendationItem entity) {
        entityManager.persist(entity);
        return entity;
    }

    public Optional<AiRecommendationItem> findById(long id) {
        return Optional.ofNullable(entityManager.find(AiRecommendationItem.class, id));
    }

    public List<AiRecommendationItem> findAll() {
        return entityManager.createQuery("select e from AiRecommendationItem e order by e.id", AiRecommendationItem.class)
                .getResultList();
    }

    public void delete(AiRecommendationItem entity) {
        entityManager.remove(entity);
    }

    public void flush() {
        entityManager.flush();
    }

    public void lockForSeeding() {
        entityManager.createNativeQuery("lock table ai_recommendation_item in share row exclusive mode").executeUpdate();
    }

    public int insertSeed(Map<String, String> row) {
        return entityManager.createNativeQuery("""
                insert into ai_recommendation_item (recommendation_item_id, recommendation_request_id, product_id,
                        rank_order, recommendation_reason, suitability, selected_for_registration)
                values (cast(:recommendation_item_id as bigint), cast(:recommendation_request_id as bigint),
                        cast(:product_id as bigint), cast(:rank_order as integer), :recommendation_reason,
                        :suitability, cast(:selected_for_registration as boolean))
                on conflict (recommendation_item_id) do nothing
                """)
                .setParameter("recommendation_item_id", row.get("recommendation_item_id"))
                .setParameter("recommendation_request_id", row.get("recommendation_request_id"))
                .setParameter("product_id", row.get("product_id"))
                .setParameter("rank_order", row.get("rank_order"))
                .setParameter("recommendation_reason", row.get("recommendation_reason"))
                .setParameter("suitability", row.get("suitability"))
                .setParameter("selected_for_registration", row.get("selected_for_registration"))
                .executeUpdate();
    }

    public void advanceIdentity() {
        entityManager.createNativeQuery("""
                select setval(pg_get_serial_sequence('ai_recommendation_item', 'recommendation_item_id'),
                    greatest(nextval(pg_get_serial_sequence('ai_recommendation_item', 'recommendation_item_id')), max(recommendation_item_id)))
                from ai_recommendation_item
                """).getSingleResult();
    }
}
