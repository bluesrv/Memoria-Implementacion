package blockchain2.consensus.HoneyBadger.models;

import lombok.Builder;
import lombok.Getter;

import java.security.PublicKey;

@Builder
@Getter
public class Vote {
    private final PublicKey voterId;
    private final String contributionId;
    private final boolean approved;
}
