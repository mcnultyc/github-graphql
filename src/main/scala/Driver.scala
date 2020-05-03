
import java.io.{ByteArrayOutputStream, PrintStream, OutputStream}

import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}

import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.jackson.JsonMethods.{parse, _}



object Driver {

  val c = ConfigFactory.load()
  val conf = c.getConfig("GQL")
  val logger = LoggerFactory.getLogger(Driver.getClass)

  def main(args: Array[String]): Unit = {

    val TOKEN = conf.getString("AUTHKEY")
    val ACCEPT = conf.getString("ACCEPT")
    val APP_JSON = conf.getString("APPJSON")

    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build

    /*
    val query: QueryCommand = QueryBuilder()
      .withRepoOwner("linux", "torvalds")
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCommits(List(CommitInfo.AUTHOR))
      .withIssues(List(IssueInfo.AUTHOR))
      .withLanguages(List(LanguageInfo.NAME))
      .build

     */
  /*
    val query: QueryCommand = QueryBuilder()
      .withRepo("Phone-List-App")
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCommits(List(CommitInfo.AUTHOR))
      .withIssues(List(IssueInfo.AUTHOR))
      .withLanguages(List(LanguageInfo.NAME))
      .build

   */

    val allIssues = IssueInfo.values.toList
    val allCommits = CommitInfo.values.toList
    val allUsers = UserInfo.values.toList
    val allLangs = LanguageInfo.values.toList
    val allRepo = RepoInfo.values.toList

    try {

      val query: QueryCommand = QueryBuilder()
        .withRepos(allRepo)
        .withAuth(github)
        .withStarGazers(allUsers)
        .withCollaborators(allUsers)
        .withCommits(allCommits)
        .withIssues(allIssues)
        .withLanguages(allLangs)
        .build
    }
    catch{
      case e: GitHubConnectionException => {
        System.err.println("Status: " + e.status)
        System.err.println("Message: " + e.message)
        e.printStackTrace()

      }
    }

  }
}