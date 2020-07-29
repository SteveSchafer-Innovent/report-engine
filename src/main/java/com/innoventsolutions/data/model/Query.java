package com.innoventsolutions.data.model;

import java.util.stream.Stream;

public interface Query<T> {
	Stream<T> getStream();
}
