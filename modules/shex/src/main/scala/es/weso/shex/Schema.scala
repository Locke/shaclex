package es.weso.shex

import cats.implicits._
import es.weso.depgraphs.DepGraph
import es.weso.rdf.{PrefixMap, RDFBuilder, RDFReader}
import es.weso.rdf.nodes.{IRI, RDFNode}
import es.weso.shex.shexR.{RDF2ShEx, ShEx2RDF}

import scala.io.Source
import scala.util.{Either, Left, Right}

case class Schema(id: IRI,
                  prefixes: Option[PrefixMap],
                  base: Option[IRI],
                  startActs: Option[List[SemAct]],
                  start: Option[ShapeExpr],
                  shapes: Option[List[ShapeExpr]],
                  tripleExprMap: Option[Map[ShapeLabel,TripleExpr]],
                  imports: List[IRI]
                 ) {

  def addShape(se: ShapeExpr): Schema = this.copy(shapes = addToOptionList(se,shapes))

  private def addToOptionList[A](x: A, maybeLs: Option[List[A]]): Option[List[A]] = maybeLs match {
    case None => Some(List(x))
    case Some(xs) => Some(x :: xs)
  }

  def getTripleExpr(lbl: ShapeLabel): Option[TripleExpr] =
    tripleExprMap.map(_.get(lbl)).getOrElse(None)

  def resolveShapeLabel(l: ShapeLabel): Either[String, IRI] = l match {
    case IRILabel(iri) => Right(iri)
    case _ => Left(s"Label $l can't be converted to IRI")
  }

  lazy val prefixMap: PrefixMap =
    prefixes.getOrElse(PrefixMap.empty)

  def getTripleExprMap(): Map[ShapeLabel, TripleExpr] =
    tripleExprMap.getOrElse(Map())

  lazy val shapesMap: Map[ShapeLabel,ShapeExpr] = {
    shapes match {
      case None => Map()
      case Some(ls) => {
        ls.collect{ case s if s.id.isDefined => (s.id.get, s)}.toMap
      }
    }
  }

  lazy val resolvedShapesMap: Either[String,Map[ShapeLabel,ShapeExpr]] = {
    closureImports(imports, List(id), shapesMap)
  }

  private def closureImports(imports: List[IRI],
                             visited: List[IRI],
                             current: Map[ShapeLabel,ShapeExpr]
                            ): Either[String, Map[ShapeLabel,ShapeExpr]] = imports match {
    case Nil => Right(current)
    case (i::is) => if (visited contains i) closureImports(is,visited,current)
    else {
      expand(i, current).fold(
        e => Left(e),
        newShapeMap => closureImports(is, i :: visited, newShapeMap))
    }
  }

  private def expand(i: IRI, current: Map[ShapeLabel,ShapeExpr]): Either[String, Map[ShapeLabel,ShapeExpr]] = {
    Schema.fromIRI(i).fold(e => Left(e),
      schema => {
        Right(schema.shapesMap ++ current)
      }
    )
  }

  def qualify(node: RDFNode): String =
    prefixMap.qualify(node)

  def qualify(label: ShapeLabel): String =
    prefixMap.qualify(label.toRDFNode)

  // TODO: Convert to Either[String,ShapeExpr]
  def getShape(label: ShapeLabel): Option[ShapeExpr] =
    shapesMap.get(label)

  lazy val shapeList = shapes.getOrElse(List())

  def labels: List[ShapeLabel] = {
    shapeList.map(_.id).flatten
  }

  def addTripleExprMap(te: Map[ShapeLabel,TripleExpr]): Schema =
    this.copy(tripleExprMap = Some(te))

  def negCycles: Either[String, Set[Set[ShapeLabel]]] =
    Dependencies.negCycles(this)

  def depGraph: Either[String, DepGraph[ShapeLabel]] =
    Dependencies.depGraph(this)

}


object Schema {

  def rdfDataFormats(rdfReader: RDFReader) = rdfReader.availableParseFormats.map(_.toUpperCase) //   RDFAsJenaModel.availableFormats.map(_.toUpperCase)

  def empty: Schema =
    Schema(IRI(""),None, None, None, None, None, None, List())

  def fromIRI(i: IRI): Either[String, Schema] = {
    val str = Source.fromURI(i.uri).mkString
    fromString(str,"ShExC",None)
  }

  /**
  * Reads a Schema from a char sequence
    * @param cs char sequence
    * @param format syntax format
    * @param base base URL
    * @param rdfReader RDFReader value from which to obtain RDF data formats (in case of RDF format)
    * @return either a Schema or a String message error
    */
  def fromString(cs: CharSequence,
                 format: String,
                 base: Option[String],
                 maybeRDFReader: Option[RDFReader] = None
                ): Either[String, Schema] = {
    val formatUpperCase = format.toUpperCase
    formatUpperCase match {
      case "SHEXC" => {
        import compact.Parser.parseSchema
        parseSchema(cs.toString, base)
      }
      case "SHEXJ" => {
        import io.circe.parser._
        import es.weso.shex.implicits.decoderShEx._
        decode[Schema](cs.toString).leftMap(_.getMessage)
      }
      case _ => maybeRDFReader match {
        case None => Left(s"Not implemented ShEx parser for format $format and no rdfReader provided")
        case Some(rdfReader) =>
         if (rdfDataFormats(rdfReader).contains(formatUpperCase)) for {
          rdf    <- rdfReader.fromString(cs, formatUpperCase, base)
          schema <- RDF2ShEx.rdf2Schema(rdf)
         } yield schema
         else Left(s"Not implemented ShEx parser for format $format")
       }
    }
  }
  def serialize(schema: Schema,
                format: String,
                rdfBuilder: RDFBuilder): Either[String,String] = {
    val formatUpperCase = format.toUpperCase
    formatUpperCase match {
      case "SHEXC" => {
        import compact.CompactShow._
        Right(showSchema(schema))
      }
      case "SHEXJ" => {
        import io.circe.syntax._
        import es.weso.shex.implicits.encoderShEx._
        Right(schema.asJson.spaces2)
      }
      case _ if (rdfDataFormats(rdfBuilder).contains(formatUpperCase)) => {
        val rdf = ShEx2RDF(schema, None, rdfBuilder.empty)
        rdf.serialize(formatUpperCase)
      }
      case _ =>
        Left(s"Not implemented conversion to $format. Schema: $schema")
    }
  }
}
