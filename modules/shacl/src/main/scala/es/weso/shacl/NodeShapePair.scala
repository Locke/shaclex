package es.weso.shacl

import es.weso.rdf.nodes._
import cats._, data._
import cats.implicits._

case class NodeShapePair(node: RDFNode, shape: ShapeRef) {

implicit val nodeShapeShow = new Show[NodeShapePair] {
  def show(ns: NodeShapePair) = s"[$node,${shape.showId}]"
 }

override def toString = Show[NodeShapePair].show(this)

}
