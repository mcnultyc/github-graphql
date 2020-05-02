
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.jackson.JsonMethods.{parse, _}

import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.entity.{ContentType, StringEntity}

import scala.util.control._
import scala.io.Source.fromInputStream

import com.typesafe.config.ConfigFactory

import org.slf4j.{Logger, LoggerFactory}

//import scala.collection.mutable.Map


// Optional fields for commits
object CommitInfo extends Enumeration{
  type CommitInfo = Value
  val AUTHOR        = Value("author{name}")       // graph-ql field
  val AUTHORED_DATE = Value("authoredDate")
  val MESSAGE       = Value("message")
  val CHANGED_FILES = Value("changedFiles")
  val PUSHED_DATE   = Value("pushedDate")
  val ID            = Value("id")
}


// Optional fields for languages
object LanguageInfo extends Enumeration{
  type LanguageInfo = Value
  val COLOR = Value("color")
  val NAME  = Value("name")
  val ID    = Value("id")
}


// Optional fields for users
object UserInfo extends Enumeration{
  type UserInfo = Value
  val BIO     = Value("bio")
  val COMPANY = Value("company")
  val EMAIL   = Value("email")
  val ID      = Value("id")
  val LOGIN   = Value("login")
  val NAME    = Value("name")
}


// Optional fields for issues
object IssueInfo extends Enumeration{
  type IssueInfo = Value
  val AUTHOR          = Value("author{login}")
  val BODY            = Value("body")
  val CREATED_AT      = Value("createdAt")
  val NUMBER          = Value("number")
  val PUBLISHED_AT    = Value("publishedAt")
  val CLOSED_AT       = Value("closedAt")
  val LAST_EDITED_AT  = Value("lastEditedAt")
}


// Optional fields for repositories
object RepoInfo extends Enumeration{
  type RepoInfo = Value
  val CREATED_AT  = Value("createdAt")
  val DESCRIPTION = Value("description")
  val FORK_COUNT  = Value("forkCount")
  val IS_PRIVATE  = Value("isPrivate")
  val IS_FORK     = Value("isFork")
  val IS_ARCHIVED = Value("isArchived")
  val NAME        = Value("name")
}


import CommitInfo.CommitInfo
import LanguageInfo.LanguageInfo
import UserInfo.UserInfo
import IssueInfo.IssueInfo
import RepoInfo.RepoInfo

sealed trait QueryInfo
object QueryInfo{

  sealed trait Empty extends QueryInfo
  sealed trait Auth  extends QueryInfo  // GitHub authorization token
  sealed trait Repo  extends QueryInfo  // Repository/repositories query

  // Mandatory traits for query
  type MandatoryInfo = Empty with Auth with Repo
}

case class QueryBuilder[I <: QueryInfo](repo: String = "",
                                        owner: String = "",
                                        repoInfo: List[RepoInfo] = null,
                                        commitsInfo: List[CommitInfo] = null,
                                        languageInfo: List[LanguageInfo] = null,
                                        languagesInfo: List[LanguageInfo] = null,
                                        starGazersInfo: List[UserInfo] = null,
                                        collaboratorsInfo: List[UserInfo] = null,
                                        issuesInfo: List[IssueInfo] = null,
                                        authorizer: GitHub = null){

  import QueryInfo._

  // Required methods for query

  def withAuth(auth: GitHub): QueryBuilder[I with Auth] ={
    this.copy(authorizer = auth)
  }

  def withRepo(name: String, info: List[RepoInfo] = List()): QueryBuilder[I with Repo] ={
    this.copy(repo = name, repoInfo = info)
  }

  def withRepoOwner(name: String, owner: String, info: List[RepoInfo] = List()): QueryBuilder[I with Repo] = {
    this.copy(repo = name, owner = owner, repoInfo = info)
  }

  def withRepos(info: List[RepoInfo] = List()): QueryBuilder[I with Repo] ={
    this.copy(repo = "", repoInfo=info)
  }

  // Optional methods for query

  def withLanguage(info: List[LanguageInfo]): QueryBuilder[I] ={
    this.copy(languageInfo = info)
  }

  def withLanguages(info: List[LanguageInfo] = List()): QueryBuilder[I] ={
    this.copy(languagesInfo = info)
  }

  def withCommits(info: List[CommitInfo] = List()): QueryBuilder[I] ={
    this.copy(commitsInfo = info)
  }

  def withStarGazers(info: List[UserInfo] = List()): QueryBuilder[I] ={
    this.copy(starGazersInfo = info)
  }

  def withCollaborators(info: List[UserInfo] = List()): QueryBuilder[I] ={
    this.copy(collaboratorsInfo = info)
  }

  def withIssues(info: List[IssueInfo] = List()): QueryBuilder[I] = {
    this.copy(issuesInfo = info)
  }

  // Builder method
  def build(implicit ev: I =:= MandatoryInfo): QueryCommand = {
    new QueryCommand(repo, owner, repoInfo, commitsInfo, languageInfo,
      languagesInfo, starGazersInfo, collaboratorsInfo, issuesInfo, authorizer)
  }
}


class QueryCommand(repo: String = "",
                   owner: String = "",
                   repoInfo: List[RepoInfo] = null,
                   commitsInfo: List[CommitInfo] = null,
                   languageInfo: List[LanguageInfo] = null,
                   languagesInfo: List[LanguageInfo] = null,
                   starGazersInfo: List[UserInfo] = null,
                   collaboratorsInfo: List[UserInfo] = null,
                   issuesInfo: List[IssueInfo] = null,
                   authorizer: GitHub = null){

  val logger = LoggerFactory.getLogger(this.getClass)

  // Execute the graph-ql query
  execute()


  private def execute(): Unit ={


    val BASE_GHQL_URL = "https://api.github.com/graphql"

    val client = HttpClientBuilder.create().build()
    // Create graph-ql query
    val query = createQuery()
    // Create http entity with graph-ql query
    val entity = new StringEntity(s"""{"query":"$query"}""" , ContentType.APPLICATION_JSON)

    val request = new HttpPost(BASE_GHQL_URL)

    var headers = authorizer.getHeaders()
    // Add authorization header if not already there
    if(!headers.toMap.contains("Authorization")){
      headers = ("Authorization", "bearer "+ authorizer.getToken()) :: headers
    }
    // Set http headers for request
    headers.foreach(x => request.addHeader(x._1, x._2))

    val loop = new Breaks
    loop.breakable{
      while(true){
        logger.info(s"""creating http post with entity: {"query":"$query"}""")
        // Set http entity for request
        request.setEntity(entity)

        logger.info("executing http post request")
        // Execute request
        val response = client.execute(request)

        // Get json from response content
        val json =
          response.getEntity match{
            case null => ""
            case x => {
              fromInputStream(x.getContent).getLines.mkString
            }
          }

        println(json)
        parseResponse(json) // TODO - getting exceptions here
        loop.break()
        // TODO - finish pagination
        /*
        // Parse the graph-ql json
        parseResponse(json)
        val cursors: Map[String, String] = Map()
        if(cursors.size == 0){
          loop.break
        }
         */
      }
    }
  }


  private def parseResponse(response: String): Unit ={

    implicit val formats = DefaultFormats

    val view = parse(response).extract[Map[String, Any]]

    val data = view.get("data").get.asInstanceOf[Map[String, Any]]

    //println(view)

    // Query repository for another user
    if(repo != "" && owner != ""){

      //Getting the repository info
      val repo = data.get("repository").get.asInstanceOf[Map[String, Any]]

      val createdInfo = repo.get("createdAt").get

      val repoName = repo.get("name").get

      val repoDesc = repo.get("description").get

      println("Repo Info -> " + "Name: " + repoName + ", Created: " + createdInfo + ", Desc: " + repoDesc)

      val endCursorMap = Map[String, String]()

      //Getting language info
      val languageInfo = repo.get("languages").get.asInstanceOf[Map[String, Any]]

      val numLanguages = languageInfo.get("totalCount").get.toString.toInt

      val languages = languageInfo.get("nodes").get.asInstanceOf[List[Map[String, Any]]]

      var languageTypes = List[Any]()

      for(element<-languages){
        languageTypes = element.get("name").get :: languageTypes
      }

      val languages_pageInfo = languageInfo.get("pageInfo").get.asInstanceOf[Map[String, Any]]

      if(languages_pageInfo.get("hasNextPage").get == true){
        // endCursorMap.addOne("languages" -> languages_pageInfo.get("endCursor").get.toString)
      }

      println("Language Info -> " + "# of Languages: " + numLanguages + ", Type of Languages: " + languageTypes)

      //print(endCursorMap)

      //Getting stargazers info
      if(repo.get("stargazers").get != null) {
        val stargazersInfo = repo.get("stargazers").get.asInstanceOf[Map[String, Any]]

        val stargazersCount = stargazersInfo.get("totalCount").get

        val stargazers = stargazersInfo.get("nodes").get.asInstanceOf[List[Map[String, Any]]]

        val stargazers_pageInfo = stargazersInfo.get("pageInfo").get.asInstanceOf[Map[String, Any]]

        if (stargazers_pageInfo.get("hasNextPage").get == true) {
          // endCursorMap.addOne("stargazers" -> stargazers_pageInfo.get("endCursor").get.toString)
        }

        println("Stargazers Info -> " + "Count: " + stargazersCount + ", Nodes: " + stargazers)

      }

      //Getting collaborators info
      if(repo.get("collaborators").get != null) {
        val collaboratorsInfo = repo.get("collaborators").get.asInstanceOf[Map[String, Any]]
      }

      //Getting commits info
      if(repo.get("commits").get != null) {
        val commitsInfo = repo.get("commits").get.asInstanceOf[Map[String, Any]]
        val target = commitsInfo.get("target").get.asInstanceOf[Map[String, Any]]
        val history = target.get("history").get.asInstanceOf[Map[String, Any]]

        val commitsCount = history.get("totalCount").get

        val commitNodes = history.get("nodes").get.asInstanceOf[List[Map[String, Any]]]

        var authorList = List[Map[String, Any]]()

        for (element <- commitNodes) {
          val author = element.get("author")
          authorList = author.get.asInstanceOf[Map[String, Any]] :: authorList
        }

        val commits_pageInfo = history.get("pageInfo").get.asInstanceOf[Map[String, Any]]

        if (commits_pageInfo.get("hasNextPage").get == true) {
          // endCursorMap.addOne("commits" -> commits_pageInfo.get("endCursor").get.toString)
        }

        println("Commits Info -> " + "Count: " + commitsCount + ", Authors: " + authorList)

      }

      //Getting issues info
      if(repo.get("issues").get != null){
        val issuesInfo = repo.get("issues").get.asInstanceOf[Map[String, Any]]

        val issuesCount = issuesInfo.get("totalCount").get

        val issuesNodes = issuesInfo.get("nodes").get.asInstanceOf[List[Map[String, Any]]]

        val issues_pageInfo = issuesInfo.get("pageInfo").get.asInstanceOf[Map[String, Any]]

        if (issues_pageInfo.get("hasNextPage").get == true) {
          // endCursorMap.addOne("issues" -> issues_pageInfo.get("endCursor").get.toString)
        }

        println("Issues Info -> " + "Count: " + issuesCount + ", Nodes: " + issuesNodes)

      }

      //Getting errors info
     // if(repo.get("errors").get != null){

      //}



    }//End of repository for another user
    // Query repository for user
    else if(repo != ""){
      val viewer = data.get("viewer").get.asInstanceOf[Map[String, Any]]

      //val authInfo = viewer.get("name").get
      //System.out.println("Auth Info: " + authInfo)

      //Getting the repositories
      val repos = viewer.get("repository").get.asInstanceOf[Map[String, Any]]
      val numRepos = repos.get("totalCount").get.toString.toInt
      System.out.println("# of repos: " + numRepos)

      val nodes = repos.get("nodes").get.asInstanceOf[List[Map[String, Any]]]

      var languagesList = List[Map[String, Any]]()

      for(element<-nodes)
      {
        languagesList = element.get("languages").get.asInstanceOf[Map[String, Any]] :: languagesList

      }

      var numLanguages = List[Int]()

      for(element<-languagesList){
        numLanguages = element.get("totalCount").get.toString.toInt :: numLanguages
      }

      println("Number of Languages used in each repository: " + numLanguages)

      var issuesList = List[Map[String, Any]]()

      for(element <- nodes){
        issuesList = element.get("issues").get.asInstanceOf[Map[String, Any]] :: issuesList
      }

      var numIssues = List[Int]()

      for(element <- issuesList){
        numIssues = element.get("totalCount").get.toString.toInt :: numIssues
      }

      println("Number of Issues in each repository: " + numIssues)

      //Getting endCursor and hasNextPage
      val pageInfo = repos.get("pageInfo").get.asInstanceOf[Map[String, Any]]
      val endCursor = pageInfo.get("endCursor").get.toString
      val hasNextPage = pageInfo.get("hasNextPage").get.toString

      var pageInfo_map = Map[String, String]()

      if(hasNextPage == "true"){
        pageInfo_map += ("repositories" -> endCursor)
      }

      //-------------------------Onto the optional fields------------------------------------------------------------

      //if(repoInfo != null){

      var createdList = List[Any]()

      for(element<-nodes)
      {
        createdList = element.get("createdAt").get :: createdList

      }

      createdList = createdList.reverse

      var descriptionList = List[Any]()

      for(element<-nodes)
      {
        descriptionList = element.get("description").get :: descriptionList

      }

      descriptionList = descriptionList.reverse

      var nameList = List[Any]()

      for(element<-nodes)
      {
        nameList = element.get("name").get :: nameList

      }

      nameList = nameList.reverse


      //languagesList = languagesList.reverse

      for( a <- 0 to numRepos-1){
        val i = a + 1
        println("Repo #" + i + " -> "+ "Name: " + nameList.lift(a).get + ", Created: " + createdList.lift(a).get + ", Description: " + descriptionList.lift(a).get + ", Number of languages: " + numLanguages.lift(a).get + ", Number of issues: " + numIssues.lift(a).get)
      }


      //}//End of repoInfo if
      //---------------------------------------------------------------------------------------------------------------------------------------------------------------
      //if(commitsInfo != null){

      var commitsList = List[Map[String, Any]]()
      for(element<-nodes){
        commitsList = element.get("commits").get.asInstanceOf[Map[String, Any]] :: commitsList
      }

      println(commitsList)
      //}
      //---------------------------------------------------------------------------------------------------------------------------------------------------------------
      //if(starGazersInfo != null) {

      var stargazersList = List[Map[String, Any]]()

      for (element <- nodes) {
        stargazersList = element.get("stargazers").get.asInstanceOf[Map[String, Any]] :: stargazersList
      }

      println(stargazersList)
      //}
      //--------------------------------------------------------------------------------------------------------------------------------------------------------------
      //if(){
      var collaboratorsList = List[Map[String, Any]]()

      for(element<-nodes){
        collaboratorsList = element.get("collaborators").get.asInstanceOf[Map[String, Any]] :: collaboratorsList
      }

      println(collaboratorsList)

      //}
    }//End of single user repository
    // Query all repositories for user
    else{
      val viewer = data.get("viewer").get.asInstanceOf[Map[String, Any]]

      //val authInfo = viewer.get("name").get
      //System.out.println("Auth Info: " + authInfo)

      //Getting the repositories
      val repos = viewer.get("repositories").get.asInstanceOf[Map[String, Any]]
      val numRepos = repos.get("totalCount").get.toString.toInt
      System.out.println("# of repos: " + numRepos)

      val nodes = repos.get("nodes").get.asInstanceOf[List[Map[String, Any]]]

      var languagesList = List[Map[String, Any]]()

      for(element<-nodes)
      {
        languagesList = element.get("languages").get.asInstanceOf[Map[String, Any]] :: languagesList

      }

      var numLanguages = List[Int]()

      for(element<-languagesList){
        numLanguages = element.get("totalCount").get.toString.toInt :: numLanguages
      }

      println("Number of Languages used in each repository: " + numLanguages)

      var issuesList = List[Map[String, Any]]()

      for(element <- nodes){
        issuesList = element.get("issues").get.asInstanceOf[Map[String, Any]] :: issuesList
      }

      var numIssues = List[Int]()

      for(element <- issuesList){
        numIssues = element.get("totalCount").get.toString.toInt :: numIssues
      }

      println("Number of Issues in each repository: " + numIssues)

      //Getting endCursor and hasNextPage
      val pageInfo = repos.get("pageInfo").get.asInstanceOf[Map[String, Any]]
      val endCursor = pageInfo.get("endCursor").get.toString
      val hasNextPage = pageInfo.get("hasNextPage").get.toString

      var pageInfo_map = Map[String, String]()

      if(hasNextPage == "true"){
        pageInfo_map += ("repositories" -> endCursor)
      }

      //-------------------------Onto the optional fields------------------------------------------------------------

      //if(repoInfo != null){

      var createdList = List[Any]()

      for(element<-nodes)
      {
        createdList = element.get("createdAt").get :: createdList

      }

      createdList = createdList.reverse

      var descriptionList = List[Any]()

      for(element<-nodes)
      {
        descriptionList = element.get("description").get :: descriptionList

      }

      descriptionList = descriptionList.reverse

      var nameList = List[Any]()

      for(element<-nodes)
      {
        nameList = element.get("name").get :: nameList

      }

      nameList = nameList.reverse


      //languagesList = languagesList.reverse

      for( a <- 0 to numRepos-1){
        val i = a + 1
        println("Repo #" + i + " -> "+ "Name: " + nameList.lift(a).get + ", Created: " + createdList.lift(a).get + ", Description: " + descriptionList.lift(a).get + ", Number of languages: " + numLanguages.lift(a).get + ", Number of issues: " + numIssues.lift(a).get)
      }


      //}//End of repoInfo if
      //---------------------------------------------------------------------------------------------------------------------------------------------------------------
      //if(commitsInfo != null){

      var commitsList = List[Map[String, Any]]()
      for(element<-nodes){
        commitsList = element.get("commits").get.asInstanceOf[Map[String, Any]] :: commitsList
      }

      println(commitsList)
      //}
      //---------------------------------------------------------------------------------------------------------------------------------------------------------------
      //if(starGazersInfo != null) {

      var stargazersList = List[Map[String, Any]]()

      for (element <- nodes) {
        stargazersList = element.get("stargazers").get.asInstanceOf[Map[String, Any]] :: stargazersList
      }

      println(stargazersList)
      //}
      //--------------------------------------------------------------------------------------------------------------------------------------------------------------
      //if(){
      var collaboratorsList = List[Map[String, Any]]()

      for(element<-nodes){
        collaboratorsList = element.get("collaborators").get.asInstanceOf[Map[String, Any]] :: collaboratorsList
      }

      println(collaboratorsList)

      //}
    }//End of all repositories

  }//End of parseResponse()



  private def createQuery(cursors: Map[String, String] = Map(), paginate: Boolean = false): String ={

    def fields(info: List[Any]): String =
      s"{${info.map(x => x.toString).mkString(" ").trim}}"

    def nodes(fields: String): String =
      if(fields.trim == "{}") "" else s" nodes $fields pageInfo{endCursor hasNextPage}"

    def args(after: String = ""): String =
      if(after.trim == "") s"(first:100)" else s"(first:100, after: $after)"

    logger.info(s"creating graph-ql query: # cursors: ${cursors.size}, pagination = $paginate")

    // Default simple fields for repositories
    val defaultFields = "{createdAt name description}"

    // Complex fields used for a repository query
    var complexFields = ""

    // Check if user requested primary language info
    if(languageInfo != null && languagesInfo.length != 0){
      // Add json for primary language
      complexFields += s" primaryLanguage ${fields(languageInfo)}"
    }
    if(languagesInfo != null){
      // Check if query is for pagination
      if(!paginate || (paginate && cursors.get("languages") != None)){
        // Add json for languages
        complexFields += s" languages ${args(cursors.getOrElse("languages",""))}" +
          s"{totalCount ${nodes(fields(languagesInfo))}}"
      }
    }
    if(starGazersInfo != null){
      if(!paginate || (paginate && cursors.get("stargazers") != None)){
        // Add json for stargazers
        complexFields += s" stargazers ${args(cursors.getOrElse("stargazers",""))}" +
          s"{totalCount ${nodes(fields(starGazersInfo))}}"
      }
    }
    if(collaboratorsInfo != null){
      if(!paginate || (paginate && cursors.get("collaborators") != None)){
        // Add json for collaborators
        complexFields += s" collaborators${args(cursors.getOrElse("collaborators",""))}" +
          s"{totalCount ${nodes(fields(collaboratorsInfo))}}"
      }
    }
    if(commitsInfo != null){
      if(!paginate || (paginate && cursors.get("commits") != None)){
        // Add json for commits
        val history = s" history ${args(cursors.getOrElse("history",""))}" +
          s"{totalCount ${nodes(fields(commitsInfo))}}"
        complexFields += s" commits: defaultBranchRef{target{... on Commit{ $history}}}"
      }
    }
    if(issuesInfo != null){
      if(!paginate || (paginate && cursors.get("issues") != None)){
        // Add json for issues
        complexFields += s" issues ${args(cursors.getOrElse("issues",""))}" +
          s"{totalCount ${nodes(fields(issuesInfo))}}"
      }
    }

    // Set default simple fields
    var simpleFields = defaultFields
    // Check if user set simple fields for repository
    if(repoInfo != null && repoInfo.length != 0){
      simpleFields = fields(repoInfo)
    }

    // Combine simple and complex fields for repositories
    val allFields = s"${simpleFields.trim.dropRight(1)} $complexFields}"

    // Query repository for another user
    if(repo != "" && owner != ""){
      s"""query{repository(name: \\"$repo\\", owner: \\"$owner\\") $allFields}"""
    }
    // Query repository for user
    else if(repo != ""){
      s"""query{viewer {name repository(name: \\"$repo\\") $allFields}}"""
    }
    // Query all repositories for user
    else{
      s"query{viewer {name repositories ${args(cursors.getOrElse("repositories",""))}" +
        s"{totalCount ${nodes(allFields)}}}}"
    }
  }
}
