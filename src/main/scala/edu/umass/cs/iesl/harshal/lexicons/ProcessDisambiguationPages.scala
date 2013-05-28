package edu.umass.cs.iesl.harshal.lexicons

import collection.immutable.{HashMap, HashSet}
import collection.mutable.ArrayBuffer
import java.io.{File, FileOutputStream, OutputStreamWriter}
import java.util.Properties

/**
 * @author harshal
 * @date: 5/22/13
 */
object ProcessDisambiguationPages {

  def writeToFile(lexicons:ArrayBuffer[ArrayBuffer[String]],files:Seq[String]){
    lexicons.zip(files) foreach { case (lexicon,file) => {
      val writer = new OutputStreamWriter(new FileOutputStream(file),"UTF-8")
      for (word <- lexicon){
        writer.write(word+"\n")
      }
      writer.close()
    }}
  }

  def main(args:Array[String])={

//    val titleWithDisambiguation = """(.+?)\s\(disambiguation\)""".r

    val disambiguation = "(disambiguation)"

    val prop = new Properties()
    prop.load(this.getClass.getResourceAsStream("/config.properties"))

    val wp = new WikipediaDumpProcessor(prop.getProperty("wikipediaDump"))

    val WpLinkFinder = """\[\[(.+?)\]\]""".r

    val files = new File("/iesl/canvas/harshal/data/freebase/lexicons").listFiles()

    val types = (for( f <- files ) yield {
      io.Source.fromFile(f,"UTF-8").getLines().foldLeft(HashSet[String]())((set,line) => {
        set+line
      })
    }).toIndexedSeq

    val lexicons = ArrayBuffer[ArrayBuffer[String]]()

    for(_ <- 1 to files.length){
      lexicons+=ArrayBuffer[String]()
    }

    while (wp.hasNext){
      val page = wp.next()
      if (page.title.endsWith(disambiguation)){
        page.text match{
          case Some(text) => {
            val links = WpLinkFinder.findAllIn(text).matchData
            while(links.hasNext){
              val title = links.next().group(1)
              if (title.toLowerCase.indexOf(page.title.toLowerCase()) != -1){
                val indices = types.zipWithIndex.filter{ case (set,i) => set(title) }.map(_._2)
                indices.foreach(lexicons(_)+=page.title)
              }
            }
          }
          case _ =>
        }
      }
    }
    writeToFile(lexicons,files.map(f=>"/iesl/canvas/harshal/data/freebase/lexicons/"+f.getName.split("""\.""")(0)+"-disambiguation.txt"))
  }

}
