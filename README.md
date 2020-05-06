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
We've provided several enums to encapsulate the various graphql fields.
For instance these are all the fields that the user can request when
querying collaborators, and or stargazers.

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

To the build the query the user must provide a [GitHub](src/main/scala/QueryCommand.scala)
object. This object will contain the authorization token used to access
the GitHub API, along with additional headers that the user can provide.
This object will be used to craft each HTTP request executed by the QueryCommand
class. It can be built like this:

```scala
    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build
```


The user is also required to specify one of the following
repository selection methods from the [QueryBuilder](src/main/scala/QueryCommand.scala) class:

```scala
      def withRepo(name: String, info: List[RepoInfo])
    
      def withRepoOwner(name: String, owner: String, info: List[RepoInfo])
    
      def withRepos(info: List[RepoInfo])
```
The first method selects a specific repository from the user's GitHub account. The 
second method selects a specific repository from another user's account.
The last method selects all of the user's repositories. Each method also takes
a list of optional graphql fields that the user can specify. More info on these
fields can be found in the **RepoInfo** enum in [QueryCommand.scala](src/main/scala/QueryCommand.scala).

The complete snippet of code used to construct a QueryCommand object is seen
below, along with all possible methods:

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
The only methods required are the repository selection method and the
GitHub authorization instance. The remaining methods can be used in
any combination. Each method also takes a list of the specific fields
being requested. A working example of all this can be found in the
[Driver](src/main/scala/Driver.scala) class.


### Parsing

### Monadic combinators

We've chosen to implement a **filter** method inside of the [QueryCommand](src/main/scala/QueryCommand.scala)
class. This method allows you to pass a predicate function along with the name
and field of the data you'll be testing. The predicate function should accept
 a single argument of the type of data being tested and should return a 
 boolean. For instance this is how to filter repositories that use Java:

```scala
      query.filter(Languages(LanguageInfo.NAME, (x:String) => x == "Java"))
```

This is how to filter repositories with a specific number of commits:

```scala
      query.filter(Commit(CommitInfo.TOTAL_COUNT, (x:Int) => x > 100 && x <= 1000))
```

