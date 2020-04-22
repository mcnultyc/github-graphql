
sealed trait QueryInfo

object QueryInfo{

  sealed trait Empty extends QueryInfo
  sealed trait Repo extends QueryInfo
  sealed trait Language extends QueryInfo

  type MandatoryInfo = Empty with Repo with Language
}

case class QueryBuilder[I <: QueryInfo](
  repo: String = "all repos", language: String = "java", commits: Int = 3){

  import QueryInfo._

  def addRepo(repo: String): QueryBuilder[I with Repo] ={
    this.copy(repo=repo)
  }

  def addLanguage(language: String): QueryBuilder[I with Language] ={
    this.copy(language=language)
  }

  def addCommits(commits: Int): QueryBuilder[I] ={
    this.copy(commits =commits)
  }

  def build(implicit ev: I =:= MandatoryInfo): QueryCommand =
    QueryCommand(repo, language, commits)
}

case class QueryCommand(repo: String, language: String, commits: Int){

}