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

Our unit tests have been implemented by capturing a stream of output into a 
variable which allows us to compare the console output from a query and ensure
it contains a certain key string that is expected to print if the code is 
working properly. This general approach is deployed in each of our tests 
with slight variations.

**Test 1: testCreateQuery()**
    This tests that our QueryBuilder is successfully building executable 
    query strings based on the options selected by the user, and that they
    meet the format we've defined so that they should execute properly.

**Test 2: testReturnNoError()**
    This tests that the query strings we generated and tested in the previous test
    execute and successfully return with data. We look for errors and blank
    return information, as well as check that a blank return is produced when it
    should be.
    
**Test 3: testParsing()**
    This tests how the information retrieved from our query is parsed, stored, 
    and displayed for the user. It checks that the console output contains 
    "Repo Info" because that indicates the data has been retrieved successfully,
    and then checks the rest of the console output for key string indicating the
    successful parse of specific data elements.

**Test 4: testFilter()**
    This tests the ability of our filter function to filter out repositories 
    from a query based on a specific attribute and predicate on that attribute.
    For example, we check that linux repo which has around 900k commits shows up
    in our filtered output when we filter by CommitsInfo.TOTAL_COUNT > 1000, and
    doesn't show up when we filter CommitsInfo.TOTAL_COUNT > 2000000

**Test 5: testEmptyRepo()**
    This is a boundary case test that checks various aspects of our program 
    functionality against a query that returns a completely empty repo. This turned
    up many bugs that caused nullPointer and other Exceptions.
    

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