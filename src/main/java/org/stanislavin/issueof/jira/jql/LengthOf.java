package org.stanislavin.issueof.jira.jql;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Scanned
public class LengthOf extends BaseByQueryFunction {

    private final CustomFieldManager customFieldManager;

    public LengthOf(@ComponentImport SearchService searchService, @ComponentImport JqlQueryParser jqlQueryParser, @ComponentImport CustomFieldManager customFieldManager) {
        super(searchService, jqlQueryParser, "lengthOf", 4);
        this.customFieldManager = customFieldManager;
    }

    @Nonnull
    @Override
    public MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        MessageSet messageSet = super.validate(applicationUser, functionOperand, terminalClause);
        List<String> args = functionOperand.getArgs();
        validateIssueField(args.get(1)).ifPresent(messageSet::addErrorMessage);
        validateComparisonOperator(args.get(2)).ifPresent(messageSet::addErrorMessage);
        validateLength(args.get(3)).ifPresent(messageSet::addErrorMessage);
        return messageSet;
    }

    @Nonnull
    @Override
    public List<QueryLiteral> getValues(@Nonnull QueryCreationContext queryCreationContext, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        final String query = functionOperand.getArgs().get(0);
        final String fieldName = functionOperand.getArgs().get(1);
        final String operator = functionOperand.getArgs().get(2);
        final long length = Long.parseLong(functionOperand.getArgs().get(3));
        return getIssuesByQuery(queryCreationContext.getApplicationUser(), query)
                .stream()
                .filter(issue -> conformsToCondition(issue, fieldName, operator, length))
                .map(issue -> new QueryLiteral(functionOperand, issue.getId()))
                .collect(Collectors.toList());
    }

    private static Optional<String> validateComparisonOperator(String operator) {
        if (Arrays.asList(">", ">=", "<", "<=", "=", "!=").contains(operator)) {
            return Optional.empty();
        }
        return Optional.of("Unsupported operator: " + operator);
    }

    private static Optional<String> validateLength(String length) {
        try {
            Long.valueOf(length);
        } catch (NumberFormatException e) {
            return Optional.of(e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<String> validateIssueField(String fieldName) {
        Object field = null;
        try {
            field = Issue.class.getField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (field == null) {
            Collection<CustomField> fields = customFieldManager.getCustomFieldObjectsByName(fieldName);
            if (fields == null || fields.isEmpty()) {
                return Optional.of("Seems that field '" + fieldName + "' does not exist");
            }
        }
        return Optional.empty();
    }

    private boolean conformsToCondition(Issue issue, String fieldName, String operator, long length) {
        return getIssueFieldLength(issue, fieldName)
                .map(l -> compareLengths(l, operator, length))
                .orElse(false);
    }

    private Optional<Long> getIssueFieldLength(Issue issue, String fieldName) {
        Long fieldLength = getIssueFieldLengthWithReflection(issue, fieldName);
        if (fieldLength == null) {
            fieldLength = getIssueCustomFieldLength(issue, fieldName);
        }
        return Optional.ofNullable(fieldLength);
    }

    private static Long getIssueFieldLengthWithReflection(Issue issue, String fieldName) {
        try {
            Field field = Issue.class.getField(fieldName);
            return (long) ((Collection<?>) field.get(issue)).size();
        } catch (Exception e) {
            return null;
        }
    }

    private Long getIssueCustomFieldLength(Issue issue, String fieldName) {
        try {
            CustomField customField = customFieldManager.getCustomFieldObjectsByName(fieldName).iterator().next();
            Object customFieldValue = customField.getValue(issue);
            return (long) ((Collection<?>) customFieldValue).size();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean compareLengths(long length, String operator, long desiredLength) {
        switch (operator) {
            case ">":
                return length > desiredLength;
            case ">=":
                return length >= desiredLength;
            case "<":
                return length < desiredLength;
            case "<=":
                return length <= desiredLength;
            case "=":
                return length == desiredLength;
            case "!=":
                return length != desiredLength;
        }
        return false;
    }
}
