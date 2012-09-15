package ca.wilmik.processor

import scala.xml.XML

import ca.wilmik.domain.WordpressBlog
import scalax.file.Path
import scalax.io.Output
import scalax.io.Seekable

object ConvertWpSiteToTelegram extends App {
  val newline = sys.props.getOrElse("line.separator", "/n")

  val (logger, formatter) = ZeroLoggerFactory.newLogger(this)

  import formatter._

  if (args.size != 2) {
    showUsage()
  } else {
    logger.info { _ ++= "Starting process to convert a Wordpress site to Telegram format" ++= "\nInput File:" ++= args(0) ++= "\nOutput directory:" ++= args(1) }
    val outDir = Path.fromString(args(1))

    if (!outDir.exists) {
      logger.warning("creating directory")
      outDir.createDirectory()
    }
    if (outDir.exists && outDir.isDirectory) {
      logger.warning("Found directory and generating site")
      generateSite(outDir)
    } else {
      showUsage()
    }
  }

  def generateSite(implicit outDir: Path) {
    implicit val blog = WordpressBlog(args(0))

    createExtraInfo
    createArchive
    createIndex
    createTemplates
    createPages
    createPosts
  }

  def createExtraInfo(implicit outDir: Path, blog: WordpressBlog) {
    implicit val outPath = outDir \ "_extra_info.md"
    write("---")

    append("blog_root: " + blog.blogRoot)
    append("site_title: " + blog.title)
    append("template_url: https://github.com/telegr-am/template-blue.git")
    append("")
    append("---")
    append("---   TAGS   ---")
    if (blog.tags.isDefined) { blog.tags.get.foreach(tag => append(tag.toString)) }
    append("---   CATEGORIES   ---")
    if (blog.categories.isDefined) { blog.categories.get.foreach(cat => append(cat.toString)) }
    append("")
    append("""This file contains _Extra Info_ about your site.  You can
put information in here like which directory you want your posts in
and that sort of stuff and the information will be part of every page
in your site.  But this page will not be part of your site.
            """)
  }

  def createArchive(implicit outDir: Path, blog: WordpressBlog) {
    val lines = Seq(
      "[title: Archive]: /",
      "[order: 10]: /",
      "[show-if: has_blog]: /",
      "",
      "<div class=\"page-header\">",
      "	<h1>Blog Archive</h1>",
      "</div>",
      "",
      "<div data-lift=\"archived_posts\">",
      "	<div name=\"year-block\">",
      "		<h2 name=\"year\">1999</h2>",
      "		<div name=\"month-block\">",
      "			<h3 name=\"month\">Febtember</h3>",
      "			<ul>",
      "				<li name=\"post-block\">",
      "				<span name=\"post-date\">sometime</span>",
      "				",
      "				<a name=\"post-title\" href=\"#\">Something fun</a>",
      "				</li>",
      "			</ul>",
      "	    </div>",
      "	</div>",
      "</div>")

    val outPath = outDir \ "archive.md"
    outPath.writeStrings(lines, newline)
  }

  def createIndex(implicit outDir: Path, blog: WordpressBlog) {
    val lines = Seq("<div data-lift=\"if?extra_true=has_blog\">",
      "      <div data-lift=\"blog.simple\"></div>",
      "</div>")

    implicit val outPath = outDir \ "index.md"
    outPath.writeStrings(lines, newline)
    append("")
    appendExtra("title", "Blog")
  }

  def createTemplates(implicit outDir: Path, blog: WordpressBlog) {
    val templateDir = outDir \ "templates-hidden"
    if (!templateDir.exists) {
      templateDir.createDirectory()
    }
    val include = <div>
                    <span data-css="footer *" data-lift="xform">Blog (c) 2012 by me</span>
                  </div>
    val outPath = templateDir \ "include.html"
    XML.save(outPath.path, include)
  }

  def createPages(implicit outDir: Path, blog: WordpressBlog) {
    if (blog.hasPages) {
      blog.pages.get.foreach {
        page =>

          implicit val outPath = outDir \ (page.filename + ".md")
          writeExtra("title", page.title)
          if (page.isHidden) {
            append("[### PRIVATE/Draft POST IN WORDPRESS ###]: /")
            appendExtra("serve", "false")
          }
          if (page.isPasswordProtected) {
            append("[### POST PASSWORD PROTECTED IN WORDPRESS ###]: /")
          }
          appendExtra("order", page.menuOrder)
          if (page.hasAlias) {
            appendExtra("alias", page.link)
          }
          if (!page.tags.isEmpty) {
            appendExtra("tags", page.tags.mkString("{", ",", "}"))
          }
          if (!page.categories.isEmpty) {
            appendExtra("category", page.categories.mkString("{", ",", "}"))
          }
          append("")
          append("# " + page.title + "  ###")
          append(page.content.getOrElse(""))
      }
    }
  }

  def createPosts(implicit outDir: Path, blog: WordpressBlog) {
    if (blog.hasPosts) {
      val postDir = outDir \ "_posts"
      if (!postDir.exists) {
        postDir.createDirectory()
      }
      blog.posts.get.foreach {
        post =>
          implicit val outPath = postDir \ (post.filename + ".md")
          writeExtra("title", post.title)
          if (post.isHidden) {
            append("[### PRIVATE/Draft POST IN WORDPRESS ###]: /")
            appendExtra("serve", "false")
          }
          if (post.isPasswordProtected) {
            append("[### POST PASSWORD PROTECTED IN WORDPRESS ###]: /")
          }
          if (post.isLinkValid) {
            appendExtra("path", post.link)
          }
          appendExtra("date", post.dateDisplay)
          if (!post.tags.isEmpty) {
            appendExtra("tags", post.tags.mkString("{", ",", "}"))
          }
          if (!post.categories.isEmpty) {
            appendExtra("category", post.categories.mkString("{", ",", "}"))
          }
          append("")
          append(post.content.getOrElse(""))
      }
    }
  }

  def write(line: String)(implicit out: Output) {
    out write line + newline
  }

  def append(line: String)(implicit out: Seekable) {
    out append line + newline
  }

  def writeExtra(key: String, value: String)(implicit out: Seekable) {
    out write "[" + key + ": " + value + "]: /" + newline
  }

  def appendExtra(key: String, value: String)(implicit out: Seekable) {
    out append "[" + key + ": " + value + "]: /" + newline
  }

  def showUsage() {
    logger.info("""Usage:
	      1. Wordpress export file name. This is a file that has been exported from Wordpress.
	      2. Output directory to write site. This must be a directory, or not already exist.
                 	      """)
  }
}