package com.accenture.dhruv.healthdekho.recommendation.repository;

import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;

public interface AiRecommendationRequestRepository extends JpaRepository<AiRecommendationRequest, Long> {

    @Modifying
    @Query(value = "lock table ai_recommendation_request in share row exclusive mode", nativeQuery = true)
    void lockForSeeding();

    @Modifying
    @Query(value = """
            insert into ai_recommendation_request (recommendation_request_id, customer_id, insurance_goals,
                preferred_term, preferred_policy_type, calculated_age, model_name, model_version,
                prompt_version, requested_at)
            values (cast(:#{#row['recommendation_request_id']} as bigint), cast(:#{#row['customer_id']} as bigint), :#{#row['insurance_goals']},
                cast(nullif(:#{#row['preferred_term']}, '') as integer), :#{#row['preferred_policy_type']}, cast(:#{#row['calculated_age']}
                as integer), :#{#row['model_name']}, :#{#row['model_version']}, :#{#row['prompt_version']}, cast(:#{#row['requested_at']} as
                timestamptz))
            on conflict (recommendation_request_id) do nothing
            """, nativeQuery = true)
    int insertSeed(@Param("row") Map<String, String> row);

    @Query(value = """
            select setval(pg_get_serial_sequence('ai_recommendation_request', 'recommendation_request_id'),
            greatest(nextval(pg_get_serial_sequence('ai_recommendation_request', 'recommendation_request_id')), max(recommendation_request_id)))
            from ai_recommendation_request
            """, nativeQuery = true)
    Long advanceIdentity();
}
