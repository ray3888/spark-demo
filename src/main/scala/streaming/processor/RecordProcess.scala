package streaming.emitter

/**
 * Currying all these functions.
 */
trait RecordProcess {
  def parseRecord[U: ClassManifest, T: ClassManifest](record: U)(parse: U => T): T =
    parse(record)

  def processRecord[U: ClassManifest, T: ClassManifest](record: U)(process: U => T): T =
    process(record)

  def outputRecord[U: ClassManifest](record: U)(output: U => Unit): Unit =
    output(record)
}
