package sam.functions;

import java.io.IOException;

@FunctionalInterface
public interface IOExceptionFunction<T, R> {
	public R apply(T t) throws IOException;
}
