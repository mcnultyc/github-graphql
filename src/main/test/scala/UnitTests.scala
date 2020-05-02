import java.io.{ByteArrayOutputStream, PrintStream}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import com.typesafe.config.ConfigFactory

object UnitTests {

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

    assertFalse(console1.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}"))


    val console2: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console2) {
      //Now testing with an archived repo + minimal query options
      val query2: QueryCommand = QueryBuilder()
        .withRepoOwner("practice", "rtonki2", List())
        .withAuth(github)
        .build
    }
    assertFalse(console2.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}"))

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
    assertFalse(console3.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}"))

    val console4: ByteArrayOutputStream = new ByteArrayOutputStream
    Console.withOut(console4) {
      //Make sure an improper query does cause error we're testing for
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
    assertTrue(console4.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}"))
  }

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
      val trimmed = console1.toString().split("\n").apply(1)

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
      val trimmed = console2.toString().split("\n").apply(2)

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
      val trimmed = console3.toString().split("\n").apply(2)

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
      val trimmed = console4.toString().split("\n").apply(2)

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
      val trimmed = console5.toString().split("\n").apply(2)

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
}
