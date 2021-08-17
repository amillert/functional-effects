package net.degoes.zio

import zio._
import java.sql.Time

/*
 * INTRODUCTION
 *
 * ZIO effects model failure, in a way similar to the Scala data types `Try`
 * and `Either`. Unlike exceptions, ZIO effects are statically-typed, allowing
 * you to determine if and how effects fail by looking at type signatures.
 *
 * ZIO effects have a large number of error-related operators to transform
 * and combine effects. Some of these "catch" errors, while others transform
 * them, and still others combine potentially failing effects with fallback
 * effects.
 *
 * In this section, you will learn about all these operators, as well as the
 * rich underlying model of errors that ZIO uses internally.
 */

object ErrorConstructor extends App {
  import zio.console._

  /**
   * EXERCISE
   *
   * Using `ZIO.fail`, construct an effect that models failure with any
   * string value, such as "Uh oh!". Explain the type signature of the
   * effect.
   */
  val failed: ZIO[Any, String, Nothing] =
    ZIO.fail("Uh oh!")

  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    failed.foldM(putStrLn(_), putStrLn(_)).as(ExitCode.success)
}

object ErrorRecoveryOrElse extends App {
  import zio.console._

  val failed = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#orElse` have the `run` function compose the preceding `failed`
   * effect with another effect that succeeds with a success exit code.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    failed.orElse(ZIO.succeed(ExitCode.success))
}

object ErrorShortCircuit extends App {
  import zio.console._

  val failed: ZIO[Console, String, Unit] =
    putStrLn("About to fail...") *>
      ZIO.fail("Uh oh!") *>
      putStrLn("This will NEVER be printed!")

  /**
   * EXERCISE
   *
   * Using `ZIO#orElse`, compose the `failed` effect with another effect that
   * succeeds with an exit code.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    failed.ignore *> ZIO.succeed(ExitCode.success)
}

object ErrorRecoveryFold extends App {
  import zio.console._

  val failed = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#fold`, map both failure and success values of `failed` into
   * exit codes.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    failed.fold(_ => ExitCode.failure, _ => ExitCode.success)
}

object ErrorRecoveryCatchAll extends App {
  import zio.console._

  val failed = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#catchAll`, catch all errors in `failed` and print them out to
   * the console using `putStrLn`.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    failed.catchAll(putStrLn(_).as(ExitCode.failure))
  // failed.catchAll(putStrLn(_) *> ZIO.succeed(ExitCode.failure))
}

object ErrorRecoveryFoldM extends App {
  import zio.console._

  val failed: ZIO[Any, String, String] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#foldM`, print out the success or failure value of `failed`
   * by using `putStrLn`.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    failed.foldM(putStrLn(_).as(ExitCode.failure), putStrLn(_).as(ExitCode.success))
}

object ErrorRecoveryEither extends App {
  import zio.console._

  val failed: ZIO[Any, String, Int] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#either`, surface the error of `failed` into the success
   * channel, and then map the `Either[String, Int]` into an exit code.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    failed.either.map(_.fold(_ => ExitCode.failure, _ => ExitCode.success))
}

object ErrorRecoveryIgnore extends App {
  import zio.console._

  val failed: ZIO[Any, String, Int] = ZIO.fail("Uh oh!")

  /**
   * EXERCISE
   *
   * Using `ZIO#ignore`, simply ignore the failure of `failed`, and then map
   * the resulting unit into a successful exit code.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    failed.ignore.map(_ => ExitCode.success)
}

object ErrorNarrowing extends App {
  import java.io.IOException
  import scala.io.StdIn.readLine

  val broadReadLine: IO[Throwable, String] = ZIO.effect(scala.io.StdIn.readLine())

  /**
   * EXERCISE
   *
   * Using `ZIO#refineToOrDie`, narrow the error type of `broadReadLine` into
   * an `IOException`:
   */
  val myReadLine: IO[IOException, String] =
    broadReadLine.refineToOrDie

  def myPrintLn(line: String): UIO[Unit] = UIO(println(line))

  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    (for {
      _    <- myPrintLn("What is your name?")
      name <- myReadLine
      _    <- myPrintLn(s"Good to meet you, ${name}")
    } yield ()).exitCode
}

object AlarmApp extends App {
  import zio.console._
  import zio.duration._
  import java.io.IOException
  import java.util.concurrent.TimeUnit

  /**
   * EXERCISE
   *
   * Create an effect that will get a `Duration` from the user, by prompting
   * the user to enter a decimal number of seconds. Use `refineToOrDie` to
   * narrow the error type as necessary.
   */
  lazy val getAlarmDuration: ZIO[Console, IOException, Duration] = {
    def parseDuration(input: String): IO[NumberFormatException, Duration] =
      ZIO.effect(scala.io.StdIn.readInt()).refineToOrDie.map(x => Duration(x, TimeUnit.HOURS))

    def fallback(input: String): ZIO[Console, IOException, Duration] =
      ZIO.succeed(Duration(input.toInt, TimeUnit.HOURS))

    for {
      _        <- putStrLn("Please enter the number of seconds to sleep: ")
      input    <- getStrLn
      duration <- parseDuration(input) orElse fallback(input)
    } yield duration
  }

  /**
   * EXERCISE
   *
   * Create a program that asks the user for a number of seconds to sleep,
   * sleeps the specified number of seconds using `ZIO.sleep(d)`, and then
   * prints out a wakeup alarm message, like "Time to wakeup!!!".
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    (for {
      _ <- getAlarmDuration
    } yield ()).exitCode
}

object SequentialCause extends App {
  import zio.console._

  val failed1 = Cause.fail("Uh oh 1")
  val failed2 = Cause.fail("Uh oh 2")

  /**
   * EXERCISE
   *
   * Using `Cause.++`, form a sequential cause by composing `failed1`
   * and `failed2`.
   */
  lazy val composed = failed1 ++ failed2

  /**
   * EXERCISE
   *
   * Using `Cause.prettyPrint`, dump out `composed` to the console.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    putStrLn(composed.prettyPrint).exitCode
}

object ParalellCause extends App {
  import zio.console._

  val failed1 = Cause.fail("Uh oh 1")
  val failed2 = Cause.fail("Uh oh 2")

  /**
   * EXERCISE
   *
   * Using `Cause.&&`, form a parallel cause by composing `failed1`
   * and `failed2`.
   */
  lazy val composed = failed1 && failed2

  /**
   * EXERCISE
   *
   * Using `Cause.prettyPrint`, dump out `composed` to the console.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    putStrLn(composed.prettyPrint).exitCode
}

object Sandbox extends App {
  import zio.console._

  val failed1    = ZIO.fail("Uh oh 1")
  val failed2    = ZIO.fail("Uh oh 2")
  val finalizer1 = ZIO.fail(new Exception("Finalizing 1!")).orDie
  val finalizer2 = ZIO.fail(new Exception("Finalizing 2!")).orDie

  val composed = ZIO.uninterruptible {
    (failed1 ensuring finalizer1) zipPar (failed2 ensuring finalizer2)
  }

  /**
   * EXERCISE
   *
   * Using `ZIO#sandbox`, sandbox the `composed` effect and print out the
   * resulting `Cause` value to the console using `putStrLn`.
   */
  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    composed.sandbox
   .mapError(_.untraced).catchAll {
       case Cause.Die(_: ArithmeticException) =>
         // Caught defect: divided by zero!
         IO.unit
       case Cause.Fail(_) =>
         // Caught error: DomainError!
         IO.unit
       case cause =>
         // Caught unknown defects, shouldn't recover!
         IO.halt(cause)
     }.exitCode
}
