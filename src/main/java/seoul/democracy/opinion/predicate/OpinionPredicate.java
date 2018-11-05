package seoul.democracy.opinion.predicate;

import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Predicate;
import seoul.democracy.opinion.domain.Opinion;

import static seoul.democracy.opinion.domain.QOpinion.opinion;

public class OpinionPredicate {
    public static Predicate equalIssueIdAndCreatedByIdAndStatus(Long issueId, Long userId, Opinion.Status status) {
        return ExpressionUtils.allOf(
            opinion.issue.id.eq(issueId),
            opinion.createdBy.id.eq(userId),
            opinion.status.eq(status));
    }
}