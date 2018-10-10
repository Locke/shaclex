package es.weso.shex.implicits
import cats._
import cats.implicits._
import es.weso.rdf.nodes._
import es.weso.shex._
import es.weso.rdf._
import compact.CompactShow
import es.weso.rdf.operations.Comparisons._


object showShEx {

  implicit lazy val showSchema: Show[Schema] = new Show[Schema] {
    final def show(s: Schema): String =
      CompactShow.showSchema(s) /*{
      s"Schema(${optShow(s.prefixes)}, ${optShow(s.base)}, ${optShow(s.startActs)}, ${optShow(s.start)}, ${optShow(s.shapes)})"
    } */
  }

  implicit lazy val showPrefixMap: Show[PrefixMap] = new Show[PrefixMap] {
    final def show(pm: PrefixMap): String = {
      s"PrefixMap($pm)"
    }
  }

  implicit lazy val showShapeExpr: Show[ShapeExpr] = new Show[ShapeExpr] {
    final def show(a: ShapeExpr): String = a match {
      case ShapeOr(id, shapes) => s"Or(${optShow(id)}, ${shapes.map(_.show).mkString(",")})"
      case ShapeAnd(id, shapes) => s"And(${optShow(id)}, ${shapes.map(_.show).mkString(",")})"
      case ShapeNot(id, shape) => s"Not(${optShow(id)}, ${shape.show})"
      case s: Shape => s.show
      case nc: NodeConstraint => nc.show
      case ShapeRef(r) => s"ShapeRef(${r.show})"
      case ShapeExternal(id) => s"ShapeExternal(${optShow(id)}"
    }
  }

  implicit lazy val showShape: Show[Shape] = new Show[Shape] {
    final def show(a: Shape): String = a match {
      case Shape(None,None,None,None,None,None,None,None) => "."
      case _ => s"Shape(${optShow(a.id)}, ${optShow(a.virtual)}, ${optShow(a.closed)}, ${optShow(a.extra)}, ${optShow(a.expression)}, ${optShow(a._extends)}, ${optShow(a.semActs)})"
    }
  }

  implicit lazy val showNodeConstraint: Show[NodeConstraint] = new Show[NodeConstraint] {
    final def show(a: NodeConstraint): String =
      s"NodeConstraint(${optShow(a.id)}, ${optShow(a.nodeKind)}, ${optShow(a.datatype)}, ${a.xsFacets.show}, ${optShow(a.values)})"
  }

  implicit lazy val showNodeKind: Show[NodeKind] = new Show[NodeKind] {
    final def show(a: NodeKind): String = a match {
      case IRIKind => "iri"
      case BNodeKind => "bnode"
      case NonLiteralKind => "nonLiteral"
      case LiteralKind => "literal"
    }
  }

  implicit lazy val showValueSetValue: Show[ValueSetValue] = new Show[ValueSetValue] {
    final def show(a: ValueSetValue): String = a match {
      case IRIValue(iri) => iri.show
      case StringValue(s) => "\"" + s + "\""
      case DatatypeString(s, d) => "\"" + s + "\"^^" + d.show
      case LangString(s, l) => "\"" + s + "\"@" + l
      case IRIStem(s) => s"stem($s)"
      case IRIStemRange(s, exclusions) => s"${s.show}~ ${optShow(exclusions)})"
      case LanguageStem(stem) => s"@$stem~ "
      case LanguageStemRange(lang,exclusions) => s"@${lang}~ ${optShow(exclusions)}"
      case LiteralStem(stem) => s"@$stem~ "
      case LiteralStemRange(stem,exclusions) => s"@${stem}~ ${optShow(exclusions)}"
      case Language(lang) => s"@$lang"
      case _ => s"Unimplemented show of $a"
    }
  }

  implicit lazy val showIRIExclusion: Show[IRIExclusion] = new Show[IRIExclusion] {
    final def show(a: IRIExclusion): String = a match {
      case IRIRefExclusion(i) => i.show
      case IRIStemExclusion(stem) => stem.show
    }
  }

  implicit lazy val showIRIStem: Show[IRIStem] = new Show[IRIStem] {
    final def show(a: IRIStem): String = a.stem.show + "~"
  }

  implicit lazy val showLiteralExclusion: Show[LiteralExclusion] = new Show[LiteralExclusion] {
    final def show(a: LiteralExclusion): String = a match {
      case LiteralStringExclusion(str) => str.show
      case LiteralStemExclusion(stem) => stem.show
    }
  }

  implicit lazy val showLiteralStem: Show[LiteralStem] = new Show[LiteralStem] {
    final def show(a: LiteralStem): String = a.stem.show + "~"
  }

  implicit lazy val showLanguageExclusion: Show[LanguageExclusion] = new Show[LanguageExclusion] {
    final def show(a: LanguageExclusion): String = a match {
      case LanguageTagExclusion(lang) => lang.show
      case LanguageStemExclusion(stem) => stem.show
    }
  }

  implicit lazy val showLanguageStem: Show[LanguageStem] = new Show[LanguageStem] {
    final def show(a: LanguageStem): String = a.stem.show + "~"
  }

  implicit lazy val showLang: Show[Lang] = new Show[Lang] {
    final def show(a: Lang): String = a.lang.show
  }

  implicit lazy val showStemValue: Show[IRIStemRangeValue] = new Show[IRIStemRangeValue] {
    final def show(a: IRIStemRangeValue): String = a match {
      case IRIStemValueIRI(i) => i.show
      case IRIStemWildcard() => "*"
    }
  }

  implicit lazy val showMax = new Show[Max] {
    final def show(a: Max): String = a match {
      case Star => "*"
      case IntMax(n) => n.show
    }
  }

  // TODO: It should qualify with schema's prefixMap
  implicit lazy val showIRI: Show[IRI] = new Show[IRI] {
    final def show(iri: IRI): String =
      iri.toString
  }

  implicit lazy val showPrefix: Show[Prefix] = new Show[Prefix] {
    final def show(p: Prefix): String = p.str
  }

  implicit lazy val showSemAct: Show[SemAct] = new Show[SemAct] {
    final def show(a: SemAct): String =
      "SemAct(" + a.name.show + "," + optShow(a.code) + ")"
  }

  implicit lazy val showXsFacet: Show[XsFacet] = new Show[XsFacet] {
    final def show(a: XsFacet): String = a match {
      case Length(v) => s"${a.fieldName}(${v.show})"
      case MinLength(v) => s"${a.fieldName}(${v.show})"
      case MaxLength(v) => s"${a.fieldName}(${v.show})"
      case Pattern(v, flags) => s"${a.fieldName}(${v.show},${flags.getOrElse("").show})"
      case MinInclusive(n) => s"${a.fieldName}(${n.show})"
      case MaxInclusive(n) => s"${a.fieldName}(${n.show})"
      case MinExclusive(n) => s"${a.fieldName}(${n.show})"
      case MaxExclusive(n) => s"${a.fieldName}(${n.show})"
      case TotalDigits(n) => s"${a.fieldName}(${n.show})"
      case FractionDigits(n) => s"${a.fieldName}(${n.show})"
    }
  }

  implicit lazy val showNumericLiteral: Show[NumericLiteral] = new Show[NumericLiteral] {
    final def show(a: NumericLiteral): String = a match {
      case NumericInt(n,repr) => repr
      case NumericDouble(n,repr) => repr
      case NumericDecimal(n,repr) => repr
    }
  }

  implicit lazy val showTripleExpr: Show[TripleExpr] = new Show[TripleExpr] {
    final def show(a: TripleExpr): String = a match {
      case e: EachOf => e.show
      case e: OneOf => e.show
      case Inclusion(i) => s"Inclusion(${i.show})"
      case tc: TripleConstraint => tc.show
    }
  }

  implicit lazy val showEachOf: Show[EachOf] = new Show[EachOf] {
    final def show(a: EachOf): String =
      s"EachOf(${optShow(a.id)}, ${a.expressions.show}, ${optShow(a.optMin)}, ${optShow(a.optMax)}, ${optShow(a.semActs)}, ${optShow(a.annotations)})"
  }

  implicit lazy val showOneOf: Show[OneOf] = new Show[OneOf] {
    final def show(a: OneOf): String =
      s"OneOf(${optShow(a.id)},${a.expressions.show}, ${optShow(a.optMin)}, ${optShow(a.optMax)}, ${optShow(a.semActs)}, ${optShow(a.annotations)})"
  }

  implicit lazy val showTripleConstraint: Show[TripleConstraint] = new Show[TripleConstraint] {
    final def show(a: TripleConstraint): String =
      s"TripleConstraint(${optShow(a.id)}, ${optShow(a.optInverse)}, ${optShow(a.optNegated)}, ${a.predicate.show}, ${a.valueExpr.show}, ${optShow(a.optMin)}, ${optShow(a.optMax)}, ${optShow(a.semActs)}, ${optShow(a.annotations)})"
  }

  implicit lazy val showAnnotation: Show[Annotation] = new Show[Annotation] {
    final def show(a: Annotation): String =
      s"Annotation(${a.predicate.show}, ${a.obj.show})"
  }

  implicit lazy val showObjectValue: Show[ObjectValue] = new Show[ObjectValue] {
    final def show(a: ObjectValue): String = a match {
      case IRIValue(i) => i.show
      case StringValue(s) => "\"" + s + "\""
      case DatatypeString(s, iri) => "\"" + s + "\"^^" + iri.show
      case LangString(s, l) => "\"" + s + "\"@" + l
    }
  }

  implicit lazy val showShapeLabel: Show[ShapeLabel] = new Show[ShapeLabel] {
    final def show(a: ShapeLabel): String = a match {
      case IRILabel(iri) => iri.show
      case BNodeLabel(bnode) => "_:" + bnode.id
    }
  }

  implicit lazy val showObjectLiteral: Show[ObjectLiteral] = new Show[ObjectLiteral] {
    final def show(a: ObjectLiteral): String = a match {
      case StringValue(s) => "\"" + s + "\""
      case DatatypeString(s,iri) => "\"" + s + "\"^^" + iri.show
      case LangString(s,lang) => "\"" + s + "\"@" + lang
    }
  }

  def optShow[A: Show](m: Option[A]): String =
    m match {
      case None => "-"
      case Some(v) => v.show
    }

}
