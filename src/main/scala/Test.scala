import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.json4s.DefaultFormats
import org.json4s.native.Document
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.io.Source.fromInputStream
import scala.sys.process

object Test {

  def main(args: Array[String]): Unit = {
    val BASE_GHQL_URL = "https://api.github.com/graphql"
    val temp="{viewer {email login url}}"
    implicit val formats = DefaultFormats

    val client = HttpClientBuilder.create().build()
    val httpUriRequest = new HttpPost(BASE_GHQL_URL)
    val token = sys.env("TOKEN")
    httpUriRequest.addHeader("Authorization", "bearer "+token)
    httpUriRequest.addHeader("Accept", "application/json")
    val gqlReq = new StringEntity("{\"query\":\"" + temp + "\"}" )
    httpUriRequest.setEntity(gqlReq)

    val response = client.execute(httpUriRequest)
    System.out.println("Response:" + response)
    response.getEntity match {
      case null => System.out.println("Response entity is null")
      case x if x != null => {
        val respJson = fromInputStream(x.getContent).getLines.mkString
        System.out.println(respJson)
        //val viewer = parse(respJson).extract[Data]
        //System.out.println(viewer)
        //System.out.println(write(viewer))
      }
    }
  }
}
