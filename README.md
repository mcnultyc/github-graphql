# Github GraphQL

Group Members:
Christian Dominguez,
Carlos McNulty,
Riley Tonkin

## Build process

In order to run our program, we assume that Intellij is installed and working in order to import the project.

Our program can be run via two methods:

1. By using the run button in IntellJ.   

2. With SBT by using the commands: `sbt clean`, `sbt compile`, and `sbt run`. 

## Unit tests

In order to run our tests, you must use SBT and the command: `sbt run test`

## Configuration file
The [configuration file](src/main/resources/application.conf) provides a key 
`AUTHKEY` for the user to enter their GitHub authorization token used for 
communicating with the GitHub API. It also provides a key `LIMIT` used for 
the pagination of the GraphQL queries. If the user builds a query and gets
a `GitHubConnectionException` they can reduce the value of the limit in the
configuration file. The highest value for `LIMIT` is 100.


## Design

### Building GraphQL queries

Our query command are built using the builder pattern
inside of [QueryBuilder](src/main/scala/QueryCommand.scala). This
builder class uses **phantom types** to ensure that the required methods
are called before the build method, and it checks for this at compile time.

```scala
      // Optional fields for users
      object UserInfo extends Enumeration{
        type UserInfo = Value
        val BIO     = Value("bio")
        val COMPANY = Value("company")
        val EMAIL   = Value("email")
        val ID      = Value("id")
        val LOGIN   = Value("login")
        val NAME    = Value("name")
      }
```


```scala

      val query: QueryCommand = QueryBuilder()
        .withRepos()
        .withAuth(github)
        .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCommits(List(CommitInfo.AUTHOR))
        .withIssues(List(IssueInfo.AUTHOR))
        .withLanguages(List(LanguageInfo.NAME))
        .build

```


### Parsing

### Monadic combinators

```scala
      // Filter repos were the language Java is used
      query.filter(Languages(LanguageInfo.NAME, (x) => x == "Java"))
```