package ru.otus.module4.homework.services

import ru.otus.module4.homework.dao.entity.{Role, RoleCode, User}
import ru.otus.module4.homework.dao.repository.UserRepository
import ru.otus.module4.phoneBook.db
import zio.{Has, RIO, ZIO, ZLayer}
import zio.macros.accessible

@accessible
object UserService {
  type UserService = Has[Service]

  trait Service {
    def listUsers(): RIO[db.DataSource, List[User]]

    def listUsersDTO(): RIO[db.DataSource, List[UserDTO]]

    def addUserWithRole(user: User, roleCode: RoleCode): RIO[db.DataSource, UserDTO]

    def listUsersWithRole(roleCode: RoleCode): RIO[db.DataSource, List[UserDTO]]
  }

  class Impl(userRepo: UserRepository.Service) extends Service {
    val dc: db.Ctx.type = db.Ctx

    import dc._

    def listUsers(): RIO[db.DataSource, List[User]] =
      userRepo.list()


    def listUsersDTO(): RIO[db.DataSource, List[UserDTO]] = {
      userRepo
        .list()
        .flatMap { users =>
          ZIO.foreach(users) {
            user =>
              userRepo.userRoles(user.typedId).map(roles => UserDTO(user, roles.toSet))
          }
        }
    }

    def addUserWithRole(user: User, roleCode: RoleCode): RIO[db.DataSource, UserDTO] = for {
      userCreated <- dc.transaction(
        for {
          userCreated <- userRepo.createUser(user)
          _ <- userRepo.insertRoleToUser(roleCode, user.typedId)
        } yield userCreated
      )
      roles <- userRepo.userRoles(userCreated.typedId)
    } yield UserDTO(userCreated, roles.toSet)

    def listUsersWithRole(roleCode: RoleCode): RIO[db.DataSource, List[UserDTO]] = {
      val users = for {
        usersWithRole <- userRepo.listUsersWithRole(roleCode)
      } yield usersWithRole
      users
        .flatMap { users =>
          ZIO.foreach(users) {
            user =>
              userRepo.userRoles(user.typedId).map(roles => UserDTO(user, roles.toSet))
          }
        }
    }
  }

  val live: ZLayer[UserRepository.UserRepository, Nothing, UserService] =
    ZLayer.fromService[UserRepository.Service, UserService.Service](repo =>
      new Impl(repo)
    )
}

case class UserDTO(user: User, roles: Set[Role])