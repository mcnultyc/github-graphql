
sealed trait GitHubInfo
object GitHubInfo{

  sealed trait Empty   extends GitHubInfo
  sealed trait Auth    extends GitHubInfo // Authorization token
  sealed trait Headers extends GitHubInfo // HTTP headers

  // Mandatory traits for github
  type MandatoryInfo = Empty with Auth with Headers
}

case class GitHubBuilder[I <: GitHubInfo](token: String = "",
                                          headers: List[(String, String)] = null){

  import GitHubInfo._

  // Required methods for github connection

  def withAuth(token: String): GitHubBuilder[I with Auth] ={
    this.copy(token = token)
  }

  def withHeaders(headers: List[(String, String)]): GitHubBuilder[I with Headers] ={
    this.copy(headers = headers)
  }

  // Builder method
  def build(implicit ev: I =:= MandatoryInfo): GitHub = {
    new GitHub(token, headers)
  }
}

class GitHub(token: String, headers: List[(String, String)]){

  def getToken(): String ={
    token
  }

  def getHeaders(): List[(String, String)] ={
    headers
  }
}
