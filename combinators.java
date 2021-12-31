import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.function.BiFunction;


public class Combinators {

  // singleton for parse failures

  public static Exception fail = new Exception();


  // parse any one of the combinators given

  public static Combinator any(Combinator ...list) {

    return Stream.of(list)
      .reduce(Combinator::or).get();

  }


  // parse all of the combinators given

  public static Combinator<Pair> all(Combinator ...list) {

    return Stream.of(list)
      .reduce((a, b) -> a.then(b)).get();
  }


  // parse a single token

  public static Combinator<String> lex(String token) {
    
    return cursor -> {

      Matcher matcher = Pattern
	.compile(token)
	.matcher(cursor.string);

      if (! matcher.lookingAt()) throw fail;
      cursor.move(matcher.end());

      return matcher
	.toMatchResult()
	.group();
    };
  }


  // parse a combinator any number of times

  public static <A> Combinator<List<A>> some(Combinator<A> other) {
    return some(other, lex(""));
  }


  // parse a combinator at least once

  public static <A> Combinator<List<A>> several(Combinator<A> other) {
    return several(other, lex(""));
  }


  public static <A> Combinator<List<A>> some(Combinator<A> other, Combinator sep) {

    return cursor -> {
    
      List<A> list = new ArrayList<>();
      Optional<A> self;

      do {
	self = other.could(cursor);
	if (self.isPresent()) list.add(self.get());
	else break;

      } while (sep.could(cursor).isPresent());

      return list;
    };
  }


  public static <A> Combinator<List<A>> several(Combinator<A> other, Combinator sep) {
    
    return cursor -> {
      List<A> list = some(other, sep).parse(cursor);

      if (list.size() > 0) return list;
      throw fail;
    };
  }


  // parse initial skips

  public static Skipper skip(Combinator<?> other) {
    return new Skipper(other);
  }


  public static class Skipper {
    private final Combinator<?> combinator;

    public Skipper(Combinator<?> combinator) {
      this.combinator = combinator;
    }

//    public Skipper skip(Combinator<?> other) {
//
//      return new Skipper(cursor -> {
//	combinator.parse(cursor);
//	other.parse(cursor);
//
//	return null;
//      });
//    }

    public <A> Combinator<A> then(Combinator<A> other) {

      return cursor -> {
	combinator.parse(cursor);
	return other.parse(cursor);
      };
    }
  }
  
	
  // parse string-like sentences

  public static <A> Combinator<A> quoted(Combinator<A> content, Combinator quote) {
    return skip(quote).then(content).skip(quote);
  }


  // extract values from a pair

  public static <A, B, C> Function<Pair<A, B>, C> apply(BiFunction<A, B, C> f) {
    return pair -> f.apply(pair.first, pair.second);
  }


  // padding

  public static <A> Combinator<A> white(Combinator<A> content) {
    return quoted(content, lex("[ \t]*"));
  }
}
