package ru.otus.module4.homework.dao.repository

import io.getquill.{EntityQuery, Quoted, Insert}
import zio.{Has, ULayer, ZIO, ZLayer}
import io.getquill.context.ZioJdbc._
import ru.otus.module4.homework.dao.entity._
import ru.otus.module4.phoneBook.db

object UserRepository {

  val dc: db.Ctx.type = db.Ctx

  import dc._

  type UserRepository = Has[Service]

  trait Service {
    def findUser(userId: UserId): QIO[Option[User]]

    def createUser(user: User): QIO[User]

    def createUsers(users: List[User]): QIO[List[User]]

    def updateUser(user: User): QIO[Unit]

    def deleteUser(user: User): QIO[Unit]

    def findByLastName(lastName: String): QIO[List[User]]

    def list(): QIO[List[User]]

    def userRoles(userId: UserId): QIO[List[Role]]

    def insertRoleToUser(roleCode: RoleCode, userId: UserId): QIO[Unit]

    def insertRole(role: Role): QIO[Role]

    def listUsersWithRole(roleCode: RoleCode): QIO[List[User]]

    def findRoleByCode(roleCode: RoleCode): QIO[Option[Role]]
  }

  class ServiceImpl extends Service {

    val userSchema: Quoted[EntityQuery[User]] = querySchema[User]("""user_table""")

    val roleSchema: Quoted[EntityQuery[Role]] = querySchema[Role]("""role""")

    val userToRoleSchema: Quoted[EntityQuery[UserToRole]] = querySchema[UserToRole]("""user_to_role""")

    override def findUser(userId: UserId): QIO[Option[User]] =
      dc.run(
          quote(
            userSchema.filter(_.id == lift(userId.id)).take(1)
          )
        )
        .map(_.headOption)


    override def createUser(user: User): QIO[User] =
      dc.run(
        quote(
          userSchema.insert(lift(user))
        )
      ).as(user)

    override def createUsers(users: List[User]): QIO[List[User]] =
      dc.run(
        quote {
          liftQuery(users).foreach { user => querySchema[User]("""user_table""").insert(user) }
        }
      ).as(users)

    override def updateUser(user: User): QIO[Unit] =
      dc.run(
        quote(
          userSchema.filter(_.id == lift(user.id))
            .update(lift(user))
        )
      ).unit

    override def deleteUser(user: User): QIO[Unit] =
      dc.run(
        quote(
          userSchema.filter(_.id == lift(user.id))
            .delete
        )
      ).unit

    override def findByLastName(lastName: String): QIO[List[User]] =
      dc.run(
        quote(
          userSchema.filter(_.lastName == lift(lastName))
        )
      )

    override def list(): QIO[List[User]] =
      dc.run(
        quote(userSchema)
      )

    override def userRoles(userId: UserId): QIO[List[Role]] =
      dc.run(
        quote(userToRoleSchema
          .filter(_.userId == lift(userId.id))
          .join(roleSchema)
          .on(_.roleId == _.code)
          .map(_._2)
        )
      )

    override def insertRoleToUser(roleCode: RoleCode, userId: UserId): QIO[Unit] =
      dc.run(
        quote(
          userToRoleSchema.insert(lift(UserToRole(roleCode.code, userId.id)))
        )
      ).unit

    override def listUsersWithRole(roleCode: RoleCode): QIO[List[User]] =
      dc.run(
        quote(
          userToRoleSchema.filter(_.roleId == lift(roleCode.code)).join(userSchema).on(_.userId == _.id).map(_._2)
        )
      )

    override def findRoleByCode(roleCode: RoleCode): QIO[Option[Role]] =
      dc.run(
          quote(
            roleSchema.filter(_.code == lift(roleCode.code)).take(1)
          )
        )
        .map(_.headOption)

    override def insertRole(role: Role): QIO[Role] =
      dc.run(
        quote(
          roleSchema.insert(lift(role))
        )
      ).as(role)
  }

  val live: ULayer[UserRepository] = ZLayer.succeed(new ServiceImpl)
}