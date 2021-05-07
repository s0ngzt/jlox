package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Get;
import com.craftinginterpreters.lox.Expr.Logical;
import com.craftinginterpreters.lox.Expr.Set;
import com.craftinginterpreters.lox.Expr.Super;
import com.craftinginterpreters.lox.Expr.This;
import com.craftinginterpreters.lox.Expr.Variable;
import com.craftinginterpreters.lox.Stmt.Block;
import com.craftinginterpreters.lox.Stmt.Class;
import com.craftinginterpreters.lox.Stmt.Expression;
import com.craftinginterpreters.lox.Stmt.Function;
import com.craftinginterpreters.lox.Stmt.If;
import com.craftinginterpreters.lox.Stmt.Print;
import com.craftinginterpreters.lox.Stmt.Return;
import com.craftinginterpreters.lox.Stmt.Var;
import com.craftinginterpreters.lox.Stmt.While;
import java.util.List;

class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

  String print(Expr expr) {
    return expr.accept(this);
  }

  String print(Stmt stmt) {
    return stmt.accept(this);
  }

  @Override
  public String visitAssignExpr(Expr.Assign expr) {
    return parenthesize2("=", expr.name.lexeme, expr.value);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitCallExpr(Call expr) {
    return parenthesize2("call", expr.callee, expr.arguments);
  }

  @Override
  public String visitGetExpr(Get expr) {
    return parenthesize2(".", expr.object, expr.name.lexeme);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) {
      return "nil";
    }
    return expr.value.toString();
  }

  @Override
  public String visitLogicalExpr(Logical expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitSetExpr(Set expr) {
    return parenthesize2("=", expr.object, expr.name.lexeme, expr.value);
  }

  @Override
  public String visitSuperExpr(Super expr) {
    return parenthesize2("super", expr.method);
  }

  @Override
  public String visitThisExpr(This expr) {
    return "this";
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visitVariableExpr(Variable expr) {
    return expr.name.lexeme;
  }

  @Override
  public String visitBlockStmt(Block stmt) {
    StringBuilder builder = new StringBuilder();
    builder.append("(block ");

    for (Stmt statement : stmt.statements) {
      builder.append(statement.accept(this));
    }

    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visitClassStmt(Class stmt) {
    StringBuilder builder = new StringBuilder();
    builder.append("(class ").append(stmt.name.lexeme);

    if (stmt.superclass != null) {
      builder.append(" < ").append(print(stmt.superclass));
    }

    for (Stmt.Function method : stmt.methods) {
      builder.append(" ").append(print(method));
    }

    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visitExpressionStmt(Expression stmt) {
    return parenthesize(";", stmt.expression);
  }

  @Override
  public String visitFunctionStmt(Function stmt) {
    StringBuilder builder = new StringBuilder();
    builder.append("(fun ").append(stmt.name.lexeme).append("(");

    for (Token param : stmt.params) {
      if (param != stmt.params.get(0)) {
        builder.append(" ");
      }
      builder.append(param.lexeme);
    }

    builder.append(") ");

    for (Stmt body : stmt.body) {
      builder.append(body.accept(this));
    }

    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visitIfStmt(If stmt) {
    if (stmt.elseBranch == null) {
      return parenthesize2("if", stmt.condition, stmt.thenBranch);
    }

    return parenthesize2("if-else", stmt.condition, stmt.thenBranch,
        stmt.elseBranch);
  }

  @Override
  public String visitPrintStmt(Print stmt) {
    return parenthesize("print", stmt.expression);
  }

  @Override
  public String visitReturnStmt(Return stmt) {
    if (stmt.value == null) {
      return "(return)";
    }
    return parenthesize("return", stmt.value);
  }

  @Override
  public String visitVarStmt(Var stmt) {
    if (stmt.initializer == null) {
      return parenthesize2("var", stmt.name);
    }

    return parenthesize2("var", stmt.name, "=", stmt.initializer);
  }

  @Override
  public String visitWhileStmt(While stmt) {
    return parenthesize2("while", stmt.condition, stmt.body);
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ").append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  private String parenthesize2(String name, Object... parts) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    transform(builder, parts);
    builder.append(")");

    return builder.toString();
  }

  private void transform(StringBuilder builder, Object... parts) {
    for (Object part : parts) {
      builder.append(" ");
      if (part instanceof Expr) {
        builder.append(((Expr) part).accept(this));
      } else if (part instanceof Stmt) {
        builder.append(((Stmt) part).accept(this));
      } else if (part instanceof Token) {
        builder.append(((Token) part).lexeme);
      } else if (part instanceof List) {
        transform(builder, ((List) part).toArray());
      } else {
        builder.append(part);
      }
    }
  }

  public static void main(String[] args) {
    Expr expression = new Expr.Binary(
        new Expr.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expr.Literal(123)),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(
            new Expr.Literal(45.67)));

    Expr expression2 = new Expr.Assign(
        new Token(TokenType.IDENTIFIER, "a", null, 1),
        new Expr.Literal(100));

    var printer = new AstPrinter();
    System.out.println(printer.print(expression));
    System.out.println(printer.print(expression2));
  }
}
