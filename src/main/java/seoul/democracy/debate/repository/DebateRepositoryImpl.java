package seoul.democracy.debate.repository;

import com.mysema.query.SearchResults;
import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import seoul.democracy.debate.domain.Debate;
import seoul.democracy.debate.dto.DebateDto;
import seoul.democracy.issue.dto.IssueFileDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mysema.query.group.GroupBy.groupBy;
import static com.mysema.query.group.GroupBy.list;
import static seoul.democracy.debate.domain.QDebate.debate;
import static seoul.democracy.issue.domain.QCategory.category;
import static seoul.democracy.issue.domain.QIssueFile.issueFile;
import static seoul.democracy.issue.domain.QIssueRelation.issueRelation;
import static seoul.democracy.issue.domain.QIssueStats.issueStats;
import static seoul.democracy.user.dto.UserDto.createdBy;
import static seoul.democracy.user.dto.UserDto.modifiedBy;

public class DebateRepositoryImpl extends QueryDslRepositorySupport implements DebateRepositoryCustom {

    public DebateRepositoryImpl() {
        super(Debate.class);
    }

    private JPQLQuery getQuery(Expression<DebateDto> projection) {
        JPQLQuery query = from(debate);
        if (projection == DebateDto.projection) {
            query.innerJoin(debate.createdBy, createdBy);
            query.innerJoin(debate.modifiedBy, modifiedBy);
            query.innerJoin(debate.category, category);
            query.innerJoin(debate.stats, issueStats);
        } else if (projection == DebateDto.projectionForAdminList || projection == DebateDto.projectionForAdminDetail) {
            query.innerJoin(debate.createdBy, createdBy);
            query.innerJoin(debate.category, category);
            query.innerJoin(debate.stats, issueStats);
        }
        return query;
    }

    @Override
    public Page<DebateDto> findAll(Predicate predicate, Pageable pageable, Expression<DebateDto> projection, boolean withFiles, boolean withRelations) {
        SearchResults<DebateDto> results = getQuerydsl()
                                               .applyPagination(
                                                   pageable,
                                                   getQuery(projection)
                                                       .where(predicate))
                                               .listResults(projection);
        List<Long> debateIds = results.getResults().stream().map(DebateDto::getId).collect(Collectors.toList());
        if (withFiles && debateIds.size() != 0) {
            Map<Long, List<IssueFileDto>> filesMap = from(debate)
                                                         .innerJoin(debate.files, issueFile)
                                                         .where(debate.id.in(debateIds))
                                                         .orderBy(issueFile.seq.asc())
                                                         .transform(groupBy(debate.id).as(list(IssueFileDto.projection)));
            results.getResults().forEach(debateDto -> debateDto.setFiles(filesMap.get(debateDto.getId())));
        }

        if (withRelations && debateIds.size() != 0) {
            Map<Long, List<Long>> relationsMap = from(debate)
                                                     .innerJoin(debate.relations, issueRelation)
                                                     .where(debate.id.in(debateIds))
                                                     .orderBy(issueRelation.seq.asc())
                                                     .transform(groupBy(debate.id).as(list(issueRelation.issueId)));
            results.getResults().forEach(debateDto -> debateDto.setRelations(relationsMap.get(debateDto.getId())));
        }

        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

    @Override
    public DebateDto findOne(Predicate predicate, Expression<DebateDto> projection, boolean withFiles, boolean withRelations) {
        DebateDto debateDto = getQuery(projection)
                                  .where(predicate)
                                  .uniqueResult(projection);

        if (debateDto != null && withFiles) {
            List<IssueFileDto> files = from(debate)
                                           .innerJoin(debate.files, issueFile)
                                           .where(predicate)
                                           .orderBy(issueFile.seq.asc())
                                           .list(IssueFileDto.projection);
            debateDto.setFiles(files);
        }

        if (debateDto != null && withRelations) {
            List<Long> relations = from(debate)
                                       .innerJoin(debate.relations, issueRelation)
                                       .where(predicate)
                                       .orderBy(issueRelation.seq.asc())
                                       .list(issueRelation.issueId);
            debateDto.setRelations(relations);
        }

        return debateDto;
    }
}
