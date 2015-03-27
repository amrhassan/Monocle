package monocle

import monocle.function._
import monocle.std._
import org.specs2.scalaz.Spec

/**
 * Show how could we use Optics to manipulate some Json AST
 */
class JsonExample extends Spec {

  sealed trait Json

  case class JsString(s: String) extends Json
  case class JsNumber(n: Int) extends Json
  case class JsArray(l: List[Json]) extends Json
  case class JsObject(m: Map[String, Json]) extends Json

  val jsString = Prism[Json, String]{ case JsString(s) => Some(s); case _ => None}(JsString.apply)
  val jsNumber = Prism[Json, Int]{ case JsNumber(n) => Some(n); case _ => None}(JsNumber.apply)
  val jsArray  = Prism[Json, List[Json]]{ case JsArray(a) => Some(a); case _ => None}(JsArray.apply)
  val jsObject = Prism[Json, Map[String, Json]]{ case JsObject(m) => Some(m); case _ => None}(JsObject.apply)

  val json = JsObject(Map(
    "first_name" -> JsString("John"),
    "last_name"  -> JsString("Doe"),
    "age"        -> JsNumber(28),
    "siblings"   -> JsArray(List(
      JsObject(Map(
        "first_name" -> JsString("Elia"),
        "age"        -> JsNumber(23)
      )),
      JsObject(Map(
        "first_name" -> JsString("Robert"),
        "age"        -> JsNumber(25)
      ))
    ))
  ))

  "Json Prism" in {
    jsNumber.getOption(JsString("plop")) ==== None
    jsNumber.getOption(JsNumber(2))      ==== Some(2)
  }

  "Use index to go into an JsObject or JsArray" in {
    (jsObject composeOptional index("age") composePrism jsNumber).getOption(json) ==== Some(28)

    (jsObject composeOptional index("siblings")
              composePrism    jsArray
              composeOptional index(1)
              composePrism    jsObject
              composeOptional index("first_name")
              composePrism    jsString
    ).set("Robert Jr.")(json) ==== JsObject(Map(
      "first_name" -> JsString("John"),
      "last_name"  -> JsString("Doe"),
      "age"        -> JsNumber(28),
      "siblings"   -> JsArray(List(
        JsObject(Map(
          "first_name" -> JsString("Elia"),
          "age"        -> JsNumber(23)
        )),
        JsObject(Map(
          "first_name" -> JsString("Robert Jr."), // name is updated
          "age"        -> JsNumber(25)
        ))
      ))
    ))
  }

  "Use at to add delete fields" in {
    (jsObject composeLens at("nick_name")).set(Some(JsString("Jojo")))(json) ==== JsObject(Map(
      "first_name" -> JsString("John"),
      "nick_name"  -> JsString("Jojo"), // new field
      "last_name"  -> JsString("Doe"),
      "age"        -> JsNumber(28),
      "siblings"   -> JsArray(List(
        JsObject(Map(
          "first_name" -> JsString("Elia"),
          "age"        -> JsNumber(23)
        )),
        JsObject(Map(
          "first_name" -> JsString("Robert"),
          "age"        -> JsNumber(25)
        ))
      ))
    ))

    (jsObject composeLens at("age")).set(None)(json) ==== JsObject(Map(
      "first_name" -> JsString("John"),
      "last_name"  -> JsString("Doe"), // John is ageless now
      "siblings"   -> JsArray(List(
        JsObject(Map(
          "first_name" -> JsString("Elia"),
          "age"        -> JsNumber(23)
        )),
        JsObject(Map(
          "first_name" -> JsString("Robert"),
          "age"        -> JsNumber(25)
        ))
      ))
    ))
  }

  "Use each and filterIndex to modify several fields at a time" in {
    (jsObject composeTraversal filterIndex((_: String).contains("name"))
              composePrism     jsString
              composeOptional  headOption
    ).modify(_.toLower)(json) ==== JsObject(Map(
      "first_name" -> JsString("john"), // starts with lower case
      "last_name"  -> JsString("doe"),  // starts with lower case
      "age"        -> JsNumber(28),
      "siblings"   -> JsArray(List(
        JsObject(Map(
          "first_name" -> JsString("Elia"),
          "age"        -> JsNumber(23)
        )),
        JsObject(Map(
          "first_name" -> JsString("Robert"),
          "age"        -> JsNumber(25)
        ))
      ))
    ))


    (jsObject composeOptional  index("siblings")
              composePrism     jsArray
              composeTraversal each
              composePrism     jsObject
              composeOptional  index("age")
              composePrism     jsNumber
    ).modify(_ + 1)(json) ==== JsObject(Map(
      "first_name" -> JsString("John"),
      "last_name"  -> JsString("Doe"),
      "age"        -> JsNumber(28),
      "siblings"   -> JsArray(List(
        JsObject(Map(
          "first_name" -> JsString("Elia"),
          "age"        -> JsNumber(24)    // Elia is older
        )),
        JsObject(Map(
          "first_name" -> JsString("Robert"),
          "age"        -> JsNumber(26)    // Robert is older
        ))
      ))
    ))
  }

}
