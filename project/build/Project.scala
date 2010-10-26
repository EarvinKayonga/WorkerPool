import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) with AkkaProject with IdeaProject{
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
}
