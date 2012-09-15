package ca.wilmik.page
import org.scalatest.FunSuite
import scala.xml.XML
import ca.wilmik.domain.WordpressItem
import ca.wilmik.domain.WordpressBlog
import org.scalatest.matchers.ShouldMatchers
import java.util.Date
import java.text.SimpleDateFormat
import org.scalatest.OptionValues
import ca.wilmik.domain.WordpressTag

class ConvertPostSuite extends FunSuite with ShouldMatchers with OptionValues {
  
  //FIXME - cover case where site and blog url are different.
  //FIXME - cover other edge cases can think of.
  
  // Very useful link to Scala XML parsing: http://www.codecommit.com/blog/scala/working-with-scalas-xml-support

  test("read file and output line for each post") {
    val exp = this.getClass.getClassLoader().getResource("wp_export.xml")
    val blog = WordpressBlog(exp.getFile())
    blog.title should be("Title of my Site goes here")
    blog.description should be("This is the best site ever!")
    blog.blogRoot should be("/")
    blog.baseSiteUrl should be("http://somesite.myco.com")
    blog.hasPages should be (true)
    blog.hasPosts should be (true)
    blog.tags.value should have length(2)
    blog.tags.value should contain(new WordpressTag("26","iPod Information","ipod", "Information"))
    blog.categories.value should have length(5)
  }

  test("All of item is parsed") {
    val baseSiteUrl = "http://somesite.com"
    val itemXml =
      <item>
        <title>title</title>
        <link>{baseSiteUrl}/postLink/</link>
        <pubDate>Fri, 05 Mar 2004 02:19:32 +0000</pubDate>
        <dc:creator>mike</dc:creator>
        <guid isPermaLink="false">http://mike.vhandw.com/archives/2004/03/04/noisy-pc-continued/</guid>
        <description>some description</description>
        <content:encoded><![CDATA[content]]></content:encoded>
        <excerpt:encoded><![CDATA[excerpt]]></excerpt:encoded>
        <wp:post_id>17</wp:post_id>
        <wp:post_date>2004-03-04 16:19:32</wp:post_date>
        <wp:post_date_gmt>2004-03-05 02:19:32</wp:post_date_gmt>
        <wp:comment_status>closed</wp:comment_status>
        <wp:ping_status>closed</wp:ping_status>
        <wp:post_name>post name</wp:post_name>
        <wp:status>publish</wp:status>
        <wp:post_parent>12</wp:post_parent>
        <wp:menu_order>15</wp:menu_order>
        <wp:post_type>post</wp:post_type>
        <wp:post_password>some password</wp:post_password>
        <wp:is_sticky>0</wp:is_sticky>
        <category domain="category" nicename="computer"><![CDATA[Computer]]></category>
        <category domain="category" nicename="hardware"><![CDATA[Hardware]]></category>
        <category domain="tag"><![CDATA[Punctuation]]></category>
        <category domain="tag" nicename="example"><![CDATA[Example]]></category>
      </item>
    val wpItem = WordpressItem(baseSiteUrl, itemXml)
    val postDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2004-03-04 16:19:32")
    wpItem should have(
      'title("title"),
      'link("/postLink"),
      'creator("mike"),
      'guid("http://mike.vhandw.com/archives/2004/03/04/noisy-pc-continued/"),
      'description("some description"),
      'content(Some("content")),
      'excerpt(Some("excerpt")),
      'postId("17"),
      'postDate(postDate),
      'postName("post name"),
      'status("publish"),
      'postParent("12"),
      'menuOrder("15"),
      'postType("post"),
      'postPassword("some password"))

    wpItem.categories should be(List("Computer", "Hardware"))
    wpItem.tags should be(List("Punctuation", "Example"))
  }

  test("read namespaced content and excerpt - both populated") {
    val expContent = "<!CDATA[some expected content]]>"
    val expExcerpt = "some expected excerpt"

    val item = <item>
                 <post_date>2004-03-04 16:19:32</post_date>
                 <content:encoded>{ expContent }</content:encoded>
                 <excerpt:encoded>{ expExcerpt }</excerpt:encoded>
                 <wp:post_id>17</wp:post_id>
               </item>
    val wpItem = WordpressItem("baseSiteUrl", item)
    wpItem.content.get should equal(expContent)
    wpItem.excerpt.get should equal(expExcerpt)
  }

  test("read namespaced content and excerpt - only content") {
    val expContent = "<!CDATA[some expected content]]>"

    val item = <item>
                 <post_date>2004-03-04 16:19:32</post_date>
                 <content:encoded>{ expContent }</content:encoded>
                 <excerpt:encoded></excerpt:encoded>
                 <wp:post_id>17</wp:post_id>
               </item>
    val wpItem = WordpressItem("", item)
    wpItem.content.get should equal(expContent)
    wpItem.excerpt should equal(None)
  }
  test("read namespaced content and excerpt - only excerpt") {
    val expExcerpt = "some expected excerpt"

    val item = <item>
                 <post_date>2004-03-04 16:19:32</post_date>
                 <content:encoded></content:encoded>
                 <excerpt:encoded>{ expExcerpt }</excerpt:encoded>
                 <wp:post_id>17</wp:post_id>
               </item>
    val wpItem = WordpressItem("", item)
    wpItem.content should equal(None)
    wpItem.excerpt.get should equal(expExcerpt)
  }

  test("CDATA doesn't get escaped") {
    val xml = <item><subitem><![CDATA[some < data > with & characters]]></subitem></item>
    val text = (xml \ "subitem").text
    text should equal("some < data > with & characters")
  }
}