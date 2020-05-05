
import java.io.{ByteArrayOutputStream, PrintStream, OutputStream}

import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}


object Driver {

  val conf = ConfigFactory.load().getConfig("GQL")
  val logger = LoggerFactory.getLogger(Driver.getClass)


  def printData(repos: List[(String, Map[String, Any])], max: Int = 3): Unit ={

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


      printData(query.filter(Commit(CommitInfo.TOTAL_COUNT, (x) => x == "40")))
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