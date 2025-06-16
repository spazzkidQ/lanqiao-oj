package com.zrx.codesandbox.impl.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.zrx.codesandbox.CodeSandBox;
import com.zrx.codesandbox.enums.QuestionExecuteResultEnum;
import com.zrx.codesandbox.model.ExecuteCodeRequest;
import com.zrx.codesandbox.model.ExecuteCodeResponse;
import com.zrx.codesandbox.model.ExecuteMessage;
import com.zrx.codesandbox.model.JudgeInfo;
import com.zrx.codesandbox.threads.TimeManagerThread;
import com.zrx.codesandbox.util.LanguageCmdUtils;
import com.zrx.codesandbox.util.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class CodeSandboxTemplate implements CodeSandBox {

	public static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

	protected String GLOBAL_CODE_FILE_NAME;    // 目标文件代码的名称;

	public static final String SECURITY_MANAGER_CLASSNAME;

	public static final int TIME_OUT = 5000;  // 最大的执行时间;

	public static final String SECURITY_MANAGER_PATH;

	private static String globalCodePathName;  //  目标代码的执行文件的路径名称;

	private String userCodeParentPath;  // 用户代码父级的路径;

	private String userCodePath;

	protected String language;

	protected String containerId;  // docker容器Id;

	protected DockerClient dockerClient;   // docker客户端;

	protected String ImageName;  //docker镜像的名称; ;

	protected String volumnName;  // docker挂卷的名称;

	protected volatile long memory;   // 耗费内存;
	protected volatile long init;   // 耗费内存;


	static {
		String property = System.getProperty("user.dir");
		SECURITY_MANAGER_PATH = property + File.separator + "src/main/resources/security";
		SECURITY_MANAGER_CLASSNAME = "OjSecurityManager";
	}

	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
		language = executeCodeRequest.getLanguage();   // 获取到我们当前的语言;
		List<String> inputList = executeCodeRequest.getInputList();   // 输入样列;
		System.err.println(inputList);
		String code = executeCodeRequest.getCode();   // 用户代码实现;
		File userCodeFile = saveCodeFile(code);  // 用户代码西写到我们下级传入进来的文件里面;
		ExecuteMessage compileExecuteMessage = null;   // 编译执行消息
		List<ExecuteMessage> runExecuteMessageList = null;   // 执行消息列表运行
		try {
			compileExecuteMessage = compileFile(userCodeFile);
			startDocker();   // 开启我们的docker;
			createContainer();   // 创建我们的docker容器;
			runContainer();   // 运行我们的容器;
			StatsCmd statsCmd = calculateMemory();   // 计算使用前的内存大小;
			while (memory < 0) ;   // 小于0的话表示我们上面的calculateMemory()方法根本就没有执行完毕;
			init = memory;
			runExecuteMessageList = runFile(inputList);
			stopContainer(); // 停止我们的容器;
			removeContainer(statsCmd);   // 删除我们的容器;
		} catch (Exception e) {
			return getErrorResponse(e);
		}

		ExecuteCodeResponse executeCodeResponse = getOutputResponse(runExecuteMessageList);

		boolean isDeleteFile = clearFile(userCodeFile);

		if (!isDeleteFile) {
			log.error("delete file error,userCodeFilePath={}", userCodeFile.getAbsolutePath());
		}
		System.err.println(executeCodeResponse);
		while (init == memory) ;
		executeCodeResponse.getJudgeInfo().setMemory(Math.abs(memory - init) / 8/1024);
		return executeCodeResponse;
	}

	public void startDocker() {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//				 .withDockerHost("unix:///var/run/docker.sock")   // 这一行是用于访问我们Linux上面的docker;
//				.withDockerHost("tcp://localhost:2375")   // 这一行是用于访问我们Linux上面的docker;
				// windows 使用管道连接
				.withDockerHost("npipe:////./pipe/docker_engine")    // 这一行用于访问我们windows上面的docker;
				// 禁用TLS验证（测试环境）
				.withDockerTlsVerify(false)
				.build();
		DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
				.dockerHost(config.getDockerHost())   // docker的主机地址;
				.sslConfig(config.getSSLConfig())
				.build();

		dockerClient = DockerClientBuilder.getInstance(config)
				.withDockerHttpClient(httpClient)
				.build();
	}


	/*
	 * @Param 创建docker的容器;
	 * */
//	public void createContainer() {
//		CreateContainerCmd containerCmd = dockerClient.createContainerCmd(ImageName);  // 指定我们镜像的名称;
//		HostConfig hostConfig = new HostConfig();
//		hostConfig.setBinds(new Bind(userCodeParentPath, new Volume(volumnName)));
//		// 将宿主机的目录（userCodeParentPath）挂载到容器的指定卷（volumnName）
//
//		/*
//		* @Param  设置资源限制
//		* */
//		hostConfig.withCpuCount(1L); // 限制使用 1 个 CPU 核心
//		hostConfig.withMemorySwap(0L);  // 禁用 Swap 内存
//		hostConfig.withMemory(100 * 1000 * 1000L);   // 限制内存为 100MB
//
//		/*
//		* @param 容器安全配置
//		* */
//		CreateContainerResponse createContainerResponse = containerCmd.withHostConfig(hostConfig)
//			.withNetworkDisabled(true) // 禁用网络（防止对外攻击）
//			.withReadonlyRootfs(true)   // 根文件系统只读（防止写入系统文件）
//			.withAttachStderr(true)  // 捕获标准错误流
//			.withAttachStdin(true)  // 允许标准输入（如需交互）
//			.withAttachStdout(true)  // 捕获标准输出流
//			.withTty(true)   // 分配伪终端（支持交互式命令）
//			.exec();   // 执行创建操作	;
//		String responseId = createContainerResponse.getId();
//		containerId = responseId;
//	}
	public static String convertToWSLPath(String windowsPath) {
		// 确保 Windows 路径是以 C: 或其他驱动器字母开头的
		if (windowsPath.startsWith("C:\\")) {
			// 将驱动器 C: 替换为 /mnt/c
			return "/mnt" + windowsPath.substring(2).replace("\\", "/");
		}
		// 其他情况根据实际需要处理
		return windowsPath.replace("\\", "/");
	}


	private long currentMemory;  // 存储当前最新的内存值

	public void createContainer() {
		try {
			// 1. 初始化容器命令
			System.out.println("[1/5] 正在初始化容器命令，镜像: " + ImageName);
			CreateContainerCmd containerCmd = dockerClient.createContainerCmd(ImageName);

			// 2. 配置挂载卷
			HostConfig hostConfig = new HostConfig();
//			String s = convertToWSLPath(userCodeParentPath);
			hostConfig.setBinds(new Bind(userCodeParentPath, new Volume(volumnName)));

			// 3. 设置资源限制
			System.out.println("[3/5] 资源限制: CPU=1核, 内存=100MB, Swap=禁用");
			hostConfig.withCpuCount(1L)
					.withMemory(500 * 1000 * 1000L);

			// 4. 安全与 I/O 配置
			System.out.println("[4/5] 安全配                    .withMemorySwap(0L)\n置: 网络禁用, 根文件系统只读");
			System.out.println("      I/O 配置: 启用输入/输出捕获, 分配伪终端");
			CreateContainerResponse createContainerResponse = containerCmd
					.withHostConfig(hostConfig)
					.withNetworkDisabled(true)
					.withReadonlyRootfs(true)
					.withAttachStderr(true)
					.withAttachStdin(true)
					.withAttachStdout(true)
					.withTty(true)
					.exec();

			// 5. 保存容器 ID
			containerId = createContainerResponse.getId();
			System.out.println("[5/5] 容器创建成功！ID: " + containerId);

		} catch (NotFoundException e) {
			System.err.println("错误: 镜像不存在 - " + ImageName);
			e.printStackTrace();
		} catch (DockerClientException e) {
			System.err.println("Docker 客户端错误: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("未知错误: " + e.getClass().getSimpleName());
			e.printStackTrace();
		}
	}

	public void runContainer() {
		dockerClient.startContainerCmd(containerId).exec();
	}

	public StatsCmd calculateMemory() {
		memory = Long.MIN_VALUE;
		StatsCmd statsCmd = dockerClient.statsCmd(containerId);
		ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

			@Override
			public void onStart(Closeable closeable) {

			}

			@Override
			public void onNext(Statistics statistics) {
				System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
				memory = statistics.getMemoryStats().getUsage();
				System.err.println(memory);
			}

			@Override
			public void close() throws IOException {

			}

			@Override
			public void onError(Throwable throwable) {

			}

			@Override
			public void onComplete() {

			}
		});
		statsCmd.exec(statisticsResultCallback);
		return statsCmd;
	}


	public void stopContainer() {
		try {
			Runtime.getRuntime().exec(String.format("docker stop -f %s", containerId));
			// dockerClient.stopContainerCmd(containerId).exec();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void removeContainer(StatsCmd statsCmd) {
		try {
			Runtime.getRuntime().exec(String.format("docker rm -f %s", containerId));
			// dockerClient.removeContainerCmd(containerId).exec();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		statsCmd.close();
	}

	public void removeContainer() {
		try {
			// Process exec = Runtime.getRuntime().exec(String.format("docker rm -f %s",
			// containerId));
			dockerClient.removeContainerCmd(containerId).exec();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * @Param code用户提交的代码;
	 * */
	public File saveCodeFile(String code) {
		String userDir = System.getProperty("user.dir");   // 获取当前java程序的工作目录;即项目的根目录或运行时的工作目录;
		globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;   //  File.separator 不同型号系统的分隔符;
		if (!FileUtil.exist(globalCodePathName)) {
			FileUtil.mkdir(globalCodePathName);
		}

		// 当前工作目录/tmpCode/UUID/Main.java
		userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
		userCodePath = userCodeParentPath + File.separator + GLOBAL_CODE_FILE_NAME;
		File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);   // 将我们的写的代码写入到拼接的路径下面;并且以为utf-8的形式;
		// 返回我们的文件的形式;
		return userCodeFile;
	}

	public ExecuteMessage compileFile(File userCodeFile) throws Exception {

		/*
		 * @param userCodeFile.getAbsolutePath() 写入文件的绝对路径;
		 * @param userCodeParentPath 代码的父路径;
		 * @param language 编程语言;
		 * */
		String compileCmd = LanguageCmdUtils.getCompileCMD(userCodeFile.getAbsolutePath(), userCodeParentPath,
				language);   // 返回的是需要执行我们的cmd的命令;
		if (compileCmd == null)
			return null;
		try {
			// 在 Java 程序中调用操作系统的命令行，执行指定的编译命令。

			Process compileProcess = Runtime.getRuntime().exec(compileCmd);    // 返回我们执行后的编译信息;
			ExecuteMessage executeMessage = ProcessUtils.compileCode(compileProcess);   // 返回编译信息;exitValue + message + errorMessage;
			if (executeMessage.getExitValue() != 0) {
				throw new RuntimeException("编译错误：\n错误的原因是：" + executeMessage.getErrorMessage());
			}
			return executeMessage;
		} catch (Exception e) {
			throw e;
		}
	}

	public List<ExecuteMessage> runFile(List<String> inputList) throws IOException, InterruptedException {
		long startMemory = memory;
		System.err.println("我傻逼" + memory);
		System.out.println("开始的内存---" + memory);
		List<ExecuteMessage> executeMessagesList = new ArrayList<>();  //存储每个输入用例的结果;
		if (inputList == null || inputList.isEmpty()) {
			Runtime runtime = Runtime.getRuntime();
			String runCmd = LanguageCmdUtils.getRunCMD(volumnName, language, containerId);   // 执行我们当前指定的docker容器;
			System.err.println(runCmd);
			Process runProcess = runtime.exec(runCmd);
			System.err.println(runProcess);

			ExecuteMessage executeMessage = null;   // 新的执行信息;
			TimeManagerThread timeManagerThread = new TimeManagerThread(TIME_OUT);
			timeManagerThread.setProcess(runProcess);
			timeManagerThread.start();
			executeMessage = ProcessUtils.runCode(runProcess, null);
			executeMessage.setMemory(memory);
			executeMessagesList.add(executeMessage);
			if (timeManagerThread.IsTime_out()) {
				throw new RuntimeException("超时异常");
			}
			timeManagerThread.stop();   // 打断执行的线程;
		} else {
			for (String inputArgs : inputList) {
				try {
					Runtime runtime = Runtime.getRuntime();
					String runCmd = LanguageCmdUtils.getRunCMD(volumnName, language, containerId);
					System.out.println(runCmd);
					Process runProcess = runtime.exec(runCmd);
					ExecuteMessage executeMessage = null;
					TimeManagerThread timeManagerThread = new TimeManagerThread(TIME_OUT);
					timeManagerThread.setProcess(runProcess);
					timeManagerThread.start();
					executeMessage = ProcessUtils.runCode(runProcess, inputArgs);
					System.out.println("结束的进程-----" + memory);
					executeMessage.setMemory(memory);
					executeMessagesList.add(executeMessage);
					if (timeManagerThread.IsTime_out()) {
						stopContainer();
						removeContainer();
						throw new RuntimeException("超时异常");
					}
					timeManagerThread.stop();
				} catch (Exception e) {
					throw e;
				}
			}
		}
		return executeMessagesList;
	}

	public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessagesList) {
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		JudgeInfo judgeResult = new JudgeInfo();
		List<String> outputList = new ArrayList<>();
		long maxTime = 0L;
		long maxMemory = 0L;
		for (ExecuteMessage executeMessage : executeMessagesList) {
			String errorMessageStr = executeMessage.getErrorMessage();
			if (StrUtil.isNotBlank(errorMessageStr)) {
				executeCodeResponse.setMessage(QuestionExecuteResultEnum.RunException.getText());
				executeCodeResponse.setStatus(QuestionExecuteResultEnum.RunException.getValue());
				break;
			}
			Long time = executeMessage.getTime();
			Long memory = executeMessage.getMemory();
			maxTime = Math.max(time, maxTime);
			maxMemory = Math.max(maxMemory, memory);
			outputList.add(executeMessage.getMessage());
		}
		if (outputList.size() == executeMessagesList.size()) {
			executeCodeResponse.setMessage(QuestionExecuteResultEnum.ExecuteSuccess.getText());
			executeCodeResponse.setStatus(2);
		}
		judgeResult.setMemory(maxMemory / 1024 / 8);
		judgeResult.setTime(maxTime);
		executeCodeResponse.setOuputList(outputList);
		executeCodeResponse.setJudgeInfo(judgeResult);
		return executeCodeResponse;
	}

	public boolean clearFile(File userCodeFile) {
		if (userCodeFile.getParentFile() != null) {
			boolean del = FileUtil.del(userCodeParentPath);
			System.out.println("消除:" + (del ? "成功" : "失败"));
			return del;
		}
		return true;
	}

	public ExecuteCodeResponse getErrorResponse(Throwable e) {
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		executeCodeResponse.setOuputList(new ArrayList<>());
		JudgeInfo judgeInfo = new JudgeInfo();
		judgeInfo.setMessage(e.getMessage());
		if (memory > 0) {
			judgeInfo.setMemory(memory / 1024 / 8);
		}
		judgeInfo.setMessage(desensitizationStr(e.getMessage()));
		if (e.getMessage().contains("编译")) {
			executeCodeResponse.setMessage(QuestionExecuteResultEnum.CompileFailure.getText());
			executeCodeResponse.setStatus(QuestionExecuteResultEnum.CompileFailure.getValue());
		} else if (e.getMessage().contains("超时")) {
			executeCodeResponse.setMessage(QuestionExecuteResultEnum.OutOfTimeException.getText());
			executeCodeResponse.setStatus(QuestionExecuteResultEnum.OutOfTimeException.getValue());
		} else {
			executeCodeResponse.setMessage(QuestionExecuteResultEnum.RunException.getText());
			executeCodeResponse.setStatus(QuestionExecuteResultEnum.RunException.getValue());
		}
		executeCodeResponse.setJudgeInfo(new JudgeInfo());
		return executeCodeResponse;
	}

	public String desensitizationStr(String information) {
		if (information != null) {
			information = information.replaceAll(userCodePath, "");
			information = information.replaceAll(userCodeParentPath, "");
		}
		return information;
	}

}
