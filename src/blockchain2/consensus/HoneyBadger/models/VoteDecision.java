package blockchain2.consensus.HoneyBadger.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VoteDecision {
    private final boolean quorumAchieved;
    private final boolean finalDecision;
}
