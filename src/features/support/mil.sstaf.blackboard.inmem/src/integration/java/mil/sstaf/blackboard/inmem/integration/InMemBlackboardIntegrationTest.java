package mil.sstaf.blackboard.inmem.integration;

import mil.sstaf.blackboard.api.Blackboard;
import mil.sstaf.core.features.FeatureSpecification;
import mil.sstaftest.util.BaseFeatureIntegrationTest;

public class InMemBlackboardIntegrationTest extends BaseFeatureIntegrationTest<Blackboard> {
    /**
     * Constructor
     */
    protected InMemBlackboardIntegrationTest() {
        super(Blackboard.class, getSpec());
    }

    private static FeatureSpecification getSpec() {
        return FeatureSpecification.builder()
                .withFeatureClass(Blackboard.class)
                .featureName("Blackboard")
                .majorVersion(1)
                .minorVersion(0)
                .requireExact(true)
                .build();
    }
}

