import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder


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
  sealed trait Auth  extends QueryInfo
  sealed trait Repo  extends QueryInfo

  // Mandatory traits for query
  type MandatoryInfo = Empty with Auth with Repo
}

case class QueryBuilder[I <: QueryInfo](repo: String = "",
                                        commitsInfo: List[CommitInfo] = null,
                                        languageInfo: List[LanguageInfo] = null,
                                        languagesInfo: List[LanguageInfo] = null,
                                        starGazersInfo: List[UserInfo] = null,
                                        collaboratorsInfo: List[UserInfo] = null,
                                        authorizer: GitHub = null){

  import QueryInfo._

  // Required methods for query

  def withAuth(auth: GitHub): QueryBuilder[I with Auth] ={
    this.copy(authorizer = auth)
  }

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
      languagesInfo, starGazersInfo, collaboratorsInfo, authorizer)
  }
}


class QueryCommand(repo: String = "",
                   commitsInfo: List[CommitInfo] = null,
                   languageInfo: List[LanguageInfo] = null,
                   languagesInfo: List[LanguageInfo] = null,
                   starGazersInfo: List[UserInfo] = null,
                   collaboratorsInfo: List[UserInfo] = null,
                   authorizer: GitHub = null){

  val query = createQuery()
  val response = execute(query)

  // TODO
  // call parse method here using response string
  parse(response)

  private def graphQLFields(info: List[Any]): String = {
    info.map(x => x.toString).mkString(" ")
  }

  private def execute(query: String): String ={
    // TODO
    // build http post request using authorizers token
    val token = authorizer.getToken()
    // placeholder
    """{"data":{"viewer":{"name":"Carlos McNulty","repositories":{"totalCount":46,"nodes":[{"languages":{"totalCount":2,"nodes":[{"name":"C++","color":"#f34b7d"},{"name":"Makefile","color":"#427819"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOPVnH-A==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"Java","color":"#b07219"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOP16dOA==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"C++","color":"#f34b7d"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOP5p4vQ==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"Java","color":"#b07219"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOP90HeQ==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":3,"nodes":[{"name":"Carlos McNulty","email":""},{"name":null,"email":""},{"name":null,"email":"igrief2@uic.edu"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOApE3qw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C++","color":"#f34b7d"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQJDgzg==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":3,"nodes":[{"name":"HTML","color":"#e34c26"},{"name":"JavaScript","color":"#f1e05a"},{"name":"CSS","color":"#563d7c"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQJGH_Q==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"Python","color":"#3572A5"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQTMMKA==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":4,"nodes":[{"name":"Carlos McNulty","email":""},{"name":null,"email":""},{"name":"Shyam Patel","email":""},{"name":null,"email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAlaNVA==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQcyUEw==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":6,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Balajee Vamanan","email":"bvamanan@uic.edu"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""},{"name":null,"email":""},{"name":"Hamid","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAeJXlQ==","hasNextPage":false}}},{"languages":{"totalCount":3,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"},{"name":"Shell","color":"#89e051"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQhPXCw==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":6,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Balajee Vamanan","email":"bvamanan@uic.edu"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""},{"name":null,"email":""},{"name":"Hamid","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAeJXlQ==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQpwWOw==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":6,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Balajee Vamanan","email":"bvamanan@uic.edu"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""},{"name":null,"email":""},{"name":"Hamid","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAeJXlQ==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"Python","color":"#3572A5"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQjyPVw==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"Python","color":"#3572A5"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQrl9Ag==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":3,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C++","color":"#f34b7d"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQu34eQ==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":6,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Balajee Vamanan","email":"bvamanan@uic.edu"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""},{"name":null,"email":""},{"name":"Hamid","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAeJXlQ==","hasNextPage":false}}},{"languages":{"totalCount":3,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"},{"name":"C++","color":"#f34b7d"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQwoO3A==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":6,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Balajee Vamanan","email":"bvamanan@uic.edu"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""},{"name":null,"email":""},{"name":"Hamid","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAeJXlQ==","hasNextPage":false}}},{"languages":{"totalCount":0,"nodes":[],"pageInfo":{"endCursor":null,"hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"C++","color":"#f34b7d"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQ42tlg==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":2,"nodes":[{"name":"Carlos McNulty","email":""},{"name":"Shyam Patel","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAlaNOw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Shell","color":"#89e051"},{"name":"Python","color":"#3572A5"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQ6YEOQ==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":2,"nodes":[{"name":"Vineet Patel ","email":"vineetrpatel@gmail.com"},{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOQ_DtZg==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":6,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Balajee Vamanan","email":"bvamanan@uic.edu"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""},{"name":null,"email":""},{"name":"Hamid","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAeJXlQ==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHORI2RNA==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":10,"nodes":[{"name":"Emacs Lisp","color":"#c065db"},{"name":"Makefile","color":"#427819"},{"name":"C++","color":"#f34b7d"},{"name":"C","color":"#555555"},{"name":"Assembly","color":"#6E4C13"},{"name":"Perl","color":"#0298c3"},{"name":"Objective-C","color":"#438eff"},{"name":"Shell","color":"#89e051"},{"name":"Ruby","color":"#701516"},{"name":"OpenEdge ABL","color":null}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHORrO-Tw==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Xingbo Wu","email":""},{"name":"Nilanjana Basu","email":"nilanjana.basu87@gmail.com"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAs3CDw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"Assembly","color":"#6E4C13"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHORua2mg==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Xingbo Wu","email":""},{"name":"Nilanjana Basu","email":"nilanjana.basu87@gmail.com"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAs3CDw==","hasNextPage":false}}},{"languages":{"totalCount":9,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"},{"name":"C++","color":"#f34b7d"},{"name":"Assembly","color":"#6E4C13"},{"name":"Perl","color":"#0298c3"},{"name":"Objective-C","color":"#438eff"},{"name":"Shell","color":"#89e051"},{"name":"Ruby","color":"#701516"},{"name":"OpenEdge ABL","color":null}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOR1zpYA==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Xingbo Wu","email":""},{"name":"Nilanjana Basu","email":"nilanjana.basu87@gmail.com"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAs3CDw==","hasNextPage":false}}},{"languages":{"totalCount":9,"nodes":[{"name":"C","color":"#555555"},{"name":"C++","color":"#f34b7d"},{"name":"Makefile","color":"#427819"},{"name":"Assembly","color":"#6E4C13"},{"name":"Perl","color":"#0298c3"},{"name":"Objective-C","color":"#438eff"},{"name":"Shell","color":"#89e051"},{"name":"Ruby","color":"#701516"},{"name":"OpenEdge ABL","color":null}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOR9p4lg==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Xingbo Wu","email":""},{"name":"Nilanjana Basu","email":"nilanjana.basu87@gmail.com"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAs3CDw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"Jupyter Notebook","color":"#DA5B0B"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSEygow==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":3,"nodes":[{"name":"Carlos McNulty","email":""},{"name":"Shyam Patel","email":""},{"name":"Manasa Kandimalla","email":"manasakandimalla9@gmail.com"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOA2V60g==","hasNextPage":false}}},{"languages":{"totalCount":8,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C++","color":"#f34b7d"},{"name":"Assembly","color":"#6E4C13"},{"name":"C","color":"#555555"},{"name":"Objective-C","color":"#438eff"},{"name":"Perl","color":"#0298c3"},{"name":"Shell","color":"#89e051"},{"name":"Ruby","color":"#701516"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSGsCKA==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Xingbo Wu","email":""},{"name":"Nilanjana Basu","email":"nilanjana.basu87@gmail.com"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAs3CDw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"Jupyter Notebook","color":"#DA5B0B"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSKcbEg==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":3,"nodes":[{"name":"Carlos McNulty","email":""},{"name":"Shyam Patel","email":""},{"name":"Manasa Kandimalla","email":"manasakandimalla9@gmail.com"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOA2V60g==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"Jupyter Notebook","color":"#DA5B0B"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSP1DFw==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":3,"nodes":[{"name":"Carlos McNulty","email":""},{"name":"Shyam Patel","email":""},{"name":"Manasa Kandimalla","email":"manasakandimalla9@gmail.com"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOA2V60g==","hasNextPage":false}}},{"languages":{"totalCount":8,"nodes":[{"name":"C","color":"#555555"},{"name":"Makefile","color":"#427819"},{"name":"C++","color":"#f34b7d"},{"name":"Assembly","color":"#6E4C13"},{"name":"Objective-C","color":"#438eff"},{"name":"Perl","color":"#0298c3"},{"name":"Shell","color":"#89e051"},{"name":"Ruby","color":"#701516"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSMaI4w==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Jakob Eriksson","email":"jakob@uic.edu"},{"name":"Xingbo Wu","email":""},{"name":"Nilanjana Basu","email":"nilanjana.basu87@gmail.com"},{"name":"Carlos McNulty","email":""},{"name":null,"email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAs3CDw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSVkb3A==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":3,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"},{"name":"C++","color":"#f34b7d"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSVkqlA==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSVko5A==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":3,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"Shell","color":"#89e051"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSVks5w==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSVkt7g==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Dockerfile","color":"#384d54"},{"name":"Scala","color":"#c22d40"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSVmc6A==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Scala","color":"#c22d40"},{"name":"Python","color":"#3572A5"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSVmoFA==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Python","color":"#3572A5"},{"name":"HTML","color":"#e34c26"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSYaoKA==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":5,"nodes":[{"name":"C","color":"#555555"},{"name":"Makefile","color":"#427819"},{"name":"Lex","color":"#DBCA00"},{"name":"C++","color":"#f34b7d"},{"name":"Shell","color":"#89e051"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSkjaMQ==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":3,"nodes":[{"name":"C","color":"#555555"},{"name":"Makefile","color":"#427819"},{"name":"Shell","color":"#89e051"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSpoxOQ==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":0,"nodes":[],"pageInfo":{"endCursor":null,"hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Xingbo Wu","email":""},{"name":"Karan Raghani","email":"karanraghani14@gmail.com"},{"name":"Sepideh Roghanchi","email":""},{"name":null,"email":""},{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Shell","color":"#89e051"},{"name":"Python","color":"#3572A5"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSrwPcg==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":4,"nodes":[{"name":"C","color":"#555555"},{"name":"Makefile","color":"#427819"},{"name":"C++","color":"#f34b7d"},{"name":"Shell","color":"#89e051"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSs43Hg==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":1,"nodes":[{"name":"Python","color":"#3572A5"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSv5B4Q==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":1,"nodes":[{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOSzSU4Q==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Xingbo Wu","email":""},{"name":"Karan Raghani","email":"karanraghani14@gmail.com"},{"name":"Sepideh Roghanchi","email":""},{"name":null,"email":""},{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":3,"nodes":[{"name":"HTML","color":"#e34c26"},{"name":"JavaScript","color":"#f1e05a"},{"name":"CSS","color":"#563d7c"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOS6WJxQ==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":2,"nodes":[{"name":"Carlos McNulty","email":""},{"name":null,"email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAlaNVA==","hasNextPage":false}}},{"languages":{"totalCount":3,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"HTML","color":"#e34c26"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOS_t8cQ==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Xingbo Wu","email":""},{"name":"Karan Raghani","email":"karanraghani14@gmail.com"},{"name":"Sepideh Roghanchi","email":""},{"name":null,"email":""},{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}},{"languages":{"totalCount":2,"nodes":[{"name":"Makefile","color":"#427819"},{"name":"C","color":"#555555"}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOTKONpg==","hasNextPage":false}},"stargazers":{"totalCount":0},"collaborators":{"totalCount":5,"nodes":[{"name":"Xingbo Wu","email":""},{"name":"Karan Raghani","email":"karanraghani14@gmail.com"},{"name":"Sepideh Roghanchi","email":""},{"name":null,"email":""},{"name":"Carlos McNulty","email":""}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOAXO7zw==","hasNextPage":false}}}],"pageInfo":{"endCursor":"Y3Vyc29yOnYyOpHOD1chhg==","hasNextPage":false}}}}}
      |""".stripMargin
  }


  private def parse(response: String): Unit ={
    // TODO
    // parse data from response
  }


  private def createQuery(): String ={
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
