package com.jaykhon.wireless.wireless.connect;

public interface ResultListener<T> {
	public void onResultsSucceded(T result);
	public void onResultsFail();
}
