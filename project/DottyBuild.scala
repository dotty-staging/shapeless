object DottyBuild {
  // copied from https://github.com/lampepfl/dotty/blob/369a4650584c090302451a1837e7f4391de9ece2/sbt-dotty/src/dotty/tools/sbtplugin/DottyPlugin.scala#L30-L82
  def latestNightlyVersion(): Option[String] = {
    import scala.io.Source

    println("Fetching latest Dotty nightly version...")

    val nightly = try {
      // get majorVersion from dotty.epfl.ch
      val source0 = Source.fromURL("https://dotty.epfl.ch/versions/latest-nightly-base")
      val majorVersionFromWebsite = source0.getLines().toSeq.head
      source0.close()

      // get latest nightly version from maven
      def fetchSource(version: String): (scala.io.BufferedSource, String) =
        try {
          val url =
            if (version.startsWith("0"))
              s"https://repo1.maven.org/maven2/ch/epfl/lamp/dotty-compiler_$version/maven-metadata.xml"
            else
              s"https://repo1.maven.org/maven2/org/scala-lang/scala3-compiler_$version/maven-metadata.xml"
          Source.fromURL(url) -> version
        }
        catch { case t: java.io.FileNotFoundException =>
          val major :: minor :: Nil = version.split('.').toList
          if (minor.toInt <= 0) throw t
          else fetchSource(s"$major.${minor.toInt - 1}")
        }
      val (source1, majorVersion) = fetchSource(majorVersionFromWebsite)
      val Version = s"      <version>($majorVersion.*-bin.*)</version>".r
      val nightly = source1
        .getLines()
        .collect { case Version(version) => version }
        .toSeq
        .lastOption
      source1.close()
      nightly
    } catch {
      case _:java.net.UnknownHostException =>
        None
    }

    nightly match {
      case Some(version) =>
        println(s"Latest Dotty nightly build version: $version")
      case None =>
        println(s"Unable to get Dotty latest nightly build version. Make sure you are connected to internet")
    }

    nightly
  }
}
