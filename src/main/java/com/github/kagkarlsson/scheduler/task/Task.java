/**
 * Copyright (C) Gustav Karlsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kagkarlsson.scheduler.task;

import java.io.*;
import java.lang.reflect.ParameterizedType;

import static com.github.kagkarlsson.scheduler.task.Task.Serializer.JAVA_SERIALIZER;

public abstract class Task<T> implements ExecutionHandler<T> {
	protected final String name;
	private final FailureHandler failureHandler;
	private final DeadExecutionHandler deadExecutionHandler;
	protected final Serializer<T> serializer;
	private Class<T> dataClass;

	public Task(String name, Class<T> dataClass, FailureHandler failureHandler, DeadExecutionHandler deadExecutionHandler) {
		this(name, dataClass, failureHandler, deadExecutionHandler, JAVA_SERIALIZER);
	}

	@SuppressWarnings("unchecked")
	public Task(String name, Class<T> dataClass, FailureHandler failureHandler, DeadExecutionHandler deadExecutionHandler,
			Serializer<T> serializer) { // TODO: remove serializer
		this.name = name;
		this.dataClass = dataClass;
		this.failureHandler = failureHandler;
		this.deadExecutionHandler = deadExecutionHandler;
		this.serializer = serializer;
	}

	public String getName() {
		return name;
	}
	
	public Class<T> getDataClass() {
		return dataClass;
	}

	public void setDataClass(Class<T> dataClass) {
		this.dataClass = dataClass;
	}

	public TaskInstance<T> instance(String id) {
		return new TaskInstance<>(this.name, id);
	}

	public TaskInstance<T> instance(String id, T data) {
		return new TaskInstance<>(this.name, id, data);
	}

	public abstract CompletionHandler execute(TaskInstance<T> taskInstance, ExecutionContext executionContext);

	public FailureHandler getFailureHandler() {
		return failureHandler;
	}

	public DeadExecutionHandler getDeadExecutionHandler() {
		return deadExecutionHandler;
	}

	@Override
	public String toString() {
		return "Task " + "task=" + getName();
	}

	public interface Serializer2 {
		byte[] serialize(Object data);

		<T> T deserialize(Class<T> clazz, byte[] serializedData);

		Serializer2 JAVA_SERIALIZER = new Serializer2() {

			public byte[] serialize(Object data) {
				if (data == null)
					return null;
				try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutput out = new ObjectOutputStream(bos)) {
					out.writeObject(data);
					return bos.toByteArray();
				} catch (Exception e) {
					throw new RuntimeException("Failed to serialize object", e);
				}
			}

			public <T> T deserialize(Class<T> clazz, byte[] serializedData) {
				if (serializedData == null)
					return null;
				try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedData);
						ObjectInput in = new ObjectInputStream(bis)) {
					return clazz.cast(in.readObject());
				} catch (Exception e) {
					throw new RuntimeException("Failed to deserialize object", e);
				}
			}
		};
	}

	public interface Serializer<T> {
		byte[] serialize(T data);

		T deserialize(byte[] serializedData);

		Serializer NO_SERIALIZER = new Serializer<Void>() {
			@Override
			public byte[] serialize(Void data) {
				return new byte[0];
			}

			@Override
			public Void deserialize(byte[] serializedData) {
				return null;
			}
		};
		Serializer JAVA_SERIALIZER = new Serializer<Object>() {
			public byte[] serialize(Object data) {
				if (data == null)
					return null;
				try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutput out = new ObjectOutputStream(bos)) {
					out.writeObject(data);
					return bos.toByteArray();
				} catch (Exception e) {
					throw new RuntimeException("Failed to serialize object", e);
				}
			}

			public Object deserialize(byte[] serializedData) {
				if (serializedData == null)
					return null;
				try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedData);
						ObjectInput in = new ObjectInputStream(bis)) {
					return in.readObject();
				} catch (Exception e) {
					throw new RuntimeException("Failed to deserialize object", e);
				}
			}
		};
	}
}
