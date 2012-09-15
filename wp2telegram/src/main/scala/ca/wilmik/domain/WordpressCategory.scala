package ca.wilmik.domain

import scala.xml.Node

//case class WordpressCategory(name: String, slug: String, description: String, children: Option[Seq[WordpressCategory]])

case class WordpressCategoryKnowsParent(name:String, slug:String, description:String, parent:String) {
  override def toString() = {
    "Category(name=%s, slug=%s, description=%s, parent=%s)".format(name, slug, description, parent)
  }
}

object WordpressCategoryKnowsParent extends NodeParser {
  def apply(implicit category:Node) = {
    new WordpressCategoryKnowsParent(value("cat_name"), value("category_nicename"), value("category_description"), value("category_parent"))
  }
}
