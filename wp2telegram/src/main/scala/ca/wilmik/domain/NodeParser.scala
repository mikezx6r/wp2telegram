package ca.wilmik.domain

import scala.xml.NodeSeq

trait NodeParser {

  def value(element: String)(implicit node: NodeSeq): String = {
    (node \ element).text
  }
}