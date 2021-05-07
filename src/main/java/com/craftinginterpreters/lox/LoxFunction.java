package com.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {

  private final Stmt.Function declaration; // name, params, body
  private final Environment closure;
  private final boolean isInitializer;

  LoxFunction(Stmt.Function declaration, Environment closure,
      boolean isInitializer) {
    this.isInitializer = isInitializer;
    this.closure = closure;
    this.declaration = declaration;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    // create a new environment at each call, enable "recursion"
    // Environment environment = new Environment(interpreter.globals); // enclosing globals (env)
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) { // interesting
      if (isInitializer) {
        return closure.getAt(0, "this");
      }
      return returnValue.value;
    }

    if (isInitializer) {
      return closure.getAt(0, "this");
    }
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }

  LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new LoxFunction(declaration, environment, isInitializer);
  }
}
