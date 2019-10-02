package org.stanislavin.issueof.jira.jql;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseByQueryFunction extends BaseJqlFunction {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final SearchService searchService;
    protected final JqlQueryParser queryParser;

    BaseByQueryFunction(SearchService searchService, JqlQueryParser queryParser, String functionName) {
        this(searchService, queryParser, functionName, 1);
    }

    BaseByQueryFunction(SearchService searchService, JqlQueryParser queryParser, String functionName, int minimumNumberOfExpectedArguments) {
        super(JiraDataTypes.ISSUE, minimumNumberOfExpectedArguments, functionName, true);
        this.searchService = searchService;
        this.queryParser = queryParser;
    }

    @Nonnull
    public MessageSet validate(ApplicationUser searcher, @Nonnull FunctionOperand operand, @Nonnull TerminalClause terminalClause) {
        MessageSet messageSet = super.validate(searcher, operand, terminalClause);
        final String query = operand.getArgs().get(0);
        try {
            queryParser.parseQuery(query);
        } catch (JqlParseException e) {
            messageSet.addErrorMessage("Cannot parse query: " + query);
        }
        return messageSet;
    }

    List<Issue> getIssuesByQuery(ApplicationUser user, String queryString) {
        final Query query;
        try {
            query = queryParser.parseQuery(queryString);
        } catch (JqlParseException e) {
            log.error("Cannot parse query " + queryString);
            return new ArrayList<>();
        }
        return getIssuesByQuery(user, query);
    }

    private List<Issue> getIssuesByQuery(ApplicationUser user, Query query) {
        try {
            SearchResults results = searchService.searchOverrideSecurity(user, query, PagerFilter.getUnlimitedFilter());
            return getIssuesFromSearchResultsWithReflection(results);
        } catch (SearchException e) {
            log.error("Cannot resolve epics by query: " + query.getQueryString(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Below is a workaround due to jira SDK versions incompatibility
     */
    private List<Issue> getIssuesFromSearchResultsWithReflection(SearchResults searchResults) {
        List<Issue> issues;
        Method getMethod = null;
        try {
            getMethod = SearchResults.class.getMethod("getIssues");
        } catch (NoSuchMethodException e) {
            try {
                log.info("SearchResults.getIssues does not exist - trying to use getResults!");
                //noinspection JavaReflectionMemberAccess
                getMethod = SearchResults.class.getMethod("getResults");
            } catch (NoSuchMethodException e2) {
                log.error("SearchResults.getResults does not exist!");
            }
        }

        if (getMethod == null) {
            log.error("ERROR NO METHOD TO GET ISSUES !");
            throw new RuntimeException("ICT: SearchResults Service from JIRA NOT AVAILABLE (getIssue / getResults)");
        }

        try {
            //noinspection unchecked
            issues = (List<Issue>) getMethod.invoke(searchResults);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Cannot invoke method via reflection", e);
            throw new RuntimeException(e);
        }
        return issues;
    }
}
