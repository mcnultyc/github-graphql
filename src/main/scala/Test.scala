import org.json4s._
import org.json4s.jackson.JsonMethods._
import QueryInfo.Empty
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.json4s.native.JsonMethods

import scala.io.Source.fromInputStream

object Test {

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
