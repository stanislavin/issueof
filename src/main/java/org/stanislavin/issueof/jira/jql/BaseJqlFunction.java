package org.stanislavin.issueof.jira.jql;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class BaseJqlFunction extends AbstractJqlFunction {

    private JiraDataType jiraDataType;
    private int minimumNumberOfExpectedArguments;
    private String functionName;
    private boolean isList;

    public BaseJqlFunction(JiraDataType jiraDataType, int minimumNumberOfExpectedArguments, String functionName, boolean isList) {
        this.jiraDataType = jiraDataType;
        this.minimumNumberOfExpectedArguments = minimumNumberOfExpectedArguments;
        this.functionName = functionName;
        this.isList = isList;
    }

    @Nonnull
    @Override
    public MessageSet validate(ApplicationUser applicationUser, @Nonnull FunctionOperand functionOperand, @Nonnull TerminalClause terminalClause) {
        return validateNumberOfArgs(functionOperand, minimumNumberOfExpectedArguments);
    }

    @Override
    public int getMinimumNumberOfExpectedArguments() {
        return minimumNumberOfExpectedArguments;
    }

    @Nonnull
    @Override
    public JiraDataType getDataType() {
        return jiraDataType;
    }

    @Nonnull
    @Override
    public String getFunctionName() {
        return Optional.ofNullable(functionName)
                .orElse(super.getFunctionName());
    }

    @Override
    public boolean isList() {
        return isList;
    }
}
