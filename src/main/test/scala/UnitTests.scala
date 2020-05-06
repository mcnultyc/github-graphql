import java.io.{ByteArrayOutputStream, PrintStream}

import Driver.printData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import com.typesafe.config.ConfigFactory

//**NOTE** Because repos are constantly changing, being accessed, viewed etc,
//         the data returned by a query will change as well. If tests are failing,
//         first ensure that the expected values written here are up to date.
object UnitTests {

  //Test the query string building functionality
  //Note*** This test expects config limit = 100
  @Test
  def testCreateQuery(): Unit = {
    val c = ConfigFactory.load()
    val conf = c.getConfig("GQL")

    val TOKEN = conf.getString("AUTHKEY")
    val ACCEPT = conf.getString("ACCEPT")
    val APP_JSON = conf.getString("APPJSON")

    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build

    val query: QueryCommand = QueryBuilder()
      .withRepoOwner("shell", "sarthak77", List())
      .withAuth(github)
      .build

    assertEquals("query{repository(name: \\\"shell\\\", owner: \\\"sarthak77\\\") {createdAt name description }}", query.queryVal)

    val query2: QueryCommand = QueryBuilder()
      .withRepoOwner("Python", "TheAlgorithms", List())
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withLanguages(List(LanguageInfo.NAME))
      .build

    assertEquals("query{repository(name: \\\"Python\\\", owner: \\\"TheAlgorithms\\\") {createdAt name description  languages (first:" + conf.getInt("LIMIT") + "){totalCount  nodes {name} pageInfo{endCursor hasNextPage}} stargazers (first:" + conf.getInt("LIMIT") + "){totalCount  nodes {name email} pageInfo{endCursor hasNextPage}}}}", query2.queryVal);

    val query3: QueryCommand = QueryBuilder()
      .withRepoOwner("practice", "rtonki2", List())
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCommits(List(CommitInfo.AUTHOR))
      .withIssues(List(IssueInfo.AUTHOR))
      .withLanguages(List(LanguageInfo.NAME))
      .build

    assertEquals("query{repository(name: \\\"practice\\\", owner: \\\"rtonki2\\\") {createdAt name description  languages (first:" + conf.getInt("LIMIT") + "){totalCount  nodes {name} pageInfo{endCursor hasNextPage}} stargazers (first:" + conf.getInt("LIMIT") + "){totalCount  nodes {name email} pageInfo{endCursor hasNextPage}}" +
      " commits: defaultBranchRef{target{... on Commit{  history (first:" + conf.getInt("LIMIT") + "){totalCount  nodes {author{name}} pageInfo{endCursor hasNextPage}}}}} issues (first:" + conf.getInt("LIMIT") + "){totalCount  nodes {author{login}} pageInfo{endCursor hasNextPage}}}}", query3.queryVal);
  }

  //test that properly executed querys return without error
  @Test
  def testReturnNoError(): Unit = {

    val c = ConfigFactory.load()
    val conf = c.getConfig("GQL")

    val TOKEN = conf.getString("AUTHKEY")
    val ACCEPT = conf.getString("ACCEPT")
    val APP_JSON = conf.getString("APPJSON")

    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build

    val console1: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console1) {

      val query: QueryCommand = QueryBuilder()
        .withRepoOwner("shell", "sarthak77", List())
        .withAuth(github)
        .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCommits(List(CommitInfo.AUTHOR))
        .withIssues(List(IssueInfo.AUTHOR))
        .withLanguages(List(LanguageInfo.NAME))
        .build
    }

    assertFalse(console1.toString().isBlank)


    val console2: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console2) {
      //Now testing with an archived repo + minimal query options
      val query2: QueryCommand = QueryBuilder()
        .withRepoOwner("practice", "rtonki2", List())
        .withAuth(github)
        .build
    }
    assertFalse(console2.toString().isBlank)

    val console3: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console3) {
      //different settings, different repo
      val query3: QueryCommand = QueryBuilder()
        .withRepoOwner("Python", "TheAlgorithms", List())
        .withAuth(github)
        .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
        .withLanguages(List(LanguageInfo.NAME))
        .build
    }
    assertFalse(console3.toString().isBlank)

    val console4: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console4) {

      var fail = false
      //Make sure an improper query does cause error we're testing for
      try {
        val query4: QueryCommand = QueryBuilder()
          .withRepoOwner("\"shell\"", "sarthak77", List())
          .withAuth(github)
          .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
          .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
          .withCommits(List(CommitInfo.AUTHOR))
          .withIssues(List(IssueInfo.AUTHOR))
          .withLanguages(List(LanguageInfo.NAME))
          .build
      }
      catch {
        case e: GitHubConnectionException => fail = true

      }
      assertEquals(true, fail)
    }

    assertTrue(console4.toString().isBlank())
  }

  //test that output data matches the expected values for a given repository
  //**NOTE** Because repos are constantly changing, being accessed, viewed etc,
  //         the data returned by a query will change as well. If tests are failing,
  //         first ensure that the expected values written here are up to date.
  @Test
  def testParsing(): Unit = {
    val c = ConfigFactory.load()
    val conf = c.getConfig("GQL")

    val TOKEN = conf.getString("AUTHKEY")
    val ACCEPT = conf.getString("ACCEPT")
    val APP_JSON = conf.getString("APPJSON")

    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build

    var fail = false

    val console1: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console1) {

      val query: QueryCommand = QueryBuilder()
        .withRepoOwner("linux", "torvalds", List())
        .withAuth(github)
        .build

    }

    //strip console output down to important values
    if(console1.toString().contains("Repo Info"))
    {
      val trimmed = console1.toString().split("\n").apply(0)

      val name = trimmed.split(":").apply(1)
      assertEquals(name, " linux, Created")

      val desc = trimmed.split(":").apply(5).trim()
      assertEquals(desc, "Linux kernel source tree")
    }
    else
    {
      fail = true;
    }

    val console2: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console2) {

      val query2: QueryCommand = QueryBuilder()
        .withRepoOwner("Chord-Protocol-for-Overlay-Network", "AmrutaB26", List())
        .withAuth(github)
        .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
        .build
    }

    //strip console output down to important values
    if(console2.toString().contains("Repo Info"))
    {
      val trimmed = console2.toString().split("\n").apply(1)

      val count = trimmed.split(":").apply(1)
      assertEquals(" 1, Nodes", count)

    }
    else
    {
      fail = true;
    }

    val console3: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console3) {

      val query3: QueryCommand = QueryBuilder()
        .withRepoOwner("Chord-Protocol-for-Overlay-Network", "AmrutaB26", List())
        .withAuth(github)
        .withCommits(List(CommitInfo.AUTHOR))
        .build
    }

    //strip console output down to important values
    if(console3.toString().contains("Repo Info"))
    {
      val trimmed = console3.toString().split("\n").apply(1)

      val count = trimmed.split(":").apply(1)
      assertEquals(" 19, Authors", count)

    }
    else
    {
      fail = true;
    }

    val console4: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console4) {

      val query4: QueryCommand = QueryBuilder()
        .withRepoOwner("Chord-Protocol-for-Overlay-Network", "AmrutaB26", List())
        .withAuth(github)
        .withIssues(List(IssueInfo.AUTHOR))
        .build
    }

    //strip console output down to important values
    if(console4.toString().contains("Repo Info"))
    {
      val trimmed = console4.toString().split("\n").apply(1)

      val count = trimmed.split(":").apply(1)
      assertEquals(" 0, Nodes", count)
    }
    else
    {
      fail = true;
    }

    val console5: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console5) {

      val query5: QueryCommand = QueryBuilder()
        .withRepoOwner("linux", "torvalds", List())
        .withAuth(github)
        .withLanguages(List(LanguageInfo.NAME))
        .build
    }

    //strip console output down to important values
    if(console5.toString().contains("Repo Info"))
    {
      val trimmed = console5.toString().split("\n").apply(1)

      val count = trimmed.split(":").apply(1)
      assertEquals(" 20, Type of Languages", count)
    }
    else
    {
      fail = true;
    }

    //check that none of the previous queries failed
    assertEquals(false, fail)
  }

  //Test filtering functionality
  @Test
  def testFilter(): Unit = {
    val c = ConfigFactory.load()
    val conf = c.getConfig("GQL")

    val TOKEN = conf.getString("AUTHKEY")
    val ACCEPT = conf.getString("ACCEPT")
    val APP_JSON = conf.getString("APPJSON")

    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build

    var fail = false

    val console1: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console1) {

      val query: QueryCommand = QueryBuilder()
        .withRepoOwner("linux", "torvalds", List())
        .withAuth(github)
        .withCommits(List(CommitInfo.AUTHOR))
        .build

      printData(query.filter(Commit(CommitInfo.TOTAL_COUNT, (x:Int) => x > 1000)))
    }


    if (console1.toString().contains("Repo Info")) {
      //filtered info starts with "Repo -> <name>" if not filtered out
      assertTrue(console1.toString().contains("Repo -> linux"))
    }
    else {
      fail = true;
    }

    val console2: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console2) {

      val query2: QueryCommand = QueryBuilder()
        .withRepoOwner("linux", "torvalds", List())
        .withAuth(github)
        .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCommits(List(CommitInfo.AUTHOR))
        .withIssues(List(IssueInfo.AUTHOR))
        .withLanguages(List(LanguageInfo.NAME))
        .build

      printData(query2.filter(Commit(CommitInfo.TOTAL_COUNT, (x:Int) => x > 2000000)))
    }

    //strip console output down to important values
    if (console2.toString().contains("Repo Info")) {
      assertFalse(console2.toString().contains("Repo -> linux"))
    }
    else {
      fail = true;
    }

    val console3: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console3) {

      val query3: QueryCommand = QueryBuilder()
        .withRepoOwner("linux", "torvalds", List())
        .withAuth(github)
        .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCommits(List(CommitInfo.AUTHOR))
        .withIssues(List(IssueInfo.AUTHOR))
        .withLanguages(List(LanguageInfo.NAME))
        .build

      printData(query3.filter(Languages(LanguageInfo.NAME, (x:String) => x == "C++")))

    }

    //strip console output down to important values
    if (console3.toString().contains("Repo Info")) {
      assertTrue(console3.toString().contains("Repo -> linux"))
    }
    else {
      fail = true;
    }

    val console4: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console4) {

      val query4: QueryCommand = QueryBuilder()
        .withRepoOwner("linux", "torvalds", List())
        .withAuth(github)
        .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCommits(List(CommitInfo.AUTHOR))
        .withIssues(List(IssueInfo.AUTHOR))
        .withLanguages(List(LanguageInfo.NAME))
        .build

      printData(query4.filter(Language(LanguageInfo.NAME, (x:String) => x == "Scala")))

    }

    //strip console output down to important values
    if (console4.toString().contains("Repo Info")) {
      assertFalse(console4.toString().contains("Repo -> linux"))
    }
    else {
      fail = true;
    }
    assertEquals(false, fail)
  }

  //Testing various queries with an empty repository to test robustness of code
  @Test
  def testEmptyRepo(): Unit = {
    val c = ConfigFactory.load()
    val conf = c.getConfig("GQL")

    val TOKEN = conf.getString("AUTHKEY")
    val ACCEPT = conf.getString("ACCEPT")
    val APP_JSON = conf.getString("APPJSON")

    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build

    val console1: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console1) {

      val query: QueryCommand = QueryBuilder()
        .withRepoOwner("practice", "rtonki2", List())
        .withAuth(github)
        .build
    }

    assertFalse(console1.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}")) //error message

    val console2: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console2) {

      val query2: QueryCommand = QueryBuilder()
        .withRepoOwner("practice", "rtonki2", List())
        .withAuth(github)
        .withLanguages(List(LanguageInfo.NAME))
        .build
    }

    assertFalse(console2.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}")) //error message

    //Testing .withLanguage option
    val console3: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console3) {

      val query3: QueryCommand = QueryBuilder()
        .withRepoOwner("practice", "rtonki2", List())
        .withAuth(github)
        .withCommits()
        .build
    }
    assertFalse(console3.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}")) //error

    //testing .withLanguages option
    val console4: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console4) {

      val query4: QueryCommand = QueryBuilder()
        .withRepoOwner("practice", "rtonki2", List())
        .withAuth(github)
        .withLanguages(List(LanguageInfo.NAME))
        .build
    }
    assertFalse(console4.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}")) //error
    //Testing that the results are different
    assert(console3.toString() != console4.toString())

    //Test all options together
    val console5: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console5) {

      val query5: QueryCommand = QueryBuilder()
        .withRepoOwner("practice", "rtonki2", List())
        .withAuth(github)
        .withLanguages(List(LanguageInfo.NAME))
        .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCommits(List(CommitInfo.AUTHOR))
        .withIssues(List(IssueInfo.AUTHOR))
        .build
    }

    assertFalse(console5.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}"))
  }
}
