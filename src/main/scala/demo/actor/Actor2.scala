package demo.actor
import scala.actors.Actor

object Actor2 {
  case class Speak2(line: String);
  case class Gesture2(bodyPart: String, action: String);
  case class NegotiateNewContract2();
  case class ThatsAWrap2();

  def main(args: Array[String]) =
    {
      val badActor =
        Actor.actor {
          var done = false
          while (!done) {
            Actor.receive {
              case NegotiateNewContract2 =>
                System.out.println("I won't do it for less than $1 million!")
              case Speak2(line) =>
                System.out.println(line)
              case Gesture2(bodyPart, action) =>
                System.out.println("(" + action + "s " + bodyPart + ")")
              case ThatsAWrap2 =>
                System.out.println("Great cast party, everybody! See ya!")
                done = true
              case _ =>
                System.out.println("Huh? I'll be in my trailer.")
            }
          }
        }

      badActor ! NegotiateNewContract2
      badActor ! Speak2("Do ya feel lucky, punk?")
      badActor ! Gesture2("face", "grimaces")
      badActor ! Speak2("Well, do ya?")
      badActor ! ThatsAWrap2
    }
}