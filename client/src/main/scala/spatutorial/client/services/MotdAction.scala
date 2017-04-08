package spatutorial.client.services

import cats.Monad
import cats.implicits._
import japgolly.scalajs.react.CatsReact.ReactS
import japgolly.scalajs.react.CatsReact.ReactST
import spatutorial.client.services.Processing.Reading

import scala.language.higherKinds

class MotdAction[M[_] : Monad](loadMotd: => M[String]) {
  type MotdState = State[String]
  val MotdReactState: ReactS.FixT[M, MotdState] = ReactS.FixT[M, MotdState]

  val UpdateMotd: ReactST[M, MotdState, Unit] = {
    val loading = Reading()
    for {
      _ <- MotdReactState.modT(_.withProcessing(loading))
      text <- loadMotd
      _ <- MotdReactState.modT(_.withoutProcessing(loading).withValue(text))
    } yield ()
  }
}
