package es.weso.rdf.jena

import java.io.{ByteArrayInputStream, InputStream, StringReader}
import java.nio.charset.StandardCharsets

import com.typesafe.config.{Config, ConfigFactory}
import java.nio.file.Paths

import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import org.scalatest.FunSpec
import es.weso.rdf.triples.RDFTriple
import es.weso.rdf.nodes._
import es.weso.rdf.jena._
import org.apache.jena.rdf.model.ModelFactory
import es.weso.rdf._
import es.weso.rdf.PREFIXES._
import org.apache.jena.graph.Graph
import org.apache.jena.riot._
import org.apache.jena.riot.RDFLanguages.shortnameToLang
import org.apache.jena.riot.system.{IRIResolver, RiotLib, StreamRDFLib}
import org.apache.jena.sparql.core.{DatasetGraph, DatasetGraphFactory}
import org.apache.jena.sparql.graph.GraphFactory

import util._

class RDFAsJenaModelTest
  extends FunSpec
  with JenaBased
  with Matchers {

  describe("Checking base") {
    // println(s"ShaclFolder file...${shaclFolderURI}")

    it("should be able to parse RDF with relative URIs and base") {
      val emptyModel = ModelFactory.createDefaultModel
      val rdf: RDFAsJenaModel = RDFAsJenaModel(emptyModel)
      val map: Map[Prefix, IRI] = Map(Prefix("") -> IRI("http://example.org#"))
      val pm: PrefixMap = PrefixMap(map)
      rdf.addPrefixMap(pm)
      rdf.addTriples(Set(RDFTriple(
        IRI("http://example.org#a"),
        IRI("http://example.org#b"),
        IRI("c"))))

      val str =
        """|@prefix : <http://example.org#> .
                   |:a :b <c> .
                   |""".stripMargin
      RDFAsJenaModel.fromChars(str, "TURTLE", None) match {
        case Right(m2) => shouldBeIsomorphic(rdf.model, m2.model)
        case Left(e) => fail(s"Error $e\n$str")
      }

    }

    it("should be able to parse RDF with relative URIs") {
      println(s"Before creating model!!!")
      val emptyModel = ModelFactory.createDefaultModel
      val rdf: RDFAsJenaModel = RDFAsJenaModel(emptyModel)
      println(s"Current rdf: $rdf")
      val rdf1 = rdf.addTriples(Set(RDFTriple(
        IRI("a"),
        IRI("b"),
        IntegerLiteral(1))
      ))
      val str =
        """|<a> <b> 1 .
                   |""".stripMargin
      val m = ModelFactory.createDefaultModel
      println(s"Model created: $m")
      RDFAsJenaModel.fromChars(str, "TURTLE", None) match {
        case Right(m2) => shouldBeIsomorphic(rdf.model, m2.model)
        case Left(e) => fail(s"Error $e\n$str")
      }
    }
  }
}

