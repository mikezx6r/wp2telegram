package ca.wilmik.domain

import java.text.SimpleDateFormat
import java.util.Date

import scala.xml.Node
import scala.xml.NodeSeq

//FIXME create filename from pubDate(yyyy-mm-dd only)-postId-post_name
//FIXME consider retrieving comments. Not sure where they can be put... Disqus?
case class WordpressItem(
                          title: String,
                          link: String,
                          postDate: Date,
                          creator: String,
                          guid: String,
                          description: String,
                          content: Option[String],
                          excerpt: Option[String],
                          postId: String,
                          postName: String,
                          status: String, // publish, draft, private, pending
                          postParent: String,
                          menuOrder: String,
                          postType: String, // page, post
                          postPassword: String,
                          categories: Seq[String],
                          tags: Seq[String]) {
  def filename: String = {
    val hiddenPrefix = if (isHidden) {
      if (isPasswordProtected) {
        "_pwd_"
      } else if(isTrash) {
        "_trash_"
      } else {
        "_"
      }
    } else {
      ""
    }
    val datePortion = if (postType == "post") {
      dateDisplay + "-"
    } else {
      ""
    }
    hiddenPrefix + datePortion + (if (postName.nonEmpty) {postName} else {postId})
  }

  def hasAlias = {
    isLinkValid && link.replaceAll("^/","") != filename
  }

  def dateDisplay = {
    new SimpleDateFormat("yyyy-MM-dd").format(postDate)
  }

  def isPrivate = {
    status == "private"
  }

  def isDraft = {
    status == "draft"
  }

  def isPending = {
    status == "pending"
  }
  def isTrash = {
    status == "trash"
  }

  def isPublished = {
    status == "publish"
  }

  def isHidden = {
    isPrivate || isDraft || isPending || isPasswordProtected || isTrash
  }

  def isPasswordProtected = {
    postPassword.nonEmpty
  }

  def isLinkValid = {
    !link.contains("?")
  }
}

object WordpressItem extends NodeParser {
  def apply(baseSiteUrl: String, item: Node): WordpressItem = {

    val encoded = (item \ "encoded")
    var content: Option[String] = None
    var excerpt: Option[String] = None
    encoded.foreach(_ match {
      case <content:encoded>{ txt }</content:encoded> => content = Some(txt.text)
      case <excerpt:encoded>{ txt }</excerpt:encoded> => excerpt = Some(txt.text)
      case _ => // neither of the above so do nothing. if excerpt is empty, it won't match above, but we don't care
    })
    val categoryElems = item \\ "category"

    val categories = catList("category", categoryElems)
    val tags = catList("tag", categoryElems)

    implicit val node = item

    val link = value("link").replace(baseSiteUrl, "").replaceFirst("/$", "")

    new WordpressItem(value("title"), link,
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value("post_date")),
      value("creator"),
      value("guid"),
      value("description"),
      content,
      excerpt,
      value("post_id"),
      value("post_name"),
      value("status"),
      value("post_parent"),
      value("menu_order"),
      value("post_type"),
      value("post_password"),
      categories,
      tags)
  }

  def catList(domain: String, catElems: NodeSeq): Seq[String] = {
    catElems.filter(_ match {
      case n@ <category>{ text }</category> if (n \ "@domain" text) == domain => true
      case _ => false
    }).map(_.text)

  }
}