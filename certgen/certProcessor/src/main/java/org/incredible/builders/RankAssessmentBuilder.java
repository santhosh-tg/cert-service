package org.incredible.builders;

import org.incredible.pojos.RankAssessment;

public class RankAssessmentBuilder implements IBuilder<RankAssessment> {

    private RankAssessment rankAssessment = new RankAssessment();

    public RankAssessmentBuilder setMaxValue(float maxValue) {
        rankAssessment.setMaxValue(maxValue);
        return this;
    }

    @Override
    public RankAssessment build() {
        return this.rankAssessment;
    }
}

