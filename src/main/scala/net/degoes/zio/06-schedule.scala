package net.degoes.zio

import zio._

object Retry {

  /**
   * EXERCISE
   *
   * Using `Schedule.recurs`, create a schedule that recurs 5 times.
   */
  val fiveTimes: Schedule[Any, Any, Int] =
    Schedule.recurs(5).map(_.toInt) // what about Schedule#as ?

  /**
   * EXERCISE
   *
   * Using the `ZIO.repeat`, repeat printing "Hello World" five times to the
   * console.
   */
  val repeated1 =
    console.putStrLn("Hello World").repeat(Schedule.recurs(4)) // 4 or 5 ?

  /**
   * EXERCISE
   *
   * Using `Schedule.spaced`, create a schedule that recurs forever every 1 second.
   */
  val everySecond =
    Schedule.spaced(duration.Duration.Finite(1000)) // some seconds syntax maybe ?

  /**
   * EXERCISE
   *
   * Using the `&&` method of the `Schedule` object, the `fiveTimes` schedule,
   * and the `everySecond` schedule, create a schedule that repeats fives times,
   * evey second.
   */
  val fiveTimesEverySecond = fiveTimes && everySecond

  /**
   * EXERCISE
   *
   * Using the `ZIO#repeat`, repeat the action putStrLn("Hi hi") using
   * `fiveTimesEverySecond`.
   */
  val repeated2 = console.putStrLn("Hi hi").repeat(fiveTimesEverySecond)

  /**
   * EXERCISE
   *
   * Using `Schedule#andThen` the `fiveTimes` schedule, and the `everySecond`
   * schedule, create a schedule that repeats fives times rapidly, and then
   * repeats every second forever.
   */
  val fiveTimesThenEverySecond = fiveTimes andThen everySecond // ?
  // val fiveTimesThenEverySecond = fiveTimes ++ everySecond

  /**
   * EXERCISE
   *
   * Using `ZIO#retry`, retry the following error a total of five times.
   */
  val error1   = IO.fail("Uh oh!")
  val retried5 = error1.retry(fiveTimes)

  /**
   * EXERCISE
   *
   * Using the `Schedule#||`, the `fiveTimes` schedule, and the `everySecond`
   * schedule, create a schedule that repeats the minimum of five times and
   * every second.
   */
  val fiveTimesOrEverySecond = fiveTimes || everySecond

  /**
   * EXERCISE
   *
   * Using `Schedule.exponential`, create an exponential schedule that starts
   * from 10 milliseconds.
   */
  val exponentialSchedule = Schedule.exponential(duration.Duration.Finite(10))

  // (effect orElse otherService).retry(exponentialSchedule).timeout(60.seconds)

  /**
   * EXERCISE
   *
   * Using `Schedule.jittered` produced a jittered version of `exponentialSchedule`.
   */
  val jitteredExponential = exponentialSchedule.jittered // hmmm

  /**
   * EXERCISE
   *
   * Using `Schedule.whileOutput`, produce a filtered schedule from `Schedule.forever`
   * that will halt when the number of recurrences exceeds 100.
   */
  val oneHundred = Schedule.forever.whileOutput(_ < 100) // I guess 100 since 0 counts

  /**
   * EXERCISE
   *
   * Using `Schedule.identity`, produce a schedule that recurs forever, without delay,
   * returning its inputs.
   */
  def inputs[A]: Schedule[Any, A, A] = Schedule.identity

  /**
   * EXERCISE
   *
   * Using `Schedule#collect`, produce a schedule that recurs forever, collecting its
   * inputs into a list.
   */
  def collectedInputs[A]: Schedule[Any, A, List[A]] = ???

  /**
   * EXERCISE
   *
   * Using  `*>` (`zipRight`), combine `fiveTimes` and `everySecond` but return
   * the output of `everySecond`.
   */
  val fiveTimesEverySecondR = fiveTimes *> everySecond

  /**
   * EXERCISE
   *
   * Produce a jittered schedule that first does exponential spacing (starting
   * from 10 milliseconds), but then after the spacing reaches 60 seconds,
   * switches over to fixed spacing of 60 seconds between recurrences, but will
   * only do that for up to 100 times, and produce a list of the inputs to
   * the schedule.
   */
  import zio.random.Random
  import Schedule.{ collectAll, exponential, fixed, recurs }
  def mySchedule[A]: Schedule[ZEnv, A, List[A]] = ???
}
