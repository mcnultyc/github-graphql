
import java.io.{ByteArrayOutputStream, PrintStream, OutputStream}

import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}

// Fields for repository info, used for filtering
sealed trait Fieldss[T]{
  def pred: (T) => Boolean
  def field: String
  def name: String
}

//abstract class Fields(name:String, field:String, pred:(String)=>Boolean){}

// Case class with predicate for 'commits'
case class Committ[T](info:String, pred:(T) => Boolean, name:String = "commits")
  extends Fieldss[T]{
  override val field = info.toString
}

class Test{

  def filter[T](f: Fieldss[T]): Unit ={
    val i: String = "carlos"
    if(i.isInstanceOf[T]){
      val convert: T = i.asInstanceOf[T]
      if(f.pred(convert)){
        println("STILL WORKS")
      }
    }
  }
}

object Driver {

  val conf = ConfigFactory.load().getConfig("GQL")
  val logger = LoggerFactory.getLogger(Driver.getClass)


  def printData(repos: List[(String, Map[String, Any])], max: Int = 3): Unit ={

    logger.info("printing filtered data")

    println("\nFILTERED DATA: ")
    println("-------------------------------------")
    // Print out limited number of repos and their fields
    repos.slice(0, max).foreach{ case (repo, fieldsMap) => {
      println("Repo -> " + repo)
      fieldsMap.foreach{ case(key, value) =>{
        println(key + " -> " + value)
      }}
      println("-------------------------------------")
    }}
  }


  def main(args: Array[String]): Unit = {

    val TOKEN = conf.getString("AUTHKEY")
    val ACCEPT = conf.getString("ACCEPT")
    val APP_JSON = conf.getString("APPJSON")

    val github: GitHub = GitHubBuilder()
      .withAuth(TOKEN)
      .withHeaders(List((ACCEPT, APP_JSON)))
      .build

    try {

      val query: QueryCommand = QueryBuilder()
        .withRepos()
        .withAuth(github)
        .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
        .withCommits(List(CommitInfo.AUTHOR))
        .withIssues(List(IssueInfo.AUTHOR))
        .withLanguages(List(LanguageInfo.NAME))
        .build

      // Filter repos were the language Java is used
      printData(query.filter(Languages(LanguageInfo.TOTAL_COUNT, (x:Int) => x < 2)))
    }
    catch{
      // Check for github connection exception and graphql errors
      case e: GitHubConnectionException => {
        // Print out status of http response
        System.err.println("Status: " + e.status)
        // Print out graphql error if provided
        System.err.println("Message: " + e.message)
        e.printStackTrace()

      }
    }
  }

}