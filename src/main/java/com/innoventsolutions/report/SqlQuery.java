package com.innoventsolutions.report;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.innoventsolutions.data.model.Query;

public abstract class SqlQuery<T> implements Query<T> {
	public Connection connection = null;
	public Map<String, String> substitutions = null;
	public Object[] arguments = null;
	public final String query;

	public SqlQuery(final String query) {
		this.query = query;
	}

	public void setConnection(final Connection connection) {
		this.connection = connection;
	}

	public void setSubstitutions(final Map<String, String> substitutions) {
		this.substitutions = substitutions;
	}

	public void setArguments(final Object[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public Stream<T> getStream() {
		if (connection == null) {
			throw new IllegalArgumentException("Connection may not be null");
		}
		String query = this.query;
		if (substitutions != null) {
			for (final String key : substitutions.keySet()) {
				final String value = substitutions.get(key);
				query = query.replace(key, value);
			}
		}
		try {
			final PreparedStatement statement = connection.prepareStatement(query);
			if (arguments != null) {
				for (int i = 0; i < arguments.length; i++) {
					final Object argument = arguments[i];
					statement.setObject(i + 1, argument);
				}
			}
			final ResultSet resultSet = statement.executeQuery();
			final Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(
					Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE) {
				@Override
				public boolean tryAdvance(final Consumer<? super T> action) {
					try {
						if (!resultSet.next()) {
							return false;
						}
					}
					catch (final SQLException e) {
						return false;
					}
					try {
						action.accept(createDataRow(resultSet));
					}
					catch (final SQLException e) {
						return false;
					}
					return true;
				}
			};
			final Stream<T> stream = StreamSupport.stream(spliterator, true);
			return stream;
		}
		catch (final SQLException e) {
			throw new RuntimeException("Failed to execute", e);
		}
	}

	abstract protected T createDataRow(ResultSet resultSet) throws SQLException;

	public static String getQuery(final InputStream inputStream) {
		if (inputStream == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		final Reader reader = new InputStreamReader(inputStream);
		final char[] buffer = new char[0x1000];
		try {
			int charsRead = reader.read(buffer);
			while (charsRead >= 0) {
				sb.append(buffer, 0, charsRead);
				charsRead = reader.read(buffer);
			}
		}
		catch (final IOException e) {
			throw new RuntimeException("Failed to read query", e);
		}
		final String query = sb.toString();
		return query;
	}
}
