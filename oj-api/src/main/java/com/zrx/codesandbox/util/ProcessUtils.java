package com.zrx.codesandbox.util;

import com.zrx.codesandbox.model.ExecuteMessage;

import java.io.*;

public class ProcessUtils {

	public static ExecuteMessage compileCode(Process compileProcess) {
		ExecuteMessage executeMessage = new ExecuteMessage();
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;
		BufferedReader errorBufferedReader = null;
		InputStream errorInputStream = null;
		try {
			int exitValue = 0;   // 执行我们的数值;
			inputStream = compileProcess.getInputStream();   // 读取该进程在控制台打印的正常输出信息
			exitValue = compileProcess.waitFor();   // 退出状态值（exit value） 用于判断进程是否成功执行 0表示成功的执行了该进程;
			// 非0：表示失败（如编译错误、命令不存在等）
			executeMessage.setExitValue(exitValue);   // 设置我们执行的结果;
			if (exitValue == 0) {
				StringBuilder compileResultStr = new StringBuilder("");
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				String compileOutputLine;
				while ((compileOutputLine = bufferedReader.readLine()) != null) {
					compileResultStr.append(compileOutputLine).append("\n");
				}
				executeMessage.setMessage(compileResultStr.toString());
				// 读取我们执行成功后的数据信息;
			}
			else {

				// 读取失败后的信息;
				StringBuilder compileResultStr = new StringBuilder("");
				bufferedReader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream(), "GBK"));
				String compileOutputLine;
				while ((compileOutputLine = bufferedReader.readLine()) != null) {
					compileResultStr.append(compileOutputLine).append("\n");
				}
				executeMessage.setMessage(compileResultStr.toString());

				// 跟成功的时候一样的;读取我们需要失败的信息消息;
				errorInputStream = compileProcess.getErrorStream();
				StringBuilder compileErrorResultStr = new StringBuilder("");
				errorBufferedReader = new BufferedReader(new InputStreamReader(errorInputStream));
				String errorCompileOutputLine;
				while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
					compileErrorResultStr.append(errorCompileOutputLine).append("\n");
				}
				executeMessage.setErrorMessage(compileErrorResultStr.toString());
				// 重复我们的的过程;读取我们错误的信息;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				inputStream.close();
				bufferedReader.close();
				if (errorInputStream != null) {
					errorInputStream.close();
				}
				if (errorBufferedReader != null) {
					errorBufferedReader.close();
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			compileProcess.destroy();
		}
		return executeMessage;

		/*
		* 最后的结尾处理;
		* */
	}

	public static ExecuteMessage runCode(Process runProcess, String args) {
		ExecuteMessage executeMessage = new ExecuteMessage();
		OutputStream outputStream = null;
		OutputStreamWriter outputStreamWriter = null;
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;
		InputStream errorInputStream = null;
		BufferedReader errorBufferedReader = null;
		try {
			if (args != null) {
				inputArgs(runProcess, args, outputStream, outputStreamWriter);
			}
			setTime(executeMessage, runProcess);    //设置我们执行的时间;
			setRunResult(runProcess, executeMessage, inputStream, bufferedReader, errorInputStream,
					errorBufferedReader);   // 设置我们成功的信息和我们错误的信息;记录到我们的ExecuteMessage里面;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (outputStreamWriter != null) {
					outputStreamWriter.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (errorInputStream != null) {
					errorInputStream.close();
				}
				if (errorBufferedReader != null) {
					errorBufferedReader.close();
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			runProcess.destroy();
		}
		return executeMessage;
	}

	private static void setTime(ExecuteMessage executeMessage, Process process) throws InterruptedException {
		long start = 0L;
		long end = 0L;
		if (process.isAlive()) {
			start = System.currentTimeMillis();
			process.waitFor();   // 阻塞的等待;
			end = System.currentTimeMillis();
		}
		executeMessage.setTime((end - start));
	}

	private static void inputArgs(Process runProcess, String args, OutputStream outputStream,
			OutputStreamWriter outputStreamWriter) throws IOException {
		outputStream = runProcess.getOutputStream();

		outputStreamWriter = new OutputStreamWriter(outputStream);

		outputStreamWriter.write(args + "\n");
		outputStreamWriter.flush();
	}

	private static void setRunResult(Process runProcess, ExecuteMessage executeMessage, InputStream inputStream,
			BufferedReader bufferedReader, InputStream errorInputStream, BufferedReader errorBufferReader)
			throws IOException {
		inputStream = runProcess.getInputStream();
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder runOutputStringBuilder = new StringBuilder();
		String runOutputLine;
		while ((runOutputLine = bufferedReader.readLine()) != null) {
			runOutputStringBuilder.append(runOutputLine);
		}
		executeMessage.setMessage(runOutputStringBuilder.toString());

		errorInputStream = runProcess.getErrorStream();
		bufferedReader = new BufferedReader(new InputStreamReader(errorInputStream));
		StringBuilder errorRunStringBuilder = new StringBuilder();
		String errorRunOutputLine;
		while ((errorRunOutputLine = bufferedReader.readLine()) != null) {
			errorRunStringBuilder.append(errorRunOutputLine);
		}
		executeMessage.setErrorMessage(errorRunStringBuilder.toString());
	}

}
