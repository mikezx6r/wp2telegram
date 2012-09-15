package ca.wilmik.domain

import scala.xml.Node

case class WordpressTag(id: String, name: String, slug: String, description: String) {
  override def toString() = {
    "Tag(name=%s, slug=%s, description=%s)".format(name, slug, description)
  }
}

object WordpressTag extends NodeParser {
  def apply(implicit tag: Node) = {
    new WordpressTag(value("term_id"), value("tag_name"), value("tag_slug"), value("tag_description"))
  }
}