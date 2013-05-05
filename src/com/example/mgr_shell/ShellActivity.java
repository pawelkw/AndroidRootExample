package com.example.mgr_shell;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ShellActivity extends Activity {

	public enum Command {
		WHOAMI, SU_WHOAMI, CAT_WIFI, SU_CAT_WIFI
	}

	public static final String COMMAND_WHOAMI = "whoami";
	public static final String COMMAND_WIFI_PASSWORDS = "cat /data/misc/wifi/wpa_supplicant.conf";

	private TextView input, output, error, exitCode;

	private Handler uiHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHandler = new Handler();
		setContentView(R.layout.activity_shell);
		initViews();
	}

	private void executeCommand(Command command) {
		clearPrevious();
		try {
			Process process = runCommand(command);

			StreamGrabber errorGobbler = new StreamGrabber(
					process.getErrorStream(), error, uiHandler);
			StreamGrabber outputGobbler = new StreamGrabber(
					process.getInputStream(), output, uiHandler);

			errorGobbler.start();
			outputGobbler.start();

			int exitVal = process.waitFor();
			setExitCode(exitVal);
			Log.i("Shell", "Exit code: " + exitVal);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void clearPrevious() {
		input.setText("");
		output.setText("");
		error.setText("");
		exitCode.setText("");
	}

	private void setExitCode(int exitVal) {
		if(exitVal != 0)
			exitCode.setTextColor(Color.RED);
		else
			exitCode.setTextColor(Color.GREEN);
		
		exitCode.setText(Integer.toString(exitVal));
	}

	private Process runCommand(Command command) throws IOException {
		Process process = null;
		switch (command) {
		case WHOAMI:
			process = runSimpleCommand(COMMAND_WHOAMI);
		case SU_WHOAMI:
			process = runRootCommand(COMMAND_WHOAMI);
			break;
		case CAT_WIFI:
			process = runSimpleCommand(COMMAND_WIFI_PASSWORDS);
			break;
		case SU_CAT_WIFI:
			process = runRootCommand(COMMAND_WIFI_PASSWORDS);
			break;
		}

		return process;
	}

	private Process runSimpleCommand(String command) throws IOException {
		setInputText(command);

		return Runtime.getRuntime().exec(command);
	}

	private Process runRootCommand(String command) throws IOException {
		String[] cmdArray = new String[] { "su", "-c", command };
		setInputText(cmdArray);

		return Runtime.getRuntime().exec(cmdArray);
	}

	private void setInputText(String... command) {
		StringBuffer commandText = new StringBuffer();
		for (int i = 0; i < command.length; i++) {
			commandText.append(command[i]);
			commandText.append(" ");
		}
		input.setText(commandText.toString());
	}

	private void initViews() {
		input = (TextView) findViewById(R.id.input);
		output = (TextView) findViewById(R.id.output);
		error = (TextView) findViewById(R.id.error);
		exitCode = (TextView) findViewById(R.id.exit_code);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = new MenuInflater(this);
		mi.inflate(R.menu.shell, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_whoami:
			executeCommand(Command.WHOAMI);
			break;
			
		case R.id.action_su_whoami:
			executeCommand(Command.WHOAMI);
			break;

		case R.id.action_wifi:
			executeCommand(Command.CAT_WIFI);
			break;

		case R.id.action_su_wifi:
			executeCommand(Command.SU_CAT_WIFI);

			break;
		}
		return true;
	}
}
