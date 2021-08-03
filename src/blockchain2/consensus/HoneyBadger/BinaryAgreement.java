package blockchain2.consensus.HoneyBadger;

import blockchain2.consensus.HoneyBadger.models.Contribution;
import blockchain2.consensus.HoneyBadger.models.Vote;
import blockchain2.consensus.HoneyBadger.models.VoteDecision;

import java.util.UUID;

public class BinaryAgreement {
    private final String contributionId;
    private final int votesThreshold;
    private boolean voted;
    private int yesVotes;
    private int noVotes;

    public BinaryAgreement(final String contributionId, final int clusterParticipants){
        this.votesThreshold = (clusterParticipants*2)/3;
        this.contributionId = contributionId;
        this.yesVotes = 0;
        this.noVotes = 0;
        this.voted = false;
    }

    public VoteDecision receiveVote(final Vote vote){
        if (vote.getContributionId().equals(contributionId)){
            if (vote.isApproved()) {
                this.yesVotes++;
            } else {
                this.noVotes++;
            }
            if (this.votesThreshold/2 < this.yesVotes && this.yesVotes + this.noVotes > this.votesThreshold) {
                return VoteDecision.builder().quorumAchieved(true).finalDecision(this.yesVotes > this.noVotes).build();
            }
        }
        return VoteDecision.builder().finalDecision(false).quorumAchieved(false).build();
    }

    public void receiveContributionAndEmitVote(final Contribution contribution) {
        if (this.voted) {
            return;
        }

    }
}
