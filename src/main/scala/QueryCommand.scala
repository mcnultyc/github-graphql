
// Optional fields for commits
object CommitInfo extends Enumeration{
  type CommitInfo = Value
  val AUTHOR        = Value("author") // graph-ql field
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


import CommitInfo.CommitInfo
import LanguageInfo.LanguageInfo
import UserInfo.UserInfo


sealed trait QueryInfo
object QueryInfo{

  sealed trait Empty extends QueryInfo
  sealed trait Repo extends QueryInfo

  // Mandatory traits for query
  type MandatoryInfo = Empty with Repo
}

case class QueryBuilder[I <: QueryInfo](repo: String = "",
                                        commitsInfo: List[CommitInfo] = null,
                                        languageInfo: List[LanguageInfo] = null,
                                        languagesInfo: List[LanguageInfo] = null,
                                        starGazersInfo: List[UserInfo] = null,
                                        collaboratorsInfo: List[UserInfo] = null){

  import QueryInfo._

  // Required methods for query

  def withRepo(name: String): QueryBuilder[I with Repo] ={
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

  // Builder method
  def build(implicit ev: I =:= MandatoryInfo): QueryCommand = {
    new QueryCommand(repo, commitsInfo, languageInfo,
      languagesInfo, starGazersInfo, collaboratorsInfo)
  }
}


class QueryCommand(repo: String = "",
                   commitsInfo: List[CommitInfo] = null,
                   languageInfo: List[LanguageInfo] = null,
                   languagesInfo: List[LanguageInfo] = null,
                   starGazersInfo: List[UserInfo] = null,
                   collaboratorsInfo: List[UserInfo] = null){

  val query = createQuery()


  def graphQLFields(info: List[Any]): String = {
    info.map(x => x.toString).mkString(" ")
  }

  def createQuery(): String ={
    var query = ""
    // Add root repo
    if(repo == ""){
      query = "{viewer {name repositories(first:100){totalCount nodes{"
    }
    else{
      query = s"query{viewer {name repository(name: $repo){"
    }
    // Add json for commit
    if(commitsInfo != null){
      var history = "history{totalCount}"
      if(commitsInfo.length != 0){
        history = s"history{totalCount nodes{${graphQLFields(commitsInfo)}}}"
      }
      query += s""" object(expression:"master"){ ... on Commit{$history}}"""
    }
    // Add json for languages(s)
    if(languageInfo != null){
      query += s" primaryLanguage{${graphQLFields(languageInfo)}}"
    }
    else if(languagesInfo != null){
      query += " languages(first:100){totalCount"
      if(languagesInfo.length != 0){
        query += s" nodes{${graphQLFields(languagesInfo)}} pageInfo{endCursor hasNextPage}}"
      }
      else{
        query += "}"
      }
    }
    // Add json for stargazers
    if(starGazersInfo != null){
      query += " stargazers(first:100){totalCount "
      if(starGazersInfo.length != 0){
        query += s" nodes{${graphQLFields(starGazersInfo)}} pageInfo{endCursor hasNextPage}"
      }
      query += "}"
    }
    // Add json for collaborators
    if(collaboratorsInfo != null){
      query += " collaborators(first:100){totalCount "
      if(collaboratorsInfo.length != 0){
        query += s" nodes{${graphQLFields(collaboratorsInfo)}} pageInfo{endCursor hasNextPage}"
      }
      query += "}"
    }

    query += "} pageInfo{endCursor hasNextPage}}}}"
    query
  }

  def getQuery(): String ={
    query
  }
}
