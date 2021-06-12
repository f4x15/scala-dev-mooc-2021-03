package module3.userDAO

import module3.userService.User
import module3.userService.UserID
import zio.macros.accessible
import zio.{Has, Task, ZLayer}


  object UserDAO{

    type UserDAO = Has[Service]

    trait Service{
      def list(): Task[List[User]]
      def findBy(id: UserID): Task[Option[User]]
    }

    val live: ZLayer[Any, Throwable, UserDAO] = ???
  }
