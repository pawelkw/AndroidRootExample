package com.example.mgr_shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

class StreamGrabber extends Thread {

	private enum Mode {
		LOGCAT, TEXTVIEW
	};

	private static final String LOG_TAG = "Shell";
	private static final String LINE_SEPERATOR = System.getProperty("line.separator");

	private final InputStream is;
	private final int priority;
	private final TextView outputView;

	private final StringBuffer streamBuffer;
	private final Mode mode;

	private final Handler uiHandler;


	StreamGrabber(InputStream is, int priority) {
		this.is = is;
		this.uiHandler = null;
		this.priority = priority;
		this.outputView = null;
		this.streamBuffer = null;
		this.mode = Mode.LOGCAT;
	}

	StreamGrabber(InputStream is, TextView outputView, Handler uiHandler) {
		this.is = is;
		this.uiHandler = uiHandler;
		this.priority = Log.DEBUG;
		this.outputView = outputView;
		this.streamBuffer = new StringBuffer();
		this.mode = Mode.TEXTVIEW;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				write(line);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void write(final String line) {
		switch (mode) {
		case LOGCAT:
			Log.println(priority, LOG_TAG, line);
			break;
		case TEXTVIEW:
			streamBuffer.append(line);
			streamBuffer.append(LINE_SEPERATOR);
			uiHandler.post(new Runnable() {

				@Override
				public void run() {
					outputView.setText(streamBuffer.toString());
					Log.d("aaa", "wtf");
				}
			});
			break;

		}
	}
}
