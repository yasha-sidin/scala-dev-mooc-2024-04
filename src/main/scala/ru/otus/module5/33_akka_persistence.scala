package ru.otus.module5

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.Effect
import ru.otus.module5.Product.{AddProduct, ChangeTitle, RemoveProduct}


case class Product(id: Int, title: String)

object Product{

  type Command = ProductCommand
  sealed trait ProductCommand
  case class AddProduct(product: Product) extends ProductCommand
  case class ChangeTitle(newTitle: String) extends ProductCommand
  case class RemoveProduct(product: Product) extends ProductCommand

  type Event = ProductEvent
  sealed trait ProductEvent
  case class ProductAdded(id: Int, product: Product) extends ProductEvent
  case class TitleChanged(id: Int, newTitle: String) extends ProductEvent
  case class ProductRemoved(id: Int, product: Product) extends ProductEvent

  sealed trait State
  case object BlankState extends State

  case class ProductState(product: Product) extends State{
    def productId: Int = product.id
    def withTitle(newTitle: String): ProductState =
      copy(product = product.copy(title = newTitle))
  }

  object State{
    def empty: State = BlankState
  }

  def apply(entityId: Int, persistenceId: PersistenceId): Behavior[ProductCommand] =
    Behaviors.setup{ ctx =>
      ctx.log.info("Starting Product {}", entityId)
      EventSourcedBehavior[Command, Event, State](
        persistenceId,
        emptyState = State.empty,
        commandHandler,
        eventHandler
      )
    }

  private val commandHandler: (State, Command) => Effect[Event, State] = {(state, command) =>
    state match {
      case BlankState =>
        command match {
          case cmd: AddProduct => addProduct(cmd)
          case ChangeTitle(_) => Effect.unhandled
          case RemoveProduct(_) => Effect.unhandled
        }
      case ps: ProductState =>
        command match {
          case AddProduct(_) => Effect.unhandled
          case cmd: ChangeTitle => changeTitle(ps, cmd)
          case cmd: RemoveProduct => removeProduct(cmd)
        }
    }
  }

  private val eventHandler: (State, Event) => State = {(state, event) =>
    state match {
      case BlankState =>
        event match {
          case ProductAdded(id, product) =>
            ProductState(product)
          case TitleChanged(_, _) =>
            throw new IllegalStateException(s"unexpected event [$event] in state [${state}]")
          case ProductRemoved(_, _) =>
            throw new IllegalStateException(s"unexpected event [$event] in state [${state}]")

        }
      case ps @ ProductState(_) =>
        event match {
          case ProductAdded(_, _) =>
            throw new IllegalStateException(s"unexpected event [$event] in state [${state}]")
          case TitleChanged(_, newTitle) =>
            ps.withTitle(newTitle)
          case ProductRemoved(_, _) => BlankState
        }
    }
  }

  private def addProduct(cmd: AddProduct): Effect[Event, State] = {
    val evt = ProductAdded(cmd.product.id, cmd.product)
    Effect.persist(evt)
  }

  private def removeProduct(cmd: RemoveProduct): Effect[Event, State] = {
    val evt = ProductRemoved(cmd.product.id, cmd.product)
    Effect.persist(evt)
  }

  private def changeTitle(state: ProductState, cmd: ChangeTitle): Effect[Event, State] = {
    val evt = TitleChanged(state.productId, cmd.newTitle)
    Effect.persist(evt)
  }


}

object ProductApp{
  def main(args: Array[String]): Unit = {
    val system = ActorSystem(Product(1, PersistenceId.ofUniqueId("Product")), "ProductApp")
//    system ! ChangeTitle("product 1.1")
    system ! RemoveProduct(Product(1, "product 1"))
  }
}