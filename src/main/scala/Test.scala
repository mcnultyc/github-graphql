import org.json4s._
import org.json4s.jackson.JsonMethods._
import QueryInfo.Empty
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

import scala.io.Source.fromInputStream

object Test {

  def main(args: Array[String]): Unit = {


    val query: QueryCommand = new QueryBuilder[QueryInfo.Empty]()
      .withRepos()
      .withLanguages(List(LanguageInfo.NAME, LanguageInfo.COLOR))
      .withStarGazers()
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .build



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
        val viewer = parse(respJson)
        System.out.println(respJson)
      }
    }
  }
}
