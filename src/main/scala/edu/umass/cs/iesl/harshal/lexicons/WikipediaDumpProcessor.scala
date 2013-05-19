package edu.umass.cs.iesl.harshal.lexicons

import xml.pull._
import org.apache.commons.lang3.StringEscapeUtils
import xml.pull.EvElemStart
import xml.pull.EvText
import xml.pull.EvElemEnd
import java.net.URLEncoder

/**
 * @author harshal
 * @date: 5/17/13
 */

case class Anchor(text:String,linksTo:String, start:Int, context:Context)

case class Context(left:String,right:String)

case class WikipediaPage(title:String,
                         id:Int,
                         text:Option[String],
                         isRedirect:Boolean,
                         redirect:Option[String]) {

  def getAnchorsFromText : Iterator[Anchor] = {
    import Regexes._
    text match {
      case Some(t) => {
          Matcher1.findAllIn(t).matchData.flatMap(m=>{
            identifyValidAnchor(t,m.matched,m.start)
          })
      }
      case None => Iterator.empty
    }
  }

  private def getAnchorWithContext(text:String,anchorText:String,linksTo:String,offset:Int):Anchor = {
    import Regexes._

    def split(txt:String)={
      val s = txt.split("""\|""")
      s(s.length-1)
    }

    val leftOffset = if(offset>=200) offset-200 else offset
    val rightOffset = if (text.length<offset+200) text.length else offset+200
    val leftCover = text.substring(leftOffset,offset)
    val temp = text.substring(offset,rightOffset)
    val rightCover =
      Matcher1.findFirstIn(temp) match {
        case Some(x) => text.substring(offset+x.length,rightOffset)
        case None => temp
      }

    val s1 = try{
      Matcher1.replaceAllIn(leftCover,r=>(split(r.group(1)))).replaceAll(Matcher7," ").split("""\s+""")
    }catch{
      case e:Exception=> leftCover.replaceAll(Matcher7," ").split("""\s+""")
    }
    val s2 = try{
      Matcher1.replaceAllIn(rightCover,r=>(split(r.group(1)))).replaceAll(Matcher7," ").split("""\s+""")
    }catch{
      case e:Exception=> rightCover.replaceAll(Matcher7," ").split("""\s+""")
    }

    val left = if(s1.length>=30) s1.takeRight(30).mkString(" ") else s1.mkString(" ")
    val right = if(s2.length>=30) s2.take(30).mkString(" ") else s2.mkString(" ")
    Anchor(anchorText,linksTo,offset,Context(left,right))
  }

  private def identifyValidAnchor(t:String,m:String,s:Int)={
    import Regexes._
    m match{
      case Matcher5(_) => None
      case Matcher3(anchorText) => {
        Some(getAnchorWithContext(t,anchorText,anchorText,s))
      }
      case Matcher4(anchorText,linksTo) => {
        Some(getAnchorWithContext(t,anchorText,linksTo,s))
      }
      case Matcher2(anchorText,linksTo) => {
        Some(getAnchorWithContext(t,anchorText,linksTo,s))
      }
      case Matcher1(anchorText) => {
        Some(getAnchorWithContext(t,anchorText,anchorText,s))
      }
      case _ => None
    }
  }

  lazy val url = {
    val encoded = URLEncoder.encode(title.replaceAll("""\s""","_"),"UTF-8")
    "http://en.wikipedia.org/wiki/"+encoded
  }
}

class WikipediaDumpProcessor(file:String) extends Iterator[WikipediaPage]{
  import Tags._

  val reader = new XMLEventReader(io.Source.fromFile(file,"UTF-8"))

  var foundPage = false

  override def hasNext = {
    while(reader.hasNext && !foundPage) {
      reader.next() match {
        case EvElemStart(_, PAGE, _, _) => foundPage = true
        case _ =>
      }
    }
    foundPage
  }

  override def next() = {
    foundPage=false
    if (hasNext) processPage(reader) else Iterator.empty.next()
  }

  private def processPage(parser : XMLEventReader):WikipediaPage={
    var newId = true
    var title = ""
    var id = ""
    var text = ""
    var redirect = ""
    var isRedirect = false
    var done = false
    while(parser.hasNext & !done){
      parser.next() match{
        case EvElemStart(_, TITLE, _, _) => title = getText(parser,"title")
        case EvElemStart(_, REDIRECT, attr, _) => {
          isRedirect = true
          redirect = StringEscapeUtils.unescapeXml(attr.get("title").get.head.toString)
        }
        case EvElemStart(_, ID, _, _) => if (newId) {
          id = getText(parser, ID)
          newId = false
        }
        case EvElemStart(_, TEXT, _, _) => {
          text = getText(parser,TEXT)
        }
        case EvElemEnd(_, "page") => {
          done = true
        }
        case _ =>
      }
    }
    if (isRedirect)
      WikipediaPage(title,id.toInt,None,isRedirect,Some(redirect))
    else
      WikipediaPage(title,id.toInt,Some(text),isRedirect,None)
  }

  private def getText( parser : XMLEventReader, inTag : String ) : String =
  {
    val fullText = new StringBuffer()
    var done = false
    while ( parser.hasNext && !done )
    {
      parser.next match
      {
        case EvElemEnd(_, tagName ) =>
        {
          assert( tagName.equalsIgnoreCase(inTag) )
          done = true
        }
        case EvText( text ) =>
        {
          fullText.append( text )
        }
        case EvEntityRef(text) =>
        {
          fullText.append( StringEscapeUtils.unescapeXml("&"+text+";") )
        }
        case _ =>
      }
    }
    fullText.toString()
  }
}

object Tags{
  val PAGE = "page"
  val TITLE = "title"
  val ID = "id"
  val REDIRECT = "redirect"
  val TEXT = "text"
}

object Regexes {
  val Matcher1 = """\[\[(.+?)\]\]""".r
  val Matcher2 = """\[\[(.+?)\|(.+?)\]\]""".r
  val Matcher3 = """\[\[File:.*?\[\[(.+?)\]\]""".r
  val Matcher4 = """\[\[File:.*?\[\[(.+?)\|(.+?)\]\]""".r
  val Matcher5 = """\[\[(en|de|fr|nl|it|es|pl|ru|ja|pt|zh|sv|vi|uk|ca|no|fi|cs|fa|hu|ko|ro|id|ar|tr|sk|kk|eo|da|sr|lt|eu|ms|he|bg|sl|vo|hr|war|hi|et|gl|nn|az|simple|la|el|th|sh|oc|new|mk|ka|roa-rup|tl|pms|ht|be|te|uz|ta|be-x-old|lv|br|sq|ceb|jv|mg|cy|mr|lb|is|bs|hy|my|yo|an|lmo|ml|fy|pnb|bpy|af|sw|bn|io|ne|gu|zh-yue|scn|ur|ba|nds|ku|ast|qu|su|diq|tt|ga|ky|cv|ia|nap|bat-smg|map-bms|als|wa|kn|am|ckb|sco|gd|bug|tg|mzn|zh-min-nan|yi|vec|hif|arz|roa-tara|nah|os|sah|mn|sa|pam|hsb|li|mi|si|se|co|gan|glk|bar|fo|ilo|bo|bcl|mrj|fiu-vro|nds-nl|ps|tk|vls|gv|rue|pa|xmf|dv|pag|nrm|zea|kv|koi|km|rm|csb|lad|udm|or|mhr|mt|fur|lij|wuu|ug|pi|sc|frr|zh-classical|bh|nov|ksh|ang|so|stq|kw|nv|hak|vep|ay|frp|pcd|ext|szl|gag|gn|ln|ie|haw|eml|xal|pfl|pdc|rw|krc|crh|ace|to|as|ce|kl|arc|dsb|myv|bjn|pap|sn|tpi|lbe|lez|kab|mdf|wo|jbo|av|srn|cbk-zam|bxr|ty|lo|kbd|ab|tet|mwl|ltg|na|kg|ig|nso|za|kaa|zu|rmy|chy|cu|tn|chr|got|cdo|sm|bi|mo|bm|iu|pih|ss|sd|pnt|ee|om|ha|ki|ti|ts|ks|sg|ve|rn|cr|ak|tum|lg|dz|ny|ik|ch|ff|st|fj|tw|xh|ng|ii|cho|mh|aa|kj|ho|mus|kr|hz):""".r
  val Matcher7 = """(\{\{.*(?=(\}\}))[\}]*)|(\{\{.*)|([^\{\{]*(?=(\}\}))[\}]*)"""
  val Matcher8 = """[^\p{L}0-9\s,\.]"""
}