package blockchain2.consensus.HoneyBadger.models;

import blockchain2.primitives.Transaction;
import lombok.Builder;

import java.util.List;

@Builder
public class Contribution {
    private final String contributionId;
    private final List<Transaction> contribution;
}
