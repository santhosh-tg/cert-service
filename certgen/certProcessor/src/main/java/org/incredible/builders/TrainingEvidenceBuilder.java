package org.incredible.builders;

import org.incredible.pojos.Duration;
import org.incredible.pojos.TrainingEvidence;

public class TrainingEvidenceBuilder implements IBuilder<TrainingEvidence> {

    private TrainingEvidence trainingEvidence ;

    public TrainingEvidenceBuilder(String context) {
        trainingEvidence = new TrainingEvidence(context);
    }

    public TrainingEvidenceBuilder setSubject(String subject) {
        trainingEvidence.setSubject(subject);
        return this;
    }


    public TrainingEvidenceBuilder setTrainedBy(String trainedBy) {
        trainingEvidence.setTrainedBy(trainedBy);
        return this;
    }


    public TrainingEvidenceBuilder setDuration(Duration duration) {
        trainingEvidence.setDuration(duration);
        return this;
    }


    public TrainingEvidenceBuilder setSession(String session) {
        trainingEvidence.setSession(session);
        return this;
    }

    @Override
    public TrainingEvidence build() {
        return this.trainingEvidence;
    }
}
