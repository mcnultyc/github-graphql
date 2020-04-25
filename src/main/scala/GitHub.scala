
sealed trait GitHubInfo
object GitHubInfo{

  sealed trait Empty   extends GitHubInfo
  sealed trait Auth    extends GitHubInfo // Authorization token
  sealed trait Headers extends GitHubInfo // HTTP headers

  // Mandatory traits for github
  type MandatoryInfo = Empty with Auth with Headers
}

case class GitHubBuilder[I <: GitHubInfo](/* TODO placeholder:*/ placeholder:String){

  import GitHubInfo._

  // Required methods for github connection

  def withAuth(token: String): GitHubBuilder[I with Auth] ={
    // TODO
    this.copy()
  }

  def withHeaders(headers: List[Map[String, String]]): GitHubBuilder[I with Headers] ={
    // TODO
    this.copy()
  }

  // Builder method
  def build(implicit ev: I =:= MandatoryInfo): GitHub = {
    // TODO
    new GitHub
  }
}

class GitHub {

  def getToken(): String ={
    // TODO might remove this method
    ""
  }

  def getHeaders(): String ={
    // TODO
    ""
  }
}
