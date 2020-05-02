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

    val console = new ByteArrayOutputStream() //new console for System.out to be sent to
    System.setOut(new PrintStream(console))

    val query: QueryCommand = QueryBuilder()
      .withRepoOwner("shell", "sarthak77", List())
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCommits(List(CommitInfo.AUTHOR))
      .withIssues(List(IssueInfo.AUTHOR))
      .withLanguages(List(LanguageInfo.NAME))
      .build

    assertFalse(console.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}"))

    //Now testing with an archived repo + minimal query options
    val query2: QueryCommand = QueryBuilder()
      .withRepoOwner("practice", "rtonki2", List())
      .withAuth(github)
      .build

    assertFalse(console.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}"))

    //different settings, different repo
    val query3: QueryCommand = QueryBuilder()
      .withRepoOwner("Python", "TheAlgorithms", List())
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .withLanguages(List(LanguageInfo.NAME))
      .build

    assertFalse(console.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}"))

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

    assertTrue(console.toString().contains("{\"message\":\"Problems parsing JSON\",\"documentation_url\":\"https://developer.github.com/v4\"}"))
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

    val console1 = new ByteArrayOutputStream() //new console for System.out to be sent to
    val old = System.out
    System.setOut(new PrintStream(console1))

    val query: QueryCommand = QueryBuilder()
      .withRepoOwner("linux", "torvalds", List())
      .withAuth(github)
      .build

    //strip console output down to important values
    if(console1.toString().contains("Repo Info"))
    {
      val trimmed = console1.toString().split("\n").apply(4)

      val name = trimmed.split(":").apply(1)
      assertEquals(name, " linux, Created")

      val desc = trimmed.split(":").apply(5).trim()
      assertEquals(desc, "Linux kernel source tree")
    }
    else
    {
      fail = true;
    }
    System.out.flush()
    System.setOut(old)

    val console2 = new ByteArrayOutputStream() //new console for System.out to be sent to
    System.setOut(new PrintStream(console2))

    val query2: QueryCommand = QueryBuilder()
      .withRepoOwner("linux", "torvalds", List())
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .build

    //strip console output down to important values
    if(console2.toString().contains("Repo Info"))
    {
      val trimmed = console1.toString().split("\n").apply(5)

      val count = trimmed.split(":").apply(1)
      assertEquals(count, " 3213, Nodes")

    }
    else
    {
      fail = true;
    }
    System.out.flush()
    System.setOut(old)

    val console3 = new ByteArrayOutputStream() //new console for System.out to be sent to
    System.setOut(new PrintStream(console3))

    val query3: QueryCommand = QueryBuilder()
      .withRepoOwner("linux", "torvalds", List())
      .withAuth(github)
      .withCommits(List(CommitInfo.AUTHOR))
      .build

    //strip console output down to important values
    if(console3.toString().contains("Repo Info"))
    {
      val trimmed = console1.toString().split("\n").apply(5)

      val count = trimmed.split(":").apply(1)
      assertEquals(count, " 916348, Authors")

    }
    else
    {
      fail = true;
    }
    System.out.flush()
    System.setOut(old)

    val console4 = new ByteArrayOutputStream() //new console for System.out to be sent to
    System.setOut(new PrintStream(console4))

    val query4: QueryCommand = QueryBuilder()
      .withRepoOwner("linux", "torvalds", List())
      .withAuth(github)
      .withIssues(List(IssueInfo.AUTHOR))
      .build

    //strip console output down to important values
    if(console4.toString().contains("Repo Info"))
    {
      val trimmed = console1.toString().split("\n").apply(5)

      val count = trimmed.split(":").apply(1)
      assertEquals(count, "0")
    }
    else
    {
      fail = true;
    }
    System.out.flush()
    System.setOut(old)

    val console5 = new ByteArrayOutputStream() //new console for System.out to be sent to
    System.setOut(new PrintStream(console5))

    val query5: QueryCommand = QueryBuilder()
      .withRepoOwner("linux", "torvalds", List())
      .withAuth(github)
      .withLanguages(List(LanguageInfo.NAME))
      .build

    //strip console output down to important values
    if(console5.toString().contains("Repo Info"))
    {
      val trimmed = console1.toString().split("\n").apply(5)

      val count = trimmed.split(":").apply(1)
      assertEquals(count, " 20, Type of Languages")
    }
    else
    {
      fail = true;
    }

    //check that none of the previous queries failed
    assertEquals(false, fail)
  }
}
