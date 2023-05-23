package mil.devcom_dac.equipment.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.state.LabeledState;
import mil.sstaf.core.state.StateProperty;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Jacksonized
public class GunMetric extends LabeledState {
    private final int numberShot;

    @StateProperty(headerLabel = "Shots Fired")
    public int getNumberShot() {
        return numberShot;
    }
}
