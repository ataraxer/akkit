package akkit

import akka.actor._

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly


@compileTimeOnly("Enable macro paradise to expand macro annotations.")
class safeActor(val dispatcher: String) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro SafeActorMacro.impl
}


private class SafeActorMacro(val c: Context) {
  import c.universe._


  def generateCompanionExtensions(actorDef: ClassDef) = {
    val q"class $name(..$fields) extends ..$parents { ..$body }" = actorDef  // "

    val actorName = name.toTypeName
    val namedRef = TypeName(actorName + "Ref")

    val propsExpression = c.prefix.tree match {
      case q"new safeActor($dispatcher)" =>
        q"props.withDispatcher($dispatcher)"

      case q"new safeActor" => q"props"
      case _ => c.abort(c.enclosingPosition, "Invalid annotation parameter")
    }


    q"""
      import akka.actor.{Props, ActorRefFactory}

      type Ref = akkit.SafeRef[$actorName]
      type $namedRef = Ref

      def props(..$fields): akkit.SafeProps[$name] = {
        val props = akka.actor.Props { new $name(..${fields.map(_.name)}) }
        akkit.SafeProps[$name]($propsExpression)
      }

      def apply(..$fields)(implicit factory: akka.actor.ActorRefFactory) = {
        val actor = factory actorOf this.props(..${fields.map(_.name)})
        akkit.SafeRef[$name](actor)
      }
    """
  }


  def transformCompanion(actorDef: ClassDef, companionDef: ModuleDef) = {
    val q"object $name extends ..$parents { ..$body }" = companionDef // "

    q"""
      object $name extends ..$parents {
        ..${generateCompanionExtensions(actorDef)}
        ..$body
      }
    """
  }


  def generateCompanion(actorDef: ClassDef) = {
    q"""
      object ${actorDef.name.toTermName} {
        ..${generateCompanionExtensions(actorDef)}
      }
    """
  }


  def impl(annottees: c.Expr[Any]*): c.Expr[Any] = {
    val result = annottees.map(_.tree).toList match {
      case (actorDef: ClassDef) :: (companionDef: ModuleDef) :: Nil =>
        q"""
          $actorDef
          ${transformCompanion(actorDef, companionDef)}
        """

      case (actorDef: ClassDef) :: Nil =>
        q"""
          $actorDef
          ${generateCompanion(actorDef)}
        """

      case _ =>
        c.abort(c.enclosingPosition, "Invalid annottee")
    }

    c.Expr[Any](result)
  }
}

