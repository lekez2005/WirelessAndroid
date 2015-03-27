package com.jaykhon.wireless.wireless.connect;

import org.json.JSONException;

import java.io.IOException;

public interface Command<T> {
	public T execute();
}
