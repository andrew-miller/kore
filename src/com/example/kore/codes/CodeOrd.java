package com.example.kore.codes;

import java.io.Serializable;

import com.example.unsuck.Boom;
import com.example.unsuck.Null;

import fj.F;
import fj.Ord;
import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Option;

/* Code can be mapped injectively to List<String> like this:
 *    +       +     +    +      +      +     +     +     +     +    +    +    +     +    +    +
 * A  B  C -> [     A -> [    A   B -> [    A B -> [    A B -> [    A -> [    A  -> [    A -> [
 *            A     +    A             A      +    A    + +    A    +    A    <>    A    +    A
 *            *     B    +             *      C    *    C D    +    C    +          <    B    +
 *            [     +    [             [           [           [    +    [          >   <A>   [
 *            ]     C    B             ]           ]           C    B    C          ]         B
 *            B          +             B           B           *    +    +                    <
 *            *          [             *           +           [    D    [                    A
 *            [          C             [           [           ]         B                    >
 *            ]          *             ]           C           ]         +                    ]
 *            C          [             ]           *           B         [                    ]
 *            *          ]                         [           +         D
 *            [          ]                         ]           [         *
 *            ]          ]                         ]           D         [
 *            ]          ]                         ]           *         ]
 *                                                             [         ]
 *                                                             ]         ]
 *                                                             ]         ]
 *                                                             ]         ]
 */
public final class CodeOrd {
  private static List<String> f(Code c, List<String> l) {
    l = l.cons(c.tag.toString());
    l = l.cons("[");
    for (P2<Label, CodeRef> lc : c.labels) { // fuck... don't know if labels
                                             // will be in order
      l = l.cons(lc._1().label);
      CodeRef cr = lc._2();
      switch (cr.tag) {
      case PATH:
        l = ff(cr.path, l);
        break;
      case CODE:
        l = f(c, l);
        break;
      default:
        Boom.boom();
      }
    }
    l = l.cons("]");
    return l;
  }

  private static List<String> ff(List<Label> path, List<String> l) {
    return null;
  }

  public static Ord<Code> ord() {
    return Ord.p2Ord(Ord.stringOrd, Ord.listOrd(Ord.stringOrd)).comap(
        new F<Code, P2<String, List<String>>>() {
          @Override
          public List<String> f(Code c) {
            Null.notNull(c);
            return serialize(c);

          };
        });
  }
}
