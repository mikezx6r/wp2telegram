package ca.wilmik.domain

import scala.xml.XML
import scalax.file.Path
import scalax.file.ImplicitConverters._
import scala.xml.Node
import scala.xml.NodeSeq

//FIXME - need to retrieve categories, authors and tags.
//FIXME -  anything else need to be retrieved?

case class WordpressBlog(
  title: String,
  description: String,
  baseSiteUrl: String,
  blogRoot: String,
  posts: Option[Seq[WordpressItem]],
  pages: Option[Seq[WordpressItem]],
  categories: Option[Seq[WordpressCategoryKnowsParent]],
  tags: Option[Seq[WordpressTag]]) {
  def hasPosts = {
    posts.isDefined
  }

  def hasPages = {
    pages.isDefined
  }
}

object WordpressBlog extends NodeParser {
  def apply(exportFileName: String): WordpressBlog = {
    val export = XML.loadFile(Path.fromString(exportFileName).asFile)
    implicit val channel = export \\ "channel"

    val baseSiteUrl = value("base_site_url")
    val baseBlogUrl = value("base_blog_url")
    val blogRoot = baseBlogUrl.replace(baseSiteUrl, "/")

    val items = (channel \ "item").map {
      item =>
        WordpressItem(baseSiteUrl, item)
    }.groupBy(_.postType)

    val tags = (channel \ "tag").map {
      WordpressTag(_)
    }
    val actualTags = if (tags.isEmpty) {
      None
    } else {
      Some(tags)
    }

    val actualCategories = getCategories(channel)

    new WordpressBlog(value("title"),
      value("description"), baseSiteUrl, blogRoot, items.get("post"), items.get("page"), actualCategories, actualTags)
  }
  
  private def getCategories(channel: NodeSeq) : Option[Seq[WordpressCategoryKnowsParent]] = {
    val categories = (channel \ "category").map {
      WordpressCategoryKnowsParent(_)
    }
    
    if (categories.isEmpty) {
      None
    } 
    Some(categories)
//    
//  todo FIXME: Still need to create a category that contains its children rather than a child that knows it's parent so could generate a tree object  
//    
//    val catByParent = categories.groupBy(_.parent)
//    val topParentsMap = catByParent.get("").get.groupBy(_.name)
//    
//    val categoriesEnd = for((parent, children) <- catByParent if !parent.isEmpty()) {
//        val p = WordpressCategory("test","test","test",children)
//        p
//    } 
//
//    None
  }
}