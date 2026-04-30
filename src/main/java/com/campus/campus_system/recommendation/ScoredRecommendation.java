package com.campus.campus_system.recommendation;

public class ScoredRecommendation<T> {
    private final T item;
    private final double semanticScore;
    private final double hotScore;
    private final double freshnessScore;
    private final double finalScore;
    private final boolean fallbackUsed;

    public ScoredRecommendation(T item, double semanticScore, double hotScore,
                                double freshnessScore, double finalScore, boolean fallbackUsed) {
        this.item = item;
        this.semanticScore = semanticScore;
        this.hotScore = hotScore;
        this.freshnessScore = freshnessScore;
        this.finalScore = finalScore;
        this.fallbackUsed = fallbackUsed;
    }

    public T getItem() {
        return item;
    }

    public double getSemanticScore() {
        return semanticScore;
    }

    public double getHotScore() {
        return hotScore;
    }

    public double getFreshnessScore() {
        return freshnessScore;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }
}
