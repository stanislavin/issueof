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
import java.util.stream.Collectors;

@Scanned
public class LinkedIssuesFromIssuesByQueryFunction extends BaseByQueryFunction {

    public LinkedIssuesFromIssuesByQueryFunction(@ComponentImport SearchService searchService, @ComponentImport JqlQueryParser queryParser) {
        super(searchService, queryParser, "linkedIssuesFromIssuesByQuery");
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        final String query = functionOperand.getArgs().get(0);
        final ApplicationUser user = queryCreationContext.getApplicationUser();
        return getIssuesByQuery(user, query)
                .stream()
                .map(issue -> getLinkedIssues(user, issue))
                .flatMap(List::stream)
                .map(issue -> new QueryLiteral(functionOperand, issue.getId()))
                .collect(Collectors.toList());
    }

    private List<Issue> getLinkedIssues(ApplicationUser user, Issue issue) {
        return getIssuesByQuery(user, "issue IN linkedissues('" + issue.getKey() + "')");
    }
}
