
import com.typesafe.config.ConfigFactory

import org.slf4j.{Logger, LoggerFactory}

object Driver {

  val logger = LoggerFactory.getLogger(Driver.getClass)

  def main(args: Array[String]): Unit = {

    val TOKEN = sys.env("TOKEN")
    val ACCEPT = "Accept"
    val APP_JSON = "application/json"

    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build

    val query: QueryCommand = QueryBuilder()
      .withRepoOwner("shell", "sarthak77")
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCommits(List(CommitInfo.AUTHOR))
      .withIssues(List(IssueInfo.AUTHOR))
      .withLanguages(List(LanguageInfo.NAME))
      .build
  }
}