package com.accenture.dhruv.healthdekho.recommendation.repository;

import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface AiRecommendationItemRepository extends JpaRepository<AiRecommendationItem, Long> {

    @Query("""
            select item from AiRecommendationItem item
            where item.id = :itemId and item.recommendationRequest.customer.id = :customerId
              and item.product.id = :productId
            """)
    Optional<AiRecommendationItem> findForRegistration(@Param("itemId") long itemId,
                                                        @Param("customerId") long customerId,
                                                        @Param("productId") long productId);

    @Modifying
    @Query(value = "lock table ai_recommendation_item in share row exclusive mode", nativeQuery = true)
    void lockForSeeding();

    @Modifying
    @Query(value = """
            insert into ai_recommendation_item (recommendation_item_id, recommendation_request_id, product_id,
                rank_order, recommendation_reason, suitability, selected_for_registration)
            values (cast(:#{#row['recommendation_item_id']} as bigint), cast(:#{#row['recommendation_request_id']} as bigint),
                cast(:#{#row['product_id']} as bigint), cast(:#{#row['rank_order']} as integer), :#{#row['recommendation_reason']},
                :#{#row['suitability']}, cast(:#{#row['selected_for_registration']} as boolean))
            on conflict (recommendation_item_id) do nothing
            """, nativeQuery = true)
    int insertSeed(@Param("row") Map<String, String> row);

    @Query(value = """
            select setval(pg_get_serial_sequence('ai_recommendation_item', 'recommendation_item_id'),
            greatest(nextval(pg_get_serial_sequence('ai_recommendation_item', 'recommendation_item_id')), max(recommendation_item_id)))
            from ai_recommendation_item
            """, nativeQuery = true)
    Long advanceIdentity();
}
