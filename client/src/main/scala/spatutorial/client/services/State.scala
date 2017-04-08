package spatutorial.client.services

import java.util.UUID

import spatutorial.client.services.Processing.Failure
import spatutorial.client.services.Processing.ProcessingId
import spatutorial.client.services.Processing.ProcessingType

final case class State[A](
  value: Option[A] = None,
  failures: Seq[Failure] = Seq.empty,
  processing: Set[Processing] = Set.empty
) {
  def withProcessing(p: Processing): State[A] = copy(processing = processing + p)
  def withoutProcessing(p: Processing): State[A] = copy(processing = processing - p)

  def withFailure(f: Failure): State[A] = copy(failures = failures :+ f)
  def withoutFailure: State[A] = copy(failures = Seq.empty)

  def withValue(v: A): State[A] = copy(value = Some(v))
  def foldValue(ifEmpty: A)(mod: A => A): State[A] = copy(value = Some(value.fold(ifEmpty)(mod)))

  def hasFailures: Boolean = failures.nonEmpty
  def inProcess: Boolean = processing.nonEmpty
}

object State {

  object Value {
    def unapply[A](state: State[A]): Option[A] = state.value
  }

  object Failures {
    def unapply[A](state: State[A]): Option[Seq[Failure]] = {
      if (state.failures.nonEmpty) Some(state.failures) else None
    }
  }

  object Processing {
    def unapply[A](state: State[A]): Option[Set[Processing]] = {
      if (state.failures.nonEmpty) Some(state.processing) else None
    }
  }

}

final case class Processing(id: ProcessingId, processingType: ProcessingType)

object Processing {
  type ProcessingId = UUID

  object ProcessingId {
    def newProcessingId: ProcessingId = UUID.randomUUID()
  }

  final case class Failure(failure: String)

  sealed trait ProcessingType extends Product with Serializable {
    def apply(): Processing = Processing(ProcessingId.newProcessingId, this)

    def or(p: ProcessingType): ProcessingType = Or(this, p)
    def ||(p: ProcessingType): ProcessingType = or(p)

    def and(p: ProcessingType): ProcessingType = And(this, p)
    def &&(p: ProcessingType): ProcessingType = and(p)
  }

  case object Creating extends ProcessingType

  case object Reading extends ProcessingType

  case object Updating extends ProcessingType

  case object Deleting extends ProcessingType

  final case class Or(left: ProcessingType, right: ProcessingType) extends ProcessingType

  final case class And(left: ProcessingType, right: ProcessingType) extends ProcessingType

}