package ca.wilmik.domain

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfter
import java.util.Date
import java.text.SimpleDateFormat

class WordpressItemSuite extends FunSuite with ShouldMatchers {
  val postDate = "2012-01-02"
  var basePage: WordpressItem = WordpressItem("title", "/link", new Date(), "creator", "guid", "description", Some("content"), Some("excerpt"), "156", "postname", "publish", "", "0", "page", "", Seq(""), Seq(""))
  var basePost: WordpressItem = WordpressItem("title", "/link", new SimpleDateFormat("yyyy-MM-dd").parse(postDate), "creator", "guid", "description", Some("content"), Some("excerpt"), "156", "postname", "publish", "", "0", "post", "", Seq(""), Seq(""))

  test("Page is published and link is valid. Filename is correct") {
    basePage should be('published)
    basePage should not be ('hidden)
    basePage should be('linkValid)
    basePage.filename should be("postname")
  }

  test("Page is published and password protected. ensure filename is correct.") {
    val page = basePage.copy(postPassword = "some password")
    page should be('published)
    page should be('hidden)
    page.filename should be("_pwd_postname")
  }

  test("Page is published. Link is invalid. Page is private. Filename is correct") {
    val page = basePage.copy(link = "/?p=122", status = "private")

    page should be('private)
    page should be('hidden)
    page should not be ('linkValid)
    page.filename should be("_postname")
  }

  test("Private page is hidden") {
    val page = basePage.copy(status = "private")

    page should be('private)
    page should be('hidden)
    page.filename should be("_postname")
  }

  test("Draft page is hidden") {
    val page = basePage.copy(status = "draft")

    page should be('draft)
    page should be('hidden)
    page.filename should be("_postname")
  }

  test("Pending page is hidden") {
    val page = basePage.copy(status = "pending")

    page should be('pending)
    page should be('hidden)
  }
  
  test("Trashed page is hidden and has appropriate prefix") {
    val page = basePage.copy(status = "trash")
    
    page should be('trash)
    page should be('hidden)
    page.filename should be("_trash_postname")
  }

  test("Post is published and link is valid. Filename contains date prefix") {
    basePost should be('published)
    basePost should not be ('hidden)
    basePost should be('linkValid)
    basePost.filename should be(postDate + "-postname")
  }

  test("Post is private and link is valid. Filename contains hidden and date prefix") {
    val post = basePost.copy(status = "private")
    
    post should be('private)
    post should be('hidden)
    post.filename should be("_"+postDate+"-postname")
  }

  test("Post is published and password protected. link is valid. Filename contains hidden and date prefix") {
    val post = basePost.copy(postPassword = "some password")

    post should be('published)
    post should be('hidden)
    post.filename should be("_pwd_"+postDate+"-postname")
  }
  
  test("Trashed post is hidden and has appropriate prefix") {
    val post = basePost.copy(status = "trash")
    
    post should be('trash)
    post should be('hidden)
    post.filename should be("_trash_"+postDate+"-postname")
  }
}