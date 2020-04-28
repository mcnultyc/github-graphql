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
      .withRepos()
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCommits(List(CommitInfo.AUTHOR))
      .withIssues(List(IssueInfo.AUTHOR))
      .withLanguages(List(LanguageInfo.NAME))
      .build


    /*
    val BASE_GHQL_URL = "https://api.github.com/graphql"
    val temp: String = query.getQuery()
    implicit val formats = DefaultFormats
    val client = HttpClientBuilder.create().build()
    val httpUriRequest = new HttpPost(BASE_GHQL_URL)
    val token = sys.env("TOKEN")
    httpUriRequest.addHeader("Authorization", "bearer "+token)
    httpUriRequest.addHeader("Accept", "application/json")
    //val gqlReq = new StringEntity("{\"query\":\"" + temp + "\", \"variables\":{\"limit\":3}}" )
    val gqlReq = new StringEntity("{\"query\":\"" + temp + "\"}" )
    httpUriRequest.setEntity(gqlReq)
    val response = client.execute(httpUriRequest)
    System.out.println("Response:" + response)
    response.getEntity match {
      case null => System.out.println("Response entity is null")
      case x if x != null => {
        val respJson = fromInputStream(x.getContent).getLines.mkString
        val viewer = JsonMethods.parse(respJson).extract[Map[String,String]]


        val responseArray = respJson.split('{')
        //val responseArray = respJson.split('[')

        //System.out.println(respJson)
        responseArray.foreach(println)
      }
    }
  */
  }
}
