
import java.io.{ByteArrayOutputStream, PrintStream, OutputStream}

import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}

// Fields for repository info, used for filtering
sealed trait Donut[A]{
  def name: String
  def pred: (A) => Boolean
}

// Case class with predicate for 'commits'
case class ChocolateDonut[A](pred:(A) => Boolean, name:String="choc")extends Donut[A]

object Driver {

  val c = ConfigFactory.load()
  val conf = c.getConfig("GQL")
  val logger = LoggerFactory.getLogger(Driver.getClass)

  def test[A](d: Donut[A], v: A): Unit ={
    if(d.pred(v)){
      println("IT WORKED WTF")
    }
  }

  def main(args: Array[String]): Unit = {

    val choc = ChocolateDonut((x:Int)=> x == 2)
    val choc2 = ChocolateDonut((x:String)=> x == "hello")
    test(choc, 2)
    test(choc2, "hello")


    return
    val c = Commit(CommitInfo.AUTHOR, (x: Int) => x == 3)




    val m: Any = List[Map[String, Any]]()
    val i: Any = 3
    val s: Any = "hello"

    val lst: List[Any] = List[Any](m, i, s)

    lst.foreach(t =>{
      t match{
        case _: List[Map[String, Any]] => { // Check for a nodes field
          println("list")
        }
        case _: Int => {
          println("int")
        }
        case _: String =>{
          println("string")
        }
      }
    })



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

    val query: QueryCommand = QueryBuilder()
      .withRepos()
      .withAuth(github)
      .withStarGazers(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCollaborators(List(UserInfo.NAME, UserInfo.EMAIL))
      .withCommits(List(CommitInfo.AUTHOR))
      .withIssues(List(IssueInfo.AUTHOR))
      .withLanguages(List(LanguageInfo.NAME))
      .build


    query.filter(c)

/*
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

      query.filter(CommitInfo.AUTHOR, (x: String) => {
        if(x == "mcnultyc"){
          true
        }
        else{
          false
        }
      })

    }
    catch{
      case e: GitHubConnectionException => {
        System.err.println("Status: " + e.status)
        System.err.println("Message: " + e.message)
        e.printStackTrace()

      }
    }


 */

  }

}