package com.example.kore.codes;

import java.util.HashMap;
import java.util.Map;

import com.example.kore.utils.CodeUtils;

public class Blob {
  private String id;
  public Code in;
  public Code out;

  public static class Pipe extends Blob {
    public Blob[] args;

    public Pipe(Blob... args) {
      super(args[0].in, args[args.length - 1].out);
      this.args = args;
    }

    @Override
    public String toString() {
      String result = "[ " + args[0];
      for (int i = 1; i < args.length; i++) {
        result += " | " + args[i];
      }
      return result + " ]";

    }
  }

  public static class Union extends Blob {
    public Blob[] args;

    public Union(Blob... args) {
      super(args[0].in, args[0].out);
      this.args = args;
    }

    @Override
    public String toString() {
      String result = "[ " + args[0];
      for (int i = 1; i < args.length; i++) {
        result += ", " + args[i];
      }
      return result + " ]";

    }
  }

  public static class Product extends Blob {
    public Label[] order;
    public Map<Label, Blob> args;

    public Product(Label[] order, Map<Label, Blob> args) {
      this.order = order;
      this.args = args;
      in = CodeUtils.unit;

      Map<Label, Code> subType = new HashMap<Label, Code>();
      for (int i = 0; i < order.length; i++)
        subType.put(order[i], args.get(order[i]).out);
      out = Code.newProduct(subType);
    }

    @Override
    public String toString() {
      if (order.length == 0)
        return "{}";
      String result = "{ '" + order[0] + " " + args.get(order[0]);
      for (int i = 1; i < order.length; i++) {
        result += ", '" + order[i] + " " + args.get(order[i]);
      }
      return result + " }";
    }
  }

  public static class Proj extends Blob {
    public Blob arg;
    public Label label;

    public Proj(Blob arg, Label label) {
      super(arg.in, arg.out.edges.get(label));
      this.arg = arg;
      this.label = label;
    }

    @Override
    public String toString() {
      return "[ " + arg + "." + label + " ]";
    }
  }

  public static class Abs extends Blob {
    public Blob.Var var;
    public Pattern pattern;
    public Blob body;

    public Abs(Blob.Var var, Pattern pattern, Blob body) {
      super(pattern.type, body.out);
      this.var = var;
      this.pattern = pattern;
      this.body = body;
    }

    @Override
    public String toString() {
      return "( " + var + " " + pattern + " -> " + body + " )";
    }

  }

  public static class Cons extends Blob {
    public Label label;
    public Blob arg;

    public Cons(Label label, Code code, Blob arg) {
      super(arg.in, code);
      this.label = label;
      this.arg = arg;
    }

    @Override
    public String toString() {
      return "[ '" + label + " " + arg + " ]";
    }
  }

  public static class Var extends Blob {
    public final String name;

    public Var(String name, Code type) {
      super(CodeUtils.unit, type);
      this.name = name;
    }

    @Override
    public String toString() {
      return "$" + name;
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Blob() {
    this(CodeUtils.unit, CodeUtils.unit);
  }

  public Blob(Code in, Code out) {
    this.in = in;
    this.out = out;
  }

  @Override
  public String toString() {
    return "?";
  }
}
