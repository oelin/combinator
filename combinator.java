import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.Function;


public interface Combinator<A> {

  A parse(Cursor cursor) throws Exception;


  // sequence operator

  default <B> Combinator<Pair<A, B>> then(Supplier<Combinator<B>> other) {
    return cursor -> then(other.get()).parse(cursor);
  }


  // choice operator

  default Combinator<A> or(Supplier<Combinator<A>> other) {
    return cursor -> or(other.get()).parse(cursor);
  }


  // skip operator

  default Combinator<A> skip(Supplier<Combinator<?>> other) {
    return cursor -> skip(other.get()).parse(cursor);
  }


  // "do" operator (for AST generation)

  default <B> Combinator<B> to(Function<A, B> f) {
    return cursor -> f.apply(parse(cursor));
  }


  // when given combinators directly...

  default <B> Combinator<Pair<A, B>> then(Combinator<B> other) {
    return cursor -> new Pair<>(parse(cursor), other.parse(cursor));
  }


  default Combinator<A> or(Combinator<A> other) {

    return cursor -> {
      Optional<A> self = could(cursor);

      return self.isPresent()? self.get() : other.parse(cursor);
    };
  }


  default Combinator<A> skip(Combinator<?> other) {

    return cursor -> {
      A self = parse(cursor);
      other.parse(cursor);
    
      return self;
    };
  }


  // optional parsing

  default Optional<A> could(Cursor cursor) {
    String here = cursor.string;

    try { 
      return Optional.of(parse(cursor));
    } 
    catch (Exception e) {
      cursor.string = here;

      return Optional.empty();
    }
  }
}
