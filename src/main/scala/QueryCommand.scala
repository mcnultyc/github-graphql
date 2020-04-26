import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

import scala.io.Source.fromInputStream


// Optional fields for commits
object CommitInfo extends Enumeration{
  type CommitInfo = Value
  val AUTHOR        = Value("author")       // graph-ql field
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
  val AUTHOR          = Value("author")
  val BODY            = Value("body")
  val CREATED_AT      = Value("createdAt")
  val NUMBER          = Value("number")
  val PUBLISHED_AT    = Value("publishedAt")
  val CLOSED_AT       = Value("closedAt")
  val LAST_EDITED_AT  = Value("lastEditedAt")
}


import CommitInfo.CommitInfo
import LanguageInfo.LanguageInfo
import UserInfo.UserInfo
import IssueInfo.IssueInfo

sealed trait QueryInfo
object QueryInfo{

  sealed trait Empty extends QueryInfo
  sealed trait Auth  extends QueryInfo  // GitHub authorization token
  sealed trait Repo  extends QueryInfo  // Repository/repositories query

  // Mandatory traits for query
  type MandatoryInfo = Empty with Auth with Repo
}

case class QueryBuilder[I <: QueryInfo](repo: String = "",
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

  def withRepo(name: String): QueryBuilder[I with Repo] ={
    this.copy(repo = name)
  }

  def withRepo(name: String, owner: String): QueryBuilder[I with Repo] = {
    // TODO - allow to look up repo from another owner
    this.copy(repo = name)
  }

  def withRepos(): QueryBuilder[I with Repo] ={
    this.copy(repo = "")
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
    new QueryCommand(repo, commitsInfo, languageInfo, languagesInfo,
      starGazersInfo, collaboratorsInfo, issuesInfo, authorizer)
  }
}


class QueryCommand(repo: String = "",
                   commitsInfo: List[CommitInfo] = null,
                   languageInfo: List[LanguageInfo] = null,
                   languagesInfo: List[LanguageInfo] = null,
                   starGazersInfo: List[UserInfo] = null,
                   collaboratorsInfo: List[UserInfo] = null,
                   issuesInfo: List[IssueInfo] = null,
                   authorizer: GitHub = null){

  val query = createQuery()

  // TODO - unfinished query method
  val response = execute(query)

  // TODO - unfinished parse method
  parse(response)


  private def page(): Unit ={
    // TODO - method to handle pagination for queries
  }


  private def execute(query: String): String ={

    // TODO - handle pagination for queries

    val BASE_GHQL_URL = "https://api.github.com/graphql"

    val client = HttpClientBuilder.create().build()
    // Create HTTP entity with graph-ql query
    val entity = new StringEntity(s"""{"query":"$query"}""" )
    val request = new HttpPost(BASE_GHQL_URL)

    var headers = authorizer.getHeaders()
    // Add authorization header if not already there
    if(!headers.toMap.contains("Authorization")){
      headers = ("Authorization", "bearer "+ authorizer.getToken()) :: headers
    }

    // Set HTTP headers for request
    headers.foreach(x => request.addHeader(x._1, x._2))
    // Set HTTP entity for request
    request.setEntity(entity)

    val response = client.execute(request)

    response.getEntity match{
      case null => null
      case x => {
        // Get json from response content
        val json = fromInputStream(x.getContent).getLines.mkString
        json
      }
    }
  }


  private def parse(response: String): Unit ={
    // TODO - parse response string and extract data
    println(response)
  }


  private def createQuery(): String ={

    def fields(info: List[Any]): String =
      info.map(x => x.toString).mkString(" ")

    def nodes(fields: String) =
      if(fields.trim =="") "" else s"nodes{$fields}pageInfo{endCursor hasNextPage}"

    var repoFields = ""

    // Add json for primary language
    if(languageInfo != null){
      repoFields += s" primaryLanguage{${fields(languageInfo)}}"
    }
    // Add json for languages
    if(languagesInfo != null){
      repoFields += s" languages(first:100){totalCount ${nodes(fields(languagesInfo))}}"
    }
    // Add json for stargazers
    if(starGazersInfo != null){
      repoFields += s" stargazers(first:100){totalCount ${nodes(fields(starGazersInfo))}}"
    }
    // Add json for collaborators
    if(collaboratorsInfo != null){
      repoFields += s" collaborators(first:100){totalCount ${nodes(fields(collaboratorsInfo))}}"
    }
    // Add json for commits
    if(commitsInfo != null){
      // TODO - not sure of graph-ql format for commits
    }
    // Add json for issues
    if(issuesInfo != null){
      repoFields += s" issues(first:100){totalCount ${nodes(fields(issuesInfo))}}"
    }

    // Add root repo
    if(repo == ""){ // Query all repositories
      s"query{viewer {name repositories(first:100){totalCount ${nodes(repoFields)}}}}"
    }
    else{ // Query single repository
      s"query{viewer {name repository(name: $repo){$repoFields}}}"
    }
  }
}
