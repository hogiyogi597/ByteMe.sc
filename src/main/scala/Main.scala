import cats.{Id, effect}
import cats.data.EitherK
import cats.effect.{ExitCode, IO, IOApp}
import cats.free.Free
import cats.syntax.all._
import dissonance.Discord
import dissonance.data.{Intent, Shard}
import yarn.{JsoupBrowserInterpreter, Yarn}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
//    program.foldMap(JsoupBrowserInterpreter.interpreter[IO])
//      .flatTap(str => IO(println(str)))
//      .map(_ => ExitCode.Success)

    val discordToken = args(0)
    Discord
      .make(discordToken)
      .use {
        case (discord) =>
          discord.subscribe(Shard.singleton, Intent.GuildMessages).compile.drain
      }
      .as(ExitCode.Success)
  }

//  def program(implicit yarn: Yarn.Ops[Yarn]): Free[Yarn, String] = {
//    yarn.getPopular.map(_.toString)
//  }
}
