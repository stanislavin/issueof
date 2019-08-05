# Issue Of (issueOf)
Atlassian Jira plugin which provides additional jql functions with sub-queries
- **issuesFromEpicsByQuery(..jql query..)** - returns list of issues contained inside epics specified by jql query
- **linkedIssuesFromIssuesByQuery(..jql query..)** - returns list of issues linked to issues specified by jql query

## Examples

### issuesFromEpicsByQuery
Issues from epics inside *MYPROJ* project or epics labelled with *myproj*:
**issue in issuesFromEpicsByQuery("project = MYPROJ or labels in (myproj)")**

Pluguin will do the following:
- find issues by jql **project = MYPROJ or labels in (myproj)**
- filter out non-epics (check for *issueType = Epic*)
- find issues for each epic and return list of them

### linkedIssuesFromIssuesByQuery
Issues linked to epics in *MYPROJ* project or to epics labelled with *myproj*:
**issue in issuesFromEpicsByQuery("(project = MYPROJ or labels in (myproj)) and issueType = Epic")**

Pluguin will do the following:
- find issues by jql **(project = MYPROJ or labels in (myproj)) and issueType = Epic**
- find linked issues for each epic and return list of them

## Installation	
*(assuming you have Atlassian SDK installed & configured)*
- go into plugin folder
- run *atlas-package*
- get *./target/issueof-X.Y.Z-SNAPSHOT.jar* and upload it to jira
- use two new jql functions when searching for issues
