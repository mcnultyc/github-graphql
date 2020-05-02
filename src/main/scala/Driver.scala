
import java.io.{ByteArrayOutputStream, PrintStream}

import com.sun.org.slf4j.internal.LoggerFactory
import com.typesafe.config.ConfigFactory

import org.slf4j.{Logger, LoggerFactory}

object Driver {

  val c = ConfigFactory.load()
  val conf = c.getConfig("GQL")
  //val logger = LoggerFactory.getLogger(Driver.getClass)

  def main(args: Array[String]): Unit = {

    val TOKEN = conf.getString("AUTHKEY")
    val ACCEPT = conf.getString("ACCEPT")
    val APP_JSON = conf.getString("APPJSON")

    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build

    val query: QueryCommand = QueryBuilder()
      .withRepoOwner("linux", "torvalds")
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCommits(List(CommitInfo.AUTHOR))
      .withIssues(List(IssueInfo.AUTHOR))
      .withLanguages(List(LanguageInfo.NAME))
      .build
  }
}