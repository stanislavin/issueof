package org.stanislavin.issueof.jira.jql;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Scanned
public class IssuesFromEpicsByQueryFunction extends BaseByQueryFunction {

    public IssuesFromEpicsByQueryFunction(@ComponentImport SearchService searchService, @ComponentImport JqlQueryParser jqlQueryParser) {
        super(searchService, jqlQueryParser, "issuesFromEpicsByQuery");
    }

    @Nonnull
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause) {
        final String query = operand.getArgs().get(0);
        final ApplicationUser user = queryCreationContext.getApplicationUser();
        return getEpicsByQuery(user, query)
                .stream()
                .map(issue -> getIssuesOfEpic(user, issue.getKey()))
                .flatMap(List::stream)
                .map(issue -> new QueryLiteral(operand, issue.getId()))
                .collect(Collectors.toList());
    }

    private List<Issue> getEpicsByQuery(ApplicationUser user, String query) {
        return getIssuesByQuery(user, query)
                .stream()
                .filter(issue -> "epic".equalsIgnoreCase(Objects.requireNonNull(issue.getIssueType()).getName()))
                .collect(Collectors.toList());
    }

    private List<Issue> getIssuesOfEpic(ApplicationUser user, String epicKey) {
        return getIssuesByQuery(user, "'Epic Link' = " + epicKey);
    }
}
