package spatutorial.client.services

import cats.Monad
import cats.implicits._
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.CatsReact.ReactS
import japgolly.scalajs.react.CatsReact.ReactST
import spatutorial.client.services.Processing.Reading
import spatutorial.client.logger._

import scala.language.higherKinds

class MotdAction[S[_] : Monad, A[_]: Monad](loadMotd: => A[String]) {
  type MotdState = State[String]
  val MotdReactState: ReactS.FixT[S, MotdState] = ReactS.FixT[S, MotdState]

  def UpdateMotd(run: ReactST[S, MotdState, Unit] => Callback): Callback = {
    log.info(s"Called UpdateMotd")
    new Exception().printStackTrace()
    val loading = Reading()
    for {
      _ <- run(MotdReactState.modT(_.withProcessing(loading)))
      _ = for {
        text <- loadMotd
      } yield run(MotdReactState.modT(_.withoutProcessing(loading).withValue(text))).runNow()
    } yield ()
  }
}
