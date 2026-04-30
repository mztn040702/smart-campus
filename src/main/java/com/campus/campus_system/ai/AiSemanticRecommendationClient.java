package com.campus.campus_system.ai;

import java.util.List;

public interface AiSemanticRecommendationClient {
    List<SemanticScoreResult> scoreItems(SemanticScoreRequest request);
}
