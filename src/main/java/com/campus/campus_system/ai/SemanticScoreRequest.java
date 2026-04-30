package com.campus.campus_system.ai;

import java.util.List;

public record SemanticScoreRequest(String userProfileText, List<SemanticScoreItem> items) {
}
